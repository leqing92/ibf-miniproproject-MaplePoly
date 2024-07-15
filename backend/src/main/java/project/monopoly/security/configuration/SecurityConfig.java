package project.monopoly.security.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import project.monopoly.security.filter.JWTAuthFilter;
import project.monopoly.security.service.UserInfoService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JWTAuthFilter authFilter;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoService();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                // no auth
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/welcome", 
                    "/api/auth/signup", 
                    "/api/auth/addNewUser", 
                    "/api/auth/generateToken", 
                    "/api/auth/logout", 
                    "/api/stripe/**",
                    // below require for after angular build into spring
                    "/index.html",
                    "/",
                    "/assets/**",
                    "/media/**",
                    "/*.ico",
                    "/*.css",
                    "/*.js", 
                    "/*.png",
                    "/*.jpg", 
                    "/*.json",
                    "/*.webmanifest"
                    ).permitAll())
                // require "ROLE_USER"
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                    "/api/auth/user/**",
                    "/api/game/**",
                    "/api/gamerooms/**",
                    "/app/**" //wevsocket
                    ).authenticated())
                // require "ROLE_ADMIN"
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/admin/**").authenticated())
                // SessionCreationPolicy.ALWAYS : create session always
                // SessionCreationPolicy.IF_REQUIRED : create only required
                // SessionCreationPolicy.NEVER : do not create session, will use existin gone
                // SessionCreationPolicy.STATELESS : no session will be save in session < typical use for spring security with JWT to ensure all request contain necessary information for auth
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configure the authentication provider
                .authenticationProvider(authenticationProvider())
                // Add JWT authentication filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}
