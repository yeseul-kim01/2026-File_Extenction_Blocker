package dev.project;

import dev.project.domain.connection.ConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ConnectionProperties.class)
@SpringBootApplication
public class
FileExtensionBlockApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileExtensionBlockApplication.class, args);
    }
}
