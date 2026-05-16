package com.codeflowx.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="build_runs")
public class BuildRun {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(unique=true) private String runId;
  private Long pipelineId;
  private String repoName;
  private String commitSha;
  private String branch;
  @Enumerated(EnumType.STRING) private BuildStatus status;
  @Enumerated(EnumType.STRING) private TriggerType triggerType;
  private Instant queuedAt;
  private Instant startedAt;
  private Instant completedAt;
  private Long durationMs;
  private Long queueWaitMs;
  @Enumerated(EnumType.STRING) private FailureType failureType;
  @Lob private String failureReason;
  private Integer retryCount = 0;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRunId() { return runId; }
  public void setRunId(String runId) { this.runId = runId; }
  public Long getPipelineId() { return pipelineId; }
  public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }
  public String getRepoName() { return repoName; }
  public void setRepoName(String repoName) { this.repoName = repoName; }
  public String getCommitSha() { return commitSha; }
  public void setCommitSha(String commitSha) { this.commitSha = commitSha; }
  public String getBranch() { return branch; }
  public void setBranch(String branch) { this.branch = branch; }
  public BuildStatus getStatus() { return status; }
  public void setStatus(BuildStatus status) { this.status = status; }
  public TriggerType getTriggerType() { return triggerType; }
  public void setTriggerType(TriggerType triggerType) { this.triggerType = triggerType; }
  public Instant getQueuedAt() { return queuedAt; }
  public void setQueuedAt(Instant queuedAt) { this.queuedAt = queuedAt; }
  public Instant getStartedAt() { return startedAt; }
  public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
  public Instant getCompletedAt() { return completedAt; }
  public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
  public Long getDurationMs() { return durationMs; }
  public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
  public Long getQueueWaitMs() { return queueWaitMs; }
  public void setQueueWaitMs(Long queueWaitMs) { this.queueWaitMs = queueWaitMs; }
  public FailureType getFailureType() { return failureType; }
  public void setFailureType(FailureType failureType) { this.failureType = failureType; }
  public String getFailureReason() { return failureReason; }
  public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
  public Integer getRetryCount() { return retryCount; }
  public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}
