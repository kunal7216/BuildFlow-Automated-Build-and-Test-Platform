package com.codeflowx.controller;

import com.codeflowx.model.BuildRun;
import com.codeflowx.repository.BuildRunRepository;
import com.codeflowx.service.BuildOrchestrationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/builds")
public class BuildController {
  private final BuildRunRepository buildRepo;
  private final BuildOrchestrationService orchestrator;
  private final StringRedisTemplate redis;
  public BuildController(BuildRunRepository buildRepo, BuildOrchestrationService orchestrator, StringRedisTemplate redis){this.buildRepo=buildRepo;this.orchestrator=orchestrator;this.redis=redis;}
  @GetMapping
  public ResponseEntity<List<BuildRun>> list(){ return ResponseEntity.ok(buildRepo.findAll()); }
  @GetMapping("{runId}")
  public ResponseEntity<BuildRun> get(@PathVariable String runId){ return ResponseEntity.of(buildRepo.findByRunId(runId)); }

  @PostMapping("{runId}/cancel")
  public ResponseEntity<String> cancel(@PathVariable String runId){
    buildRepo.findByRunId(runId).ifPresent(r -> {
      // set redis cancel flag (TTL 1h)
      try{ redis.opsForValue().set("codeflow:cancel:"+runId, "1", Duration.ofHours(1)); } catch(Exception ignore){}
      r.setStatus(com.codeflowx.model.BuildStatus.CANCELLED);
      buildRepo.save(r);
    });
    return ResponseEntity.ok("cancellation requested");
  }
}
