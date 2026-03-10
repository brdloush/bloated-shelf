package com.example.bloatedshelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN", "LIBRARIAN", "MEMBER", "VIEWER")
                .build();

        UserDetails librarian = User.withUsername("librarian")
                .password(encoder.encode("lib123"))
                .roles("LIBRARIAN", "MEMBER", "VIEWER")
                .build();

        UserDetails member = User.withUsername("member1")
                .password(encoder.encode("member123"))
                .roles("MEMBER", "VIEWER")
                .build();

        UserDetails viewer = User.withUsername("readonly")
                .password(encoder.encode("read123"))
                .roles("VIEWER")
                .build();

        return new InMemoryUserDetailsManager(admin, librarian, member, viewer);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()); // Disable CSRF for simple API demo
            
        return http.build();
    }
}
