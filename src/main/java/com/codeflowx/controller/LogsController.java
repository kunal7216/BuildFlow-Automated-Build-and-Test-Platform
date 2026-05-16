package com.codeflowx.controller;

import com.codeflowx.service.LogStreamingServiceV2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/builds")
public class LogsController {
  private final LogStreamingServiceV2 logs;
  public LogsController(LogStreamingServiceV2 logs){this.logs=logs;}
  @GetMapping("{runId}/logs/stream")
  public SseEmitter stream(@PathVariable String runId){ return logs.subscribe(runId); }
}
