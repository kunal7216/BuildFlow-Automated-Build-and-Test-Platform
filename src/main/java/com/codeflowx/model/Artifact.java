package com.codeflowx.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="artifacts")
public class Artifact {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private Long buildRunId;
  private String fileName;
  private String objectKey;
  private Long sizeBytes;
  private String contentType;
  private String checksum;
  private Instant createdAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBuildRunId() { return buildRunId; }
  public void setBuildRunId(Long buildRunId) { this.buildRunId = buildRunId; }
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }
  public String getObjectKey() { return objectKey; }
  public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
  public Long getSizeBytes() { return sizeBytes; }
  public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
  public String getContentType() { return contentType; }
  public void setContentType(String contentType) { this.contentType = contentType; }
  public String getChecksum() { return checksum; }
  public void setChecksum(String checksum) { this.checksum = checksum; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
