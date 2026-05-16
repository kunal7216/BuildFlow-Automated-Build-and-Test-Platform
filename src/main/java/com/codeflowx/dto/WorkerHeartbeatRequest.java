package com.codeflowx.dto;

public class WorkerHeartbeatRequest {
  public String hostname;
  public String status;
  public String supportedLanguages;
  public int activeJobs;

  public String getHostname() { return hostname; }
  public void setHostname(String hostname) { this.hostname = hostname; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getSupportedLanguages() { return supportedLanguages; }
  public void setSupportedLanguages(String supportedLanguages) { this.supportedLanguages = supportedLanguages; }
  public int getActiveJobs() { return activeJobs; }
  public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
}
