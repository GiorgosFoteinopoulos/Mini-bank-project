package com.practice.bank.web;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           BankAuthenticationProvider bankAuthenticationProvider) throws Exception {
        http
                .authenticationProvider(bankAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard", "/account/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("accountNumber")
                        .passwordParameter("pin")
                        .successHandler((request, response, authentication) -> {
                            boolean admin = AccountSession.hasRole(authentication, "ROLE_ADMIN");
                            response.sendRedirect(admin ? "/admin" : "/dashboard");
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
