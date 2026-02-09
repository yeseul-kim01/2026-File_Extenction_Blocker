package dev.project.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())

                .authorizeHttpRequests(auth -> auth
                        // pages
                        .requestMatchers("/", "/policy").permitAll()

                        // static
                        .requestMatchers(
                                "/app.css",
                                "/policy.js",
                                "/favicon.ico",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        // health/version
                        .requestMatchers("/health", "/version").permitAll()

                        // swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/docs/**"
                        ).permitAll()

                        // tenant 발급 (확장 최초 실행)
                        .requestMatchers(HttpMethod.POST, "/api/tenants/issue").permitAll()

                        .requestMatchers("/api/**").permitAll()

                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
