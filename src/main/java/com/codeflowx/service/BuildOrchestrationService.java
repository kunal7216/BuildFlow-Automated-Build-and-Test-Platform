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
  private final java.util.concurrent.ScheduledExecutorService scheduler;
  public BuildOrchestrationService(BuildRunRepository buildRepo, BuildQueueProducer producer, java.util.concurrent.ScheduledExecutorService scheduler){this.buildRepo=buildRepo;this.producer=producer;this.scheduler=scheduler;}
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

  public void scheduleRetry(BuildRun run, String yamlContent, int maxRetries){
    int nextRetry = run.getRetryCount()==null?1:run.getRetryCount()+1;
    if(nextRetry>maxRetries) return;
    long delaySeconds = (long)(Math.pow(2, nextRetry-1) * 5); // exponential backoff base 5s
    scheduler.schedule(() -> {
      try {
        run.setRetryCount(nextRetry);
        run.setStatus(BuildStatus.QUEUED);
        buildRepo.save(run);
        producer.publishBuildCreated(run.getRunId(), run.getPipelineId(), run.getRepoName(), run.getCommitSha(), run.getBranch(), yamlContent);
      } catch(Exception ex){ ex.printStackTrace(); }
    }, delaySeconds, java.util.concurrent.TimeUnit.SECONDS);
  }
}

