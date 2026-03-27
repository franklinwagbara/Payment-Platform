package com.walletplatform.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        HikariConfig config = new HikariConfig();

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Railway provides DATABASE_URL in format: postgresql://user:pass@host:port/dbname
            try {
                URI uri = new URI(databaseUrl);
                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath(); // e.g. /railway
                String userInfo = uri.getUserInfo(); // e.g. user:pass
                String query = uri.getQuery(); // e.g. sslmode=require

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (query != null && !query.isEmpty()) {
                    jdbcUrl += "?" + query;
                }

                String username = userInfo.split(":")[0];
                String password = userInfo.split(":")[1];

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);

                System.out.println("DATABASE_URL parsed successfully. Connecting to: " + host + ":" + port + path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse DATABASE_URL: " + e.getMessage(), e);
            }
        } else {
            // Fallback to individual environment variables
            String host = env("PGHOST", "localhost");
            String port = env("PGPORT", "5432");
            String database = env("PGDATABASE", "railway");
            String username = env("PGUSER", "postgres");
            String password = env("PGPASSWORD", "");

            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);

            System.out.println("Using individual PG vars. Connecting to: " + host + ":" + port + "/" + database);
        }

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);

        return new HikariDataSource(config);
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
