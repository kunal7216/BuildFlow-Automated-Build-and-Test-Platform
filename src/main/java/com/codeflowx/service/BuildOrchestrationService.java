package com.codeflowx.service;

import com.codeflowx.model.BuildRun;
import com.codeflowx.model.BuildStatus;
import com.codeflowx.model.TriggerType;
import com.codeflowx.repository.BuildRunRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class BuildOrchestrationService {
  private final BuildRunRepository buildRepo;
  private final BuildQueueProducer producer;
  public BuildOrchestrationService(BuildRunRepository buildRepo, BuildQueueProducer producer){this.buildRepo=buildRepo;this.producer=producer;}
  public BuildRun createBuild(Long pipelineId, String repoName, String commitSha, String branch, TriggerType triggerType, String yamlContent){
    BuildRun run=new BuildRun();
    run.setRunId(UUID.randomUUID().toString());
    run.setPipelineId(pipelineId);
    run.setRepoName(repoName);
    run.setCommitSha(commitSha);
    run.setBranch(branch);
    run.setTriggerType(triggerType);
    run.setStatus(BuildStatus.QUEUED);
    run.setQueuedAt(Instant.now());
    run = buildRepo.save(run);
    producer.publishBuildCreated(run.getRunId(), pipelineId, repoName, commitSha, branch, yamlContent);
    return run;
  }

  // helper to trigger build from PipelineController
  public BuildRun triggerBuild(com.codeflowx.model.Pipeline pipeline, String branch, String commitSha, String triggerSource){
    String repoName = pipeline.getRepoName();
    TriggerType tt = "manual".equalsIgnoreCase(triggerSource) ? TriggerType.MANUAL : TriggerType.GITHUB_WEBHOOK;
    String yaml = pipeline.getYamlContent();
    return createBuild(pipeline.getId(), repoName, commitSha==null?"":commitSha, branch==null?pipeline.getBranch():branch, tt, yaml);
  }
}
