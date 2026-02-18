package com.pickleball.config;

import com.pickleball.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers("/api/v1/auth/**","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/partners/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/courts/*/slots/**").permitAll()
                // 사업주 전용 엔드포인트
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/partner-manage/**").hasRole("PARTNER")
//                .requestMatchers(
//                            "/v3/api-docs/**",
//                            "/swagger-ui/**",
//                            "swagger-ui.html"
//                ).permitAll()
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(this.userDetailsService);
//        provider.setUserDetailsService(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use patterns so ephemeral dev domains (e.g. ngrok) work.
        // With allowCredentials=true, Spring will echo back the request Origin
        // when it matches an allowedOriginPattern.
        String customOriginPatterns = System.getenv("APP_CORS_ALLOWED_ORIGIN_PATTERNS");
        if (customOriginPatterns != null && !customOriginPatterns.isBlank()) {
            configuration.setAllowedOriginPatterns(Arrays.asList(customOriginPatterns.split("\\s*,\\s*")));
        } else {
            configuration.setAllowedOriginPatterns(List.of(
                "https://localhost",
                "https://localhost:*",
                "https://127.0.0.1:*",
                "http://localhost:*",
                "http://localhost",
                "http://127.0.0.1:*",
                "https://*.railway.app",
                "https://*.up.railway.app",
                "https://*.vercel.app",
                "https://vercel.app",
                "https://*.ngrok-free.dev",
                "http://*.ngrok-free.dev",
                "https://*.ngrok-free.app",
                "http://*.ngrok-free.app",
                "https://*.ngrok.io",
                "http://*.ngrok.io",
                "https://*.ngrok.app",
                "http://*.ngrok.app"
            ));
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
