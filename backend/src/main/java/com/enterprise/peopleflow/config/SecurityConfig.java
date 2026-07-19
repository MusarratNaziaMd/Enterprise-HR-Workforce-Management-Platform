package com.enterprise.peopleflow.config;

import com.enterprise.peopleflow.security.JwtAuthenticationFilter;
import com.enterprise.peopleflow.security.SecurityEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityEntryPoint securityEntryPoint;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(securityEntryPoint)
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    // ── Public endpoints ──
                    .requestMatchers(
                            "/auth/**",
                            "/public/**",
                            "/actuator/health",
                            "/actuator/info",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/swagger-resources/**"
                    ).permitAll()

                    // ── CORS preflight ──
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // ── Employee management ──
                    .requestMatchers(HttpMethod.POST, "/employees/**")
                            .hasAuthority("EMPLOYEE_CREATE")
                    .requestMatchers(HttpMethod.PUT, "/employees/**")
                            .hasAuthority("EMPLOYEE_UPDATE")
                    .requestMatchers(HttpMethod.DELETE, "/employees/**")
                            .hasAuthority("EMPLOYEE_DELETE")

                    // ── Attendance ──
                    .requestMatchers(HttpMethod.POST, "/attendance/**")
                            .hasAuthority("ATTENDANCE_MARK")
                    .requestMatchers(HttpMethod.PUT, "/attendance/**")
                            .hasAuthority("ATTENDANCE_UPDATE")

                    // ── Leaves: apply + approve ──
                    .requestMatchers(HttpMethod.POST, "/leaves/**")
                            .hasAuthority("LEAVE_APPLY")
                    .requestMatchers(HttpMethod.PUT, "/leaves/*/approve")
                            .hasAuthority("LEAVE_APPROVE")
                    .requestMatchers(HttpMethod.PUT, "/leaves/*/reject")
                            .hasAuthority("LEAVE_APPROVE")

                    // ── Salary management ──
                    .requestMatchers(HttpMethod.POST, "/salary/**")
                            .hasAuthority("SALARY_MANAGE")
                    .requestMatchers(HttpMethod.PUT, "/salary/**")
                            .hasAuthority("SALARY_MANAGE")

                    // ── Department management ──
                    .requestMatchers(HttpMethod.POST, "/departments/**")
                            .hasAuthority("DEPARTMENT_CREATE")
                    .requestMatchers(HttpMethod.PUT, "/departments/**")
                            .hasAuthority("DEPARTMENT_UPDATE")
                    .requestMatchers(HttpMethod.DELETE, "/departments/**")
                            .hasAuthority("DEPARTMENT_DELETE")

                    // ── Audit logs ──
                    .requestMatchers(HttpMethod.GET, "/audit/**")
                            .hasAuthority("AUDIT_READ")

                    // ── All remaining GET requests require authentication ──
                    .requestMatchers(HttpMethod.GET, "/**")
                            .authenticated()

                    // ── Catch-all ──
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
