
// src/main/java/com/ecobank/core/configs/SecurityConfig.java
package com.ecobank.core.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest; // <-- add this

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain h2ConsoleSecurity(HttpSecurity http) throws Exception {
        http
            // Use Boot's matcher for H2 console (less error-prone than a raw string)
            .securityMatcher(PathRequest.toH2Console())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())                    // H2 console issues POSTs without CSRF
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // disable frames for safety

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
    http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    // CSRF: disable ONLY for API
    .csrf(csrf -> csrf
        .ignoringRequestMatchers("/api/**")
    )
    .authorizeHttpRequests(auth -> auth
    .requestMatchers("/", "/login", "/login/oauth2/**", "/css/**", "/js/**",
    "/images/**", "/error")
    .permitAll()
    .requestMatchers("/api/auth/logout").permitAll()
    .requestMatchers("/ui/**").authenticated()
    .requestMatchers("/api/**").authenticated() // Change BY SHUBHAM
    .anyRequest().permitAll()
    )
    .oauth2Login(oauth -> oauth
    .loginPage("/login")
    .defaultSuccessUrl("http://localhost:4200", true)
    // .defaultSuccessUrl("/ui", true)
    )
    .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) ->
                        res.setStatus(HttpServletResponse.SC_OK))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

    
    // .csrf(Customizer.withDefaults()); // keep CSRF for app
    return http.build();
    }

}
