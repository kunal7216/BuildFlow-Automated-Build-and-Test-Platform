package com.codeflowx.service;

import com.codeflowx.model.BuildRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class DockerExecutionService {
  public interface LogCallback { void onLine(String line); }

  @Value("${codeflow.docker.memoryLimit:512m}")
  private String memoryLimit;

  @Value("${codeflow.docker.cpuLimit:1.0}")
  private String cpuLimit;

  private final List<String> allowlist = List.of("mvn test","mvn package -DskipTests","pytest");

  public void executeBuild(BuildRun run, List<Map<String,Object>> stages, LogCallback cb, long stageTimeoutSeconds) throws Exception {
    Path workspace = Files.createTempDirectory("codeflowx-"+run.getRunId());
    try {
      // For each stage, run container
      for(Map<String,Object> stage: stages){
        String stageName = (String)stage.get("name");
        String image = (String)stage.getOrDefault("image","alpine:latest");
        List<String> commands = (List<String>)stage.getOrDefault("commands", Collections.emptyList());
        String joined = String.join(" && ", commands);
        // validate commands against allowlist
        for(String c: commands){
          boolean ok = allowlist.stream().anyMatch(a -> c.trim().startsWith(a));
          if(!ok) throw new IllegalArgumentException("Command not allowed: "+c);
        }
        runContainer(workspace, image, joined, cb, stageTimeoutSeconds);
      }
    } finally {
      try { Files.walk(workspace).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); } catch(Exception ignore){}
    }
  }

  private void runContainer(Path workspace, String image, String cmd, LogCallback cb, long timeoutSeconds) throws Exception {
    List<String> dockerCmd = new ArrayList<>();
    dockerCmd.addAll(Arrays.asList("docker","run","--rm","--network","none","-v", workspace.toAbsolutePath()+":/workspace","-w","/workspace","--cpus", cpuLimit,"--memory", memoryLimit, image,"sh","-c", cmd));
    ProcessBuilder pb = new ProcessBuilder(dockerCmd);
    pb.redirectErrorStream(true);
    Process proc = pb.start();
    ExecutorService ex = Executors.newSingleThreadExecutor();
    Future<?> reader = ex.submit(() -> {
      try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
        String line;
        while((line=r.readLine())!=null){ cb.onLine(line); }
      } catch(IOException ignore){}
    });
    boolean finished = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS);
    if(!finished){ proc.destroyForcibly(); throw new TimeoutException("Stage exceeded timeout " + timeoutSeconds + "s"); }
    reader.get(5, TimeUnit.SECONDS);
    ex.shutdownNow();
    int exit = proc.exitValue();
    if(exit!=0) throw new RuntimeException("Container exit="+exit);
  }
}
