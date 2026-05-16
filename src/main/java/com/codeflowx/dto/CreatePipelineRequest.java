package com.codeflowx.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePipelineRequest {
  @NotBlank public String repoName;
  @NotBlank public String repoUrl;
  public String branch = "main";
  @NotBlank public String yamlContent;

  public String getRepoName() { return repoName; }
  public void setRepoName(String repoName) { this.repoName = repoName; }
  public String getRepoUrl() { return repoUrl; }
  public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
  public String getBranch() { return branch; }
  public void setBranch(String branch) { this.branch = branch; }
  public String getYamlContent() { return yamlContent; }
  public void setYamlContent(String yamlContent) { this.yamlContent = yamlContent; }
}
