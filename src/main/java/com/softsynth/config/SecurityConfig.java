package com.softsynth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

//    used for passwords authentication
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/verify-otp").permitAll()
                        .requestMatchers("/api/auth/refresh-token").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Profile endpoints - accessible to authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/admin/profile").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/profile").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/change-password").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/managers/profile").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/managers/profile").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/managers/change-password").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/employees/profile").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/employees/profile").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/employees/change-password").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                        // Admin-specific management endpoints
                        .requestMatchers(HttpMethod.POST, "/api/admin/managers/initiate-registration").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/employees/initiate-registration").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/managers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/employees").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/users").hasRole("ADMIN")
                        .requestMatchers("/api/admin/users/{userId}/toggle-status").hasRole("ADMIN") // Fixed pattern
                        .requestMatchers("/api/admin/users/{userId}").hasRole("ADMIN") // Fixed pattern for GET, PUT, DELETE
                        .requestMatchers("/api/admin/managers/{managerId}/employees").hasRole("ADMIN") // Fixed pattern

                        // Manager team management
                        .requestMatchers(HttpMethod.GET, "/api/managers/team").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/managers/team/{employeeId}").hasAnyRole("MANAGER", "ADMIN") // Fixed pattern

                        // Employee manager info
                        .requestMatchers(HttpMethod.GET, "/api/employees/manager").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/employees/team-mates").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                        // Password change endpoints for authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/auth/change-password").authenticated()

                        // Default - all other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Insufficient permissions\"}");
                        })
                );

        return http.build();
    }
}