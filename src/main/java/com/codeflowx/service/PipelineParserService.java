package com.codeflowx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PipelineParserService {
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  public Map<String,Object> parseAndValidate(String yaml) {
    try {
      Map<String,Object> map = yamlMapper.readValue(yaml, Map.class);
      if(!map.containsKey("stages")) throw new IllegalArgumentException("stages missing");
      return map;
    } catch(Exception ex){ throw new IllegalArgumentException("Invalid YAML: "+ex.getMessage(), ex); }
  }
}
