package com.codeflowx.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.context.annotation.Bean;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
  @Bean
  public ProducerFactory<String,String> producerFactory(org.springframework.core.env.Environment env){
    Map<String,Object> props=new HashMap<>();
    String servers = env.getProperty("KAFKA_BOOTSTRAP_SERVERS", env.getProperty("kafka.bootstrap-servers","kafka:9092"));
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return new DefaultKafkaProducerFactory<>(props);
  }
  @Bean
  public KafkaTemplate<String,String> kafkaTemplate(ProducerFactory<String,String> pf){ return new KafkaTemplate<>(pf); }
}
