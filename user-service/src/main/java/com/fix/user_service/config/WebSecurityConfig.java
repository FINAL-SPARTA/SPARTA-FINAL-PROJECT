package com.fix.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/**")
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/api/v1/auth/sign-in", "/api/v1/users/sign-up", "/api/v1/auth/user-info/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf((csrf) -> csrf.disable())
            .formLogin((form) -> form.disable())
            .httpBasic((basic) -> basic.disable())
            .sessionManagement((session) ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
    }

}
