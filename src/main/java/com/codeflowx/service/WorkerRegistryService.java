package com.codeflowx.service;

import com.codeflowx.model.WorkerNode;
import com.codeflowx.repository.WorkerRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class WorkerRegistryService {
  private final WorkerRepository repo;
  private final StringRedisTemplate redis;
  public WorkerRegistryService(WorkerRepository repo, StringRedisTemplate redis){ this.repo=repo; this.redis=redis; }

  public WorkerNode registerHeartbeat(String workerId, String hostname, String status, String supportedLanguages, int activeJobs){
    WorkerNode node = repo.findByWorkerId(workerId).orElseGet(() -> { WorkerNode n=new WorkerNode(); n.setWorkerId(workerId); n.setCreatedAt(Instant.now()); return n; });
    node.setHostname(hostname); node.setStatus(status); node.setSupportedLanguages(supportedLanguages); node.setActiveJobs(activeJobs); node.setLastHeartbeatAt(Instant.now());
    node = repo.save(node);
    // cache in redis
    String key = "codeflow:worker:"+workerId;
    try {
      redis.opsForValue().set(key, String.format("%s|%s|%d", node.getStatus(), node.getSupportedLanguages(), node.getActiveJobs()), Duration.ofSeconds(90));
    } catch(Exception ignore){}
    return node;
  }
}
