package com.codeflowx.service;

import com.codeflowx.model.Artifact;
import com.codeflowx.repository.ArtifactRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtifactService {
  private final ArtifactRepository repo;
  private final MinioService minio;
  public ArtifactService(ArtifactRepository repo, MinioService minio){ this.repo=repo; this.minio=minio; }

  public List<Artifact> uploadArtifactsFromWorkspace(Path workspace, List<String> patterns, String runId) throws Exception{
    List<Artifact> results = new ArrayList<>();
    if(patterns==null || patterns.isEmpty()) return results;
    for(String pattern: patterns){
      // naive glob search
      List<Path> matched = Files.walk(workspace, FileVisitOption.FOLLOW_LINKS)
        .filter(p -> p.toFile().isFile())
        .filter(p -> p.getFileName().toString().matches(globToRegex(pattern)))
        .collect(Collectors.toList());
      for(Path p: matched){
        String fileName = p.getFileName().toString();
        String objectKey = "builds/"+runId+"/artifacts/"+fileName;
        File f = p.toFile();
        minio.upload(objectKey, f, Files.probeContentType(p));
        Artifact a = new Artifact(); a.setBuildRunId(Long.valueOf(runId)); a.setFileName(fileName); a.setObjectKey(objectKey); a.setSizeBytes(f.length()); a.setContentType(Files.probeContentType(p)); a.setChecksum("");
        repo.save(a);
        results.add(a);
      }
    }
    return results;
  }

  private String globToRegex(String glob){
    // very simple: translate '*' to '.*'
    return glob.replace("*",".*");
  }
}
