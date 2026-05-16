package com.codeflowx.repository;

import com.codeflowx.model.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArtifactRepository extends JpaRepository<Artifact, Long> {
  List<Artifact> findByBuildRunId(Long buildRunId);
}
