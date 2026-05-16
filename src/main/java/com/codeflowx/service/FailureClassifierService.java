package com.codeflowx.service;

import com.codeflowx.model.FailureType;
import org.springframework.stereotype.Service;

@Service
public class FailureClassifierService {
  public FailureType classify(Throwable t){
    String msg = t.getMessage()==null?"":t.getMessage().toLowerCase();
    if(t instanceof java.util.concurrent.TimeoutException || msg.contains("timeout")) return FailureType.TIMEOUT;
    if(msg.contains("test") || msg.contains("fail") ) return FailureType.TEST_FAILURE;
    if(msg.contains("could not resolve") || msg.contains("dependency")) return FailureType.DEPENDENCY_ERROR;
    if(msg.contains("compile") || msg.contains("compilation")) return FailureType.COMPILATION_ERROR;
    return FailureType.INFRA_FAILURE;
  }
}
