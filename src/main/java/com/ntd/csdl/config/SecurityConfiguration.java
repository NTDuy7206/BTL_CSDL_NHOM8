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
                        .requestMatchers(HttpMethod.GET,"/api/landlords","/api/landlords/rooms").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/queries/employees/**", "/api/queries/rooms/**", "/api/queries/incidents/**", "/api/queries/violations/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/demo/my-rental").hasRole("USER")
                        .requestMatchers("/", "/index.html", "/login", "/register", "/assets/**", "/favicon.ico", "/favicon.svg").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employees/**", "/api/rooms/**", "/api/incidents/**", "/api/rules/**", "/api/violations/**", "/api/contracts", "/api/contracts/*", "/api/invoices", "/api/invoices/*", "/api/contract-requests", "/api/payment-reports", "/api/rentals/me", "/api/profile").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/incidents", "/api/profile/avatar").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/contract-requests", "/api/payment-reports").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/profile").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(errors -> errors
                    .authenticationEntryPoint((request, response, ex) -> {
                        response.setStatus(401);
                        response.setContentType("text/plain;charset=UTF-8");
                        response.getWriter().write("Vui lòng đăng nhập để tiếp tục");
                    })
                    .accessDeniedHandler((request, response, ex) -> {
                        response.setStatus(403);
                        response.setContentType("text/plain;charset=UTF-8");
                        response.getWriter().write("Tài khoản không có quyền thực hiện thao tác này");
                    }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
