package com.codeflowx.service;

import com.codeflowx.model.BuildRun;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class DockerExecutionService {
  public interface LogCallback { void onLine(String line); }
  public void executeBuild(BuildRun run, String yamlContent, LogCallback cb) throws Exception {
    Path workspace = Files.createTempDirectory("codeflowx-"+run.getRunId());
    try {
      List<String> commands = List.of("echo Starting build for "+run.getRunId(),"sleep 1","echo Build done");
      for(String cmd : commands){
        ProcessBuilder pb = new ProcessBuilder("sh","-c", cmd);
        pb.directory(workspace.toFile());
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        BufferedReader r=new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while((line=r.readLine())!=null){ cb.onLine(line); }
        int exit = proc.waitFor();
        if(exit!=0) throw new RuntimeException("Stage failed exit="+exit);
      }
    } finally {
      try { Files.walk(workspace).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); } catch(Exception ignore){}
    }
  }
}
