package io.zhc1.realworld.config;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Profile("h2")
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = "io.zhc1.realworld.persistence",
    includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*JpaRepository")
)
class JpaConfiguration {}
