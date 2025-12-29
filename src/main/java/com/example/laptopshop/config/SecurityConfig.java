package com.example.laptopshop.config;

import com.example.laptopshop.service.CustomOAuth2UserService;
import com.example.laptopshop.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Các trang Public
                        .requestMatchers("/", "/login", "/register", "/forgot-password", "/reset-password", "/product/**", "/search/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()

                        // Mở khóa cho Chat WebSocket và API Chat
                        .requestMatchers("/ws/**", "/api/chat/**").permitAll()

                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/videos/**").permitAll()

                        // 3. Trang Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 4. Trang Khách hàng
                        .requestMatchers("/cart/**", "/checkout/**", "/account/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(myAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(myAuthenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request,
                                                jakarta.servlet.http.HttpServletResponse response,
                                                org.springframework.security.core.Authentication authentication)
                    throws java.io.IOException, jakarta.servlet.ServletException {

                var authorities = authentication.getAuthorities();
                String targetUrl = "/";

                for (var authority : authorities) {
                    if (authority.getAuthority().equals("ROLE_ADMIN")) {
                        targetUrl = "/admin/dashboard";
                        break;
                    }
                }
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            }
        };
    }
}