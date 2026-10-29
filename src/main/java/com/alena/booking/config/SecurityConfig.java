
package com.alena.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${ADMIN_USERNAME}")
            String username,

            @Value("${ADMIN_PASSWORD}")
            String password,

            PasswordEncoder encoder) {

        UserDetails admin = User.builder()
                .username(username)
                .password(encoder.encode(password))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        // Public booking availability for GET APIs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/booked-times",
                                "/api/cabinet/history",
                                "/api/i18n",
                                "/api/services",
                                "/api/sms/is-verified",
                                "/api/customers/verified"
                        ).permitAll()

                        // Existing public booking operations for POST APIs
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/bookings",
                                "/api/bookings/cancel",
                                "/api/bookings/cancel/old",
                                "/api/sms4free/callback",
                                "/api/sms4free/status",
                                "/api/sms/send-code",
                                "/api/sms/validate",
                                "/api/sms/status"
                        ).permitAll()

                        // All admin endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Diagnostic endpoint must not be public
                        .requestMatchers("/api/bookings/test/**")
                        .hasRole("ADMIN")

                        // Admin page
                        .requestMatchers("/admin", "/admin/**")
                        .hasRole("ADMIN")

                        // Swagger available after login
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .hasRole("ADMIN")  // requires both authentication and the administrator role.
                        //.authenticated()

                        // Deny anonymous access to everything else
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/admin/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/bookings"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/bookings/cancel"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/bookings/cancel/old"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/sms4free/callback"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/sms4free/status"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/sms/send-code"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/sms/validate"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/sms/status"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.GET, "/api/bookings/booked-times"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.GET, "/api/cabinet/history"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.GET, "/api/i18n"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.GET, "/api/services"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.GET, "/api/sms/is-verified"),
                        PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.GET, "/api/customers/verified")
                )
        );


        return http.build();
    }
}
