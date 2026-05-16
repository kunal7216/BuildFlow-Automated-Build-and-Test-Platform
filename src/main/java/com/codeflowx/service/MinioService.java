package com.codeflowx.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import io.minio.GetPresignedObjectUrlArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

@Service
public class MinioService {
  private final MinioClient client;
  @Value("${minio.bucket}") private String bucket;
  public MinioService(MinioClient client){ this.client = client; }

  public void upload(String objectKey, File file, String contentType) throws Exception{
    client.putObject(PutObjectArgs.builder().bucket(bucket).`object`(objectKey).contentType(contentType).stream("".getBytes(), file.length(), -1).build());
  }

  public String getPresignedUrl(String objectKey, Duration expiry) throws Exception{
    return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucket).`object`(objectKey).expiry((int)expiry.getSeconds()).build());
  }
}
