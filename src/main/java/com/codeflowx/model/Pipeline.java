package com.codeflowx.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="pipelines")
public class Pipeline {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String repoName;
  private String repoUrl;
  private String branch;
  private String pipelineName;
  @Lob private String yamlContent;
  private String language;
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRepoName() { return repoName; }
  public void setRepoName(String repoName) { this.repoName = repoName; }
  public String getRepoUrl() { return repoUrl; }
  public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
  public String getBranch() { return branch; }
  public void setBranch(String branch) { this.branch = branch; }
  public String getPipelineName() { return pipelineName; }
  public void setPipelineName(String pipelineName) { this.pipelineName = pipelineName; }
  public String getYamlContent() { return yamlContent; }
  public void setYamlContent(String yamlContent) { this.yamlContent = yamlContent; }
  public String getLanguage() { return language; }
  public void setLanguage(String language) { this.language = language; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
