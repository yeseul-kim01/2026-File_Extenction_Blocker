package dev.project.infra;

import dev.project.domain.connection.ConnectionCodeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectionBeansConfig {

    @Bean
    public ConnectionCodeRepository connectionCodeRepository() {
        return new InMemoryConnectionCodeRepository();
    }
}
