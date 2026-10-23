package com.ntd.csdl.config;

import com.ntd.csdl.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/index.html", "/login", "/register", "/assets/**", "/favicon.ico", "/favicon.svg").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contract-requests").hasAnyRole("TENANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/contract-requests").hasAnyRole("TENANT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/contract-requests/*/status").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payment-reports").hasAnyRole("TENANT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payment-reports").hasRole("TENANT")
                        .requestMatchers(HttpMethod.PATCH, "/api/payment-reports/*/status").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/profile").hasAnyRole("USER", "OWNER_PENDING", "TENANT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/profile/avatar").hasAnyRole("USER", "OWNER_PENDING", "TENANT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/profile").hasAnyRole("USER", "OWNER_PENDING", "TENANT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "TENANT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("OWNER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
