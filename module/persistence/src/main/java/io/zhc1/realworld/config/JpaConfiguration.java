package io.zhc1.realworld.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Profile("h2")
@Configuration
@EnableJpaAuditing
class JpaConfiguration {}
