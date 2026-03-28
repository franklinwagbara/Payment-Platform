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
        // Debug: print all database-related env vars
        System.out.println("=== DATABASE ENV VAR DEBUG ===");
        System.out.println("DATABASE_URL=" + maskPassword(System.getenv("DATABASE_URL")));
        System.out.println("DATABASE_PUBLIC_URL=" + maskPassword(System.getenv("DATABASE_PUBLIC_URL")));
        System.out.println("DATABASE_PRIVATE_URL=" + maskPassword(System.getenv("DATABASE_PRIVATE_URL")));
        System.out.println("PGHOST=" + System.getenv("PGHOST"));
        System.out.println("PGPORT=" + System.getenv("PGPORT"));
        System.out.println("PGDATABASE=" + System.getenv("PGDATABASE"));
        System.out.println("PGUSER=" + System.getenv("PGUSER"));
        System.out.println("PGPASSWORD=" + (System.getenv("PGPASSWORD") != null ? "***SET***" : "null"));
        System.out.println("SPRING_DATASOURCE_URL=" + maskPassword(System.getenv("SPRING_DATASOURCE_URL")));
        System.out.println("=== END DEBUG ===");

        // Try DATABASE_URL first (standard Railway reference variable)
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = System.getenv("DATABASE_PUBLIC_URL");
        }
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = System.getenv("DATABASE_PRIVATE_URL");
        }

        HikariConfig config = new HikariConfig();

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Handle both postgresql:// and jdbc:postgresql:// formats
                String urlToParse = databaseUrl;
                if (urlToParse.startsWith("jdbc:")) {
                    urlToParse = urlToParse.substring(5);
                }

                URI uri = new URI(urlToParse);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String userInfo = uri.getUserInfo();
                String query = uri.getQuery();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (query != null && !query.isEmpty()) {
                    jdbcUrl += "?" + query;
                }

                String username = userInfo != null ? userInfo.split(":")[0] : "postgres";
                String password = (userInfo != null && userInfo.contains(":")) ? userInfo.split(":", 2)[1] : "";

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);

                System.out.println("DATABASE_URL parsed successfully. Connecting to: " + host + ":" + port + path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl + " - " + e.getMessage(), e);
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

            System.out.println("WARNING: No DATABASE_URL found! Using individual PG vars. Connecting to: " + host + ":" + port + "/" + database);
        }

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setInitializationFailTimeout(-1); // Don't fail on startup, retry later

        return new HikariDataSource(config);
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private String maskPassword(String url) {
        if (url == null) return "null";
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:***@");
    }
}
