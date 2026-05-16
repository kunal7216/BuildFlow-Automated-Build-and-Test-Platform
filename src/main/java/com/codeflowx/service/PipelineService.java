package com.codeflowx.service;

import com.codeflowx.dto.CreatePipelineRequest;
import com.codeflowx.model.Pipeline;
import com.codeflowx.repository.PipelineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PipelineService {
  private final PipelineRepository repo;
  private final PipelineParserService parser;
  public PipelineService(PipelineRepository repo, PipelineParserService parser){this.repo=repo;this.parser=parser;}
  public Pipeline createPipeline(CreatePipelineRequest req){
    parser.parseAndValidate(req.getYamlContent());
    Pipeline p=new Pipeline();
    p.setRepoName(req.getRepoName()); p.setRepoUrl(req.getRepoUrl()); p.setBranch(req.getBranch());
    p.setYamlContent(req.getYamlContent());
    p.setPipelineName("pipeline-"+System.currentTimeMillis());
    return repo.save(p);
  }
  public List<Pipeline> listPipelines(){ return repo.findAll(); }
  public Optional<Pipeline> getPipeline(Long id){ return repo.findById(id); }
  public void deletePipeline(Long id){ repo.deleteById(id); }
}
