package com.github.superz97.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.github.superz97.chat.repository")
@EnableMongoAuditing
public class MongoConfig {

    // Remove _class field from MongoDB documents
    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoTemplate mongoTemplate) {
        MappingMongoConverter converter = (MappingMongoConverter) mongoTemplate.getConverter();
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

}
