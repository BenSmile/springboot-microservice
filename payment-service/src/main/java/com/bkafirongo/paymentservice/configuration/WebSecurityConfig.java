package com.bkafirongo.paymentservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(
                        authorizedRequests-> authorizedRequests
                                .antMatchers("/payments/**")
                                .hasAuthority("SCOPE_internal")
                                .anyRequest()
                                .authenticated()
                ).oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

        return http.build();
    }


}