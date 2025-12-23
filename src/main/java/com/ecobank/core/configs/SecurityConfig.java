
// src/main/java/com/ecobank/core/configs/SecurityConfig.java
package com.ecobank.core.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest; // <-- add this

@Configuration
public class SecurityConfig {

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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/error")
                    .permitAll()
                .requestMatchers("/ui/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/ui", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(Customizer.withDefaults()); // keep CSRF for app

        return http.build();
    }
}
