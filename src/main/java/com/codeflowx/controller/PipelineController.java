package com.codeflowx.controller;

import com.codeflowx.dto.CreatePipelineRequest;
import com.codeflowx.model.Pipeline;
import com.codeflowx.service.PipelineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
public class PipelineController {
  private final PipelineService pipelineService;
  public PipelineController(PipelineService pipelineService){this.pipelineService=pipelineService;}
  @PostMapping
  public ResponseEntity<Pipeline> create(@Valid @RequestBody CreatePipelineRequest req){
    Pipeline p = pipelineService.createPipeline(req);
    return ResponseEntity.ok(p);
  }
  @GetMapping
  public ResponseEntity<List<Pipeline>> list(){ return ResponseEntity.ok(pipelineService.listPipelines()); }
  @GetMapping("{id}")
  public ResponseEntity<Pipeline> get(@PathVariable Long id){ return ResponseEntity.of(pipelineService.getPipeline(id)); }
  @DeleteMapping("{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){ pipelineService.deletePipeline(id); return ResponseEntity.noContent().build(); }
}
