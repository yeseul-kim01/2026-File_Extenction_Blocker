package dev.project.web.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {

    @Value("${app.git.sha:dev}")
    private String sha;

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of("gitSha", sha);
    }
}
