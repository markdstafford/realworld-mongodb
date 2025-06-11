package io.zhc1.realworld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Profile("mongodb")
@Configuration
@EnableMongoRepositories(
    basePackages = "io.zhc1.realworld.persistence",
    includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*MongoRepository")
)
class MongoConfiguration {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
