package com.codeflowx.controller;

import com.codeflowx.dto.WorkerHeartbeatRequest;
import com.codeflowx.service.WorkerRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {
  private final WorkerRegistryService registry;
  public WorkerController(WorkerRegistryService registry){ this.registry=registry; }

  @PostMapping("/{workerId}/heartbeat")
  public ResponseEntity<String> heartbeat(@PathVariable String workerId, @RequestBody WorkerHeartbeatRequest req){
    registry.registerHeartbeat(workerId, req.getHostname(), req.getStatus(), req.getSupportedLanguages(), req.getActiveJobs());
    return ResponseEntity.ok("ok");
  }
}
