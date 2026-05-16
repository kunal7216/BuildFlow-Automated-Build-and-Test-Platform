package com.codeflowx.repository;

import com.codeflowx.model.BuildRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BuildRunRepository extends JpaRepository<BuildRun, Long> {
  Optional<BuildRun> findByRunId(String runId);
}
