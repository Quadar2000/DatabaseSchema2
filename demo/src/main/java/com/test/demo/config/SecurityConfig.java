package com.test.demo.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.test.demo.services.customUserDetailsService.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

            //.csrf(csrf -> csrf.disable())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Przechowywanie tokenu CSRF w ciasteczkach
                .ignoringRequestMatchers("/api/validate-token")
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                // .requireCsrfProtectionMatcher(request -> 
                //     "POST".equals(request.getMethod()) // Ograniczenie CSRF do metod POST
                // )
                .requireCsrfProtectionMatcher(new RequestMatcher() { // Niestandardowy RequestMatcher
                    @Override
                    public boolean matches(HttpServletRequest request) {
                        // Sprawdź, czy metoda to POST lub DELETE
                        String method = request.getMethod();
                        return "POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
                    }
                })
            )
            .cors(cors -> {
                cors.configurationSource(corsConfigurationSource());
            })

            .anonymous(anonymous-> anonymous.disable())
            .authorizeHttpRequests(authz -> authz
                            .requestMatchers( "/api/auth/login","/api/auth/csrf-token","/api/auth/session-info").permitAll()
                            .requestMatchers( "/api/delete-permission","/api/delete-user","/api/get-users",
                            "/api/grant-permission","/api/register-user","/api/generate-token","/api/validate-token").hasRole("ADMIN")
                            .anyRequest().authenticated()
                            
                            
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(2) 
                .maxSessionsPreventsLogin(false)
            )
            .authenticationManager(authenticationManager(userDetailsService()))
            .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"You need to log in\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"You don't have permission to access this resource\"}");
            }))
            
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")  // URL, pod który frontend wysyła żądanie
                .invalidateHttpSession(true)  // Unieważnia sesję
                .clearAuthentication(true)  // Usuwa dane autoryzacyjne
                .deleteCookies("JSESSIONID")  // Usuwa ciasteczko sesji
                // .logoutSuccessHandler((request, response, authentication) -> {
                //     response.setStatus(HttpServletResponse.SC_OK);
                //     response.getWriter().write("Logged out successfully");
                // })
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) throws Exception {
        //return new ProviderManager(Arrays.asList(new DaoAuthenticationProvider()));
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(Arrays.asList(authProvider));
    }

    // @Bean
    // public UserDetailsService userDetailsService(){
    //     UserDetails user = User.withUsername("user")
    //     .password(passwordEncoder().encode("user"))
    //     .roles("USER")
    //     .build();

    //     UserDetails admin = User.withUsername("admin")
    //     .password(passwordEncoder().encode("admin"))
    //     .roles("ADMIN")
    //     .build();

    //     return new InMemoryUserDetailsManager(user,admin);
    // }

    @Bean
    public UserDetailsService userDetailsService(){
        return userDetailsService;
    }


    @Bean 
    public PasswordEncoder passwordEncoder() { 
      return new BCryptPasswordEncoder(); 
    }

    // @Bean
    // public DaoAuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    //     authProvider.setUserDetailsService(userDetailsService);
    //     authProvider.setPasswordEncoder(passwordEncoder());
    //     return authProvider;
    // }

    // @Bean
    // public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    //     return http.getSharedObject(AuthenticationManager.class);
    // }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization","x-xsrf-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
