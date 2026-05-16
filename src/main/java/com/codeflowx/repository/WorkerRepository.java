package com.codeflowx.repository;

import com.codeflowx.model.WorkerNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<WorkerNode, Long> {
  Optional<WorkerNode> findByWorkerId(String workerId);
}
