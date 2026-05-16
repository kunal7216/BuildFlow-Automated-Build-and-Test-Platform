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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "codeflow", name = "worker-mode", havingValue = "true", matchIfMissing = false)
public class BuildWorkerConsumer {
  private final ObjectMapper om=new ObjectMapper();
  private final BuildRunRepository buildRepo;
  private final DockerExecutionService dockerService;
  private final LogStreamingServiceV2 logStreamingService;
  private final BuildStageRepository stageRepo;
  private final BuildLogRepository logRepo;

  public BuildWorkerConsumer(BuildRunRepository buildRepo, DockerExecutionService dockerService, LogStreamingServiceV2 logStreamingService, BuildStageRepository stageRepo, BuildLogRepository logRepo){
    this.buildRepo=buildRepo; this.dockerService=dockerService; this.logStreamingService=logStreamingService; this.stageRepo=stageRepo; this.logRepo=logRepo;
  }

  @KafkaListener(topics="codeflow.build.created", groupId="codeflow-worker-group")
  @Transactional
  public void onBuildCreated(String payload){
    try {
      Map map=om.readValue(payload, Map.class);
      String runId=(String)map.get("runId");
      BuildRun run=buildRepo.findByRunId(runId).orElseThrow();
      run.setStatus(BuildStatus.RUNNING);
      run.setStartedAt(Instant.now());
      buildRepo.save(run);
      String yaml=(String)map.get("yamlContent");
      Map parsed = new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory()).readValue(yaml, Map.class);
      List<Map<String,Object>> stages = (List<Map<String,Object>>)parsed.get("stages");
      for(Map<String,Object> s : stages){
        BuildStage bs=new BuildStage(); bs.setBuildRunId(run.getId()); bs.setStageName((String)s.get("name")); bs.setStatus(com.codeflowx.model.StageStatus.RUNNING); bs.setStartedAt(Instant.now());
        bs = stageRepo.save(bs);
        try {
          dockerService.executeBuild(run, List.of(s), line -> {
            BuildLog l = new BuildLog(); l.setBuildRunId(run.getId()); l.setStageId(bs.getId()); l.setLogLine(line); l.setStreamType("stdout");
            logRepo.save(l);
            logStreamingService.appendAndEmit(run.getRunId(), bs.getStageName(), line);
          }, ((Number)parsed.getOrDefault("timeoutSeconds",300)).longValue());
          bs.setStatus(com.codeflowx.model.StageStatus.SUCCESS);
        } catch(Exception ex){
          bs.setStatus(com.codeflowx.model.StageStatus.FAILED); bs.setFailureReason(ex.getMessage());
          run.setStatus(BuildStatus.FAILED); run.setFailureReason(ex.getMessage()); run.setFailureType(com.codeflowx.model.FailureType.UNKNOWN);
          bs.setCompletedAt(Instant.now()); bs.setDurationMs(Instant.now().toEpochMilli()-bs.getStartedAt().toEpochMilli());
          stageRepo.save(bs); buildRepo.save(run); return;
        }
        bs.setCompletedAt(Instant.now()); bs.setDurationMs(Instant.now().toEpochMilli()-bs.getStartedAt().toEpochMilli());
        stageRepo.save(bs);
      }
      run.setStatus(BuildStatus.SUCCESS);
      run.setCompletedAt(Instant.now()); run.setDurationMs(Instant.now().toEpochMilli()-run.getStartedAt().toEpochMilli());
      buildRepo.save(run);
      logStreamingService.emitEvent(run.getRunId(), "build.completed", "SUCCESS");
      logStreamingService.closeStream(run.getRunId());
    } catch(Exception ex){ ex.printStackTrace(); }
  }
}
