package com.codeflowx.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;

@Service
public class BuildQueueProducer {
  private final KafkaTemplate<String,String> kafka;
  private final ObjectMapper om = new ObjectMapper();
  public BuildQueueProducer(KafkaTemplate<String,String> kafka){this.kafka=kafka;}
  public void publishBuildCreated(String runId, Long pipelineId, String repoName, String commitSha, String branch, String yamlContent){
    try {
      HashMap<String,Object> ev=new HashMap<>();
      ev.put("runId",runId); ev.put("pipelineId",pipelineId); ev.put("repoName",repoName);
      ev.put("commitSha",commitSha); ev.put("branch",branch); ev.put("yamlContent",yamlContent);
      kafka.send("codeflow.build.created", runId, om.writeValueAsString(ev));
    } catch(Exception ex){ throw new RuntimeException(ex); }
  }
}
