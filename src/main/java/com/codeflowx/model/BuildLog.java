package com.codeflowx.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="build_logs")
public class BuildLog {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private Long buildRunId;
  private Long stageId;
  @Lob private String logLine;
  private String streamType;
  private Instant timestamp = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBuildRunId() { return buildRunId; }
  public void setBuildRunId(Long buildRunId) { this.buildRunId = buildRunId; }
  public Long getStageId() { return stageId; }
  public void setStageId(Long stageId) { this.stageId = stageId; }
  public String getLogLine() { return logLine; }
  public void setLogLine(String logLine) { this.logLine = logLine; }
  public String getStreamType() { return streamType; }
  public void setStreamType(String streamType) { this.streamType = streamType; }
  public Instant getTimestamp() { return timestamp; }
  public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
