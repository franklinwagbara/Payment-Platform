package com.walletplatform.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        // Railway may provide postgres:// or postgresql:// — normalize to postgresql://
        if (databaseUrl.startsWith("postgres://")) {
            databaseUrl = databaseUrl.replaceFirst("postgres://", "postgresql://");
        }

        try {
            URI dbUri = new URI(databaseUrl);
            String userInfo = dbUri.getUserInfo();

            if (userInfo == null || !userInfo.contains(":")) {
                throw new IllegalStateException("DATABASE_URL is missing user credentials");
            }

            String username = userInfo.split(":")[0];
            String password = userInfo.split(":")[1];
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            // Include query params (e.g., ?sslmode=require) if present
            if (dbUri.getQuery() != null) {
                jdbcUrl += "?" + dbUri.getQuery();
            }

            log.info("Connecting to database at {}:{}{}", dbUri.getHost(), dbUri.getPort(), dbUri.getPath());

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid DATABASE_URL format: " + e.getMessage(), e);
        }
    }
}
