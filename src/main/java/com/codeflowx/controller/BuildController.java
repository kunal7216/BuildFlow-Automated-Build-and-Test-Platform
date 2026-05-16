package com.codeflowx.controller;

import com.codeflowx.model.BuildRun;
import com.codeflowx.repository.BuildRunRepository;
import com.codeflowx.service.BuildOrchestrationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/builds")
public class BuildController {
  private final BuildRunRepository buildRepo;
  private final BuildOrchestrationService orchestrator;
  public BuildController(BuildRunRepository buildRepo, BuildOrchestrationService orchestrator){this.buildRepo=buildRepo;this.orchestrator=orchestrator;}
  @GetMapping
  public ResponseEntity<List<BuildRun>> list(){ return ResponseEntity.ok(buildRepo.findAll()); }
  @GetMapping("{runId}")
  public ResponseEntity<BuildRun> get(@PathVariable String runId){ return ResponseEntity.of(buildRepo.findByRunId(runId)); }
}
