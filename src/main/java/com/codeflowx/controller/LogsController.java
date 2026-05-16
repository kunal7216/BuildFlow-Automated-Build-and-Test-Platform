package com.codeflowx.controller;

import com.codeflowx.service.LogStreamingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/builds")
public class LogsController {
  private final LogStreamingService logs;
  public LogsController(LogStreamingService logs){this.logs=logs;}
  @GetMapping("{runId}/logs/stream")
  public SseEmitter stream(@PathVariable String runId){ return logs.subscribe(runId); }
}
