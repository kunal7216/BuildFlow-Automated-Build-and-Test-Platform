package com.codeflowx.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="build_stages")
public class BuildStage {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private Long buildRunId;
  private String stageName;
  @Enumerated(EnumType.STRING) private StageStatus status;
  private Instant startedAt;
  private Instant completedAt;
  private Long durationMs;
  private Integer exitCode;
  @Lob private String failureReason;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBuildRunId() { return buildRunId; }
  public void setBuildRunId(Long buildRunId) { this.buildRunId = buildRunId; }
  public String getStageName() { return stageName; }
  public void setStageName(String stageName) { this.stageName = stageName; }
  public StageStatus getStatus() { return status; }
  public void setStatus(StageStatus status) { this.status = status; }
  public Instant getStartedAt() { return startedAt; }
  public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
  public Instant getCompletedAt() { return completedAt; }
  public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
  public Long getDurationMs() { return durationMs; }
  public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
  public Integer getExitCode() { return exitCode; }
  public void setExitCode(Integer exitCode) { this.exitCode = exitCode; }
  public String getFailureReason() { return failureReason; }
  public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
