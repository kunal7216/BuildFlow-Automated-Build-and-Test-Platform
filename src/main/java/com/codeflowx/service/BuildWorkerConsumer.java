package com.codeflowx.service;

import com.codeflowx.model.BuildRun;
import com.codeflowx.model.BuildStatus;
import com.codeflowx.model.BuildStage;
import com.codeflowx.model.BuildLog;
import com.codeflowx.repository.BuildRunRepository;
import com.codeflowx.repository.BuildStageRepository;
import com.codeflowx.repository.BuildLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.nio.file.Path;
import java.util.*;

@Service
public class BuildWorkerConsumer {
  private final ObjectMapper om=new ObjectMapper();
  private final BuildRunRepository buildRepo;
  private final DockerExecutionService dockerService;
  private final LogStreamingServiceV2 logStreamingService;
  private final BuildStageRepository stageRepo;
  private final BuildLogRepository logRepo;
  private final ArtifactService artifactService;
  private final FailureClassifierService failureClassifier;
  private final BuildOrchestrationService orchestrationService;

  @Autowired
  public BuildWorkerConsumer(BuildRunRepository buildRepo,
                             DockerExecutionService dockerService,
                             LogStreamingServiceV2 logStreamingService,
                             BuildStageRepository stageRepo,
                             BuildLogRepository logRepo,
                             ArtifactService artifactService,
                             FailureClassifierService failureClassifier,
                             BuildOrchestrationService orchestrationService){
    this.buildRepo=buildRepo; this.dockerService=dockerService; this.logStreamingService=logStreamingService; this.stageRepo=stageRepo; this.logRepo=logRepo; this.artifactService=artifactService; this.failureClassifier=failureClassifier; this.orchestrationService=orchestrationService;
  }

  @KafkaListener(topics="codeflow.build.created", groupId="codeflow-worker-group")
  @Transactional
  public void onBuildCreated(String payload){
    Path workspace = null;
    BuildRun run = null;
    try {
      Map map=om.readValue(payload, Map.class);
      String runId=(String)map.get("runId");
      run=buildRepo.findByRunId(runId).orElseThrow();
      run.setStatus(BuildStatus.RUNNING);
      run.setStartedAt(Instant.now());
      buildRepo.save(run);
      String yaml=(String)map.get("yamlContent");
      // execute build, returns workspace
      workspace = dockerService.executeBuild(run, yaml, line -> {
        BuildLog l = new BuildLog(); l.setBuildRunId(run.getId()); l.setStageId(null); l.setLogLine(line); l.setStreamType("stdout");
        logRepo.save(l);
        logStreamingService.appendAndEmit(run.getRunId(), "build", line);
      });

      // collect artifacts
      Map parsed = new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory()).readValue(yaml, Map.class);
      List<String> artifacts = (List<String>) parsed.getOrDefault("artifacts", Collections.emptyList());
      if(workspace!=null && artifacts!=null && !artifacts.isEmpty()){
        artifactService.uploadArtifactsFromWorkspace(workspace, artifacts, run.getId());
      }

      run.setStatus(BuildStatus.SUCCESS);
      run.setCompletedAt(Instant.now()); run.setDurationMs(Instant.now().toEpochMilli()-run.getStartedAt().toEpochMilli());
      buildRepo.save(run);
      logStreamingService.emitEvent(run.getRunId(), "build.completed", "SUCCESS");
    } catch(Exception ex){
      ex.printStackTrace();
      // classify failure
      com.codeflowx.model.FailureType ftype;
      try {
        ftype = failureClassifier.classify(ex);
      } catch(Exception ignore){ ftype = com.codeflowx.model.FailureType.UNKNOWN; }

      try{
        Map payloadMap = om.readValue(payload, Map.class);
        String yamlFromPayload = (String) payloadMap.getOrDefault("yamlContent", "");
        Map parsed = new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory()).readValue(yamlFromPayload, Map.class);
        int maxRetries = ((Number)parsed.getOrDefault("retries", 0)).intValue();
        int currRetry = (run==null || run.getRetryCount()==null)?0:run.getRetryCount();
        if((ftype!=com.codeflowx.model.FailureType.COMPILATION_ERROR && ftype!=com.codeflowx.model.FailureType.TEST_FAILURE) && currRetry<maxRetries && run!=null){
          // schedule retry
          orchestrationService.scheduleRetry(run, yamlFromPayload, maxRetries);
          run.setStatus(BuildStatus.RETRYING);
          buildRepo.save(run);
        } else if(run!=null) {
          run.setStatus(BuildStatus.FAILED);
          run.setFailureType(ftype);
          run.setFailureReason(ex.getMessage());
          buildRepo.save(run);
        }
      } catch(Exception e){ e.printStackTrace(); if(run!=null){ run.setStatus(BuildStatus.FAILED); buildRepo.save(run); } }

      try{ if(workspace!=null) { java.nio.file.Files.walk(workspace).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); } } catch(Exception ignore){}
    } finally {
      try{ if(workspace!=null) { java.nio.file.Files.walk(workspace).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); } } catch(Exception ignore){}
      logStreamingService.closeStream(runIdFromPayload(payload));
    }
  }

  private String runIdFromPayload(String payload){ try{ Map m=new ObjectMapper().readValue(payload, Map.class); return (String)m.get("runId"); } catch(Exception e){ return ""; } }
}
