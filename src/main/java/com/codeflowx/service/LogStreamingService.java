package com.codeflowx.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LogStreamingService {
  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  public SseEmitter subscribe(String runId){
    SseEmitter emitter = new SseEmitter(0L);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    return emitter;
  }
  public void appendAndEmit(String runId, String stageName, String line){
    for(SseEmitter e: emitters){
      try { e.send(line); } catch(IOException ex){ emitters.remove(e); }
    }
  }
}
