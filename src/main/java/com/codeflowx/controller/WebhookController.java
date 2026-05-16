package com.codeflowx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {
  private final com.codeflowx.service.PipelineService pipelineService;
  private final com.codeflowx.service.BuildOrchestrationService orchestrator;
  private final org.springframework.data.redis.core.StringRedisTemplate redis;
  public WebhookController(com.codeflowx.service.PipelineService pipelineService, com.codeflowx.service.BuildOrchestrationService orchestrator, org.springframework.data.redis.core.StringRedisTemplate redis){
    this.pipelineService=pipelineService; this.orchestrator=orchestrator; this.redis = redis;
  }
  @PostMapping("/github")
  public ResponseEntity<String> github(@RequestBody Map<String,Object> payload){
    Map repo = (Map)payload.get("repository");
    if(repo==null) return ResponseEntity.badRequest().body("missing repository");
    String repoName = (String)repo.get("full_name");
    String commit = "";
    try { commit = ((Map)((Map)payload.get("head_commit"))).get("id").toString(); } catch(Exception e){ commit = ""; }
    pipelineService.listPipelines().stream().filter(p->repoName.equals(p.getRepoName())).findFirst().ifPresent(p->{
      String key = String.format("codeflow:idempotency:%s:%s:%s", p.getRepoName(), commit, p.getPipelineName());
      Boolean created = redis.opsForValue().setIfAbsent(key, "1", java.time.Duration.ofHours(1));
      if(Boolean.TRUE.equals(created)){
        orchestrator.createBuild(p.getId(), p.getRepoName(), commit, p.getBranch(), com.codeflowx.model.TriggerType.GITHUB_WEBHOOK, p.getYamlContent());
      }
    });
    return ResponseEntity.ok("ok");
  }
}
