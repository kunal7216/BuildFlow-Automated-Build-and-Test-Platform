package com.codeflowx.service;

import com.codeflowx.model.BuildRun;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
nimport com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DockerExecutionService {
  public interface LogCallback { void onLine(String line); }
  private final StringRedisTemplate redis;
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  public DockerExecutionService(StringRedisTemplate redis){ this.redis = redis; }

  public Path executeBuild(BuildRun run, String yamlContent, LogCallback cb) throws Exception {
    Path workspace = Files.createTempDirectory("codeflowx-"+run.getRunId());
    long timeoutSeconds = 300; // default
    try {n      if(yamlContent!=null && !yamlContent.isBlank()){
        try {          Map parsed = yamlMapper.readValue(yamlContent, Map.class);          Number t = (Number) parsed.getOrDefault("timeoutSeconds", timeoutSeconds);          timeoutSeconds = t.longValue();        } catch(Exception ignore){}
      }
n      long startMillis = System.currentTimeMillis();
      List<String> commands = List.of("echo Starting build for "+run.getRunId(),"sleep 1","echo Build done");
      for(String cmd : commands){        ProcessBuilder pb = new ProcessBuilder("sh","-c", cmd);        pb.directory(workspace.toFile());        pb.redirectErrorStream(true);        Process proc = pb.start();        BufferedReader r=new BufferedReader(new InputStreamReader(proc.getInputStream()));        String line;        while(true){          if((line=r.readLine())!=null){            cb.onLine(line);          } else {            // no more immediate lines; check if process exited or continue reading after short sleep            if(!proc.isAlive()) break;            // check cancellation            try { if(Boolean.TRUE.equals(redis.hasKey("codeflow:cancel:"+run.getRunId()))) { proc.destroyForcibly(); throw new RuntimeException("Build cancelled"); } } catch(Exception ignore){}            // check timeout            long elapsed = System.currentTimeMillis()-startMillis;            if(elapsed > timeoutSeconds*1000){ proc.destroyForcibly(); throw new RuntimeException("Build timed out"); }            Thread.sleep(200);            continue;          }        }        long remaining = timeoutSeconds*1000 - (System.currentTimeMillis()-startMillis);        if(remaining <= 0){ proc.destroyForcibly(); throw new RuntimeException("Stage timed out"); }        boolean exited = proc.waitFor(remaining, TimeUnit.MILLISECONDS);        if(!exited){ proc.destroyForcibly(); throw new RuntimeException("Stage timed out"); }        int exit = proc.exitValue();        if(exit!=0) throw new RuntimeException("Stage failed exit="+exit);      }      // return workspace for artifact collection; do not delete here      return workspace;    } catch(Exception ex){      try { Files.walk(workspace).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); } catch(Exception ignore){}      throw ex;    }  }}
