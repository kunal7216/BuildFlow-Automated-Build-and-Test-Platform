package com.codeflowx.repository;

import com.codeflowx.model.BuildLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildLogRepository extends JpaRepository<BuildLog, Long> {}
