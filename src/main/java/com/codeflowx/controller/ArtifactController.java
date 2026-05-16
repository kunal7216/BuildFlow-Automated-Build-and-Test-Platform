package com.codeflowx.controller;

import com.codeflowx.repository.ArtifactRepository;
import com.codeflowx.model.Artifact;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/artifacts")
public class ArtifactController {
  private final ArtifactRepository repo;
  public ArtifactController(ArtifactRepository repo){ this.repo=repo; }

  @GetMapping("/build/{runId}")
  public ResponseEntity<List<Artifact>> listForRun(@PathVariable Long runId){ return ResponseEntity.ok(repo.findByBuildRunId(runId)); }
}
