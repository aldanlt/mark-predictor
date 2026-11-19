package es.uvigo.markpredictor.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/** Crea la conexión a MySQL usando los datos de AppConfig. */
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        AppConfig config = AppConfig.getInstance();
        return DataSourceBuilder.create()
                .url(config.getDbUrl())
                .username(config.getDbUser())
                .password(config.getDbPassword())
                .build();
    }
}
