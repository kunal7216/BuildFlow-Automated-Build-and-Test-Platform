package com.codeflowx.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "worker_nodes")
public class WorkerNode {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private String workerId;
  private String hostname;
  private String status;
  private String supportedLanguages;
  private Integer activeJobs;
  private Instant lastHeartbeatAt = Instant.now();
  private Instant createdAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getWorkerId() { return workerId; }
  public void setWorkerId(String workerId) { this.workerId = workerId; }
  public String getHostname() { return hostname; }
  public void setHostname(String hostname) { this.hostname = hostname; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getSupportedLanguages() { return supportedLanguages; }
  public void setSupportedLanguages(String supportedLanguages) { this.supportedLanguages = supportedLanguages; }
  public Integer getActiveJobs() { return activeJobs; }
  public void setActiveJobs(Integer activeJobs) { this.activeJobs = activeJobs; }
  public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
  public void setLastHeartbeatAt(Instant lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
