package com.bloomreach.webnotification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the web notification application.
 *
 * <p>Requires authentication for the WebSocket handshake at {@code /ws/notifications}
 * and exposes an in-memory {@link UserDetailsService} for local development.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security for REST and WebSocket handshake requests.
     *
     * @param http HTTP security builder
     * @return the configured security filter chain
     * @throws Exception if the security configuration cannot be applied
     */
    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/ws/notifications").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/notifications"));
        return http.build();
    }

    /**
     * Provides in-memory user credentials for authenticating WebSocket connections.
     *
     * @return an in-memory user details service
     */
    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("user-1")
                        .password("{noop}password")
                        .roles("USER")
                        .build()
        );
    }
}
