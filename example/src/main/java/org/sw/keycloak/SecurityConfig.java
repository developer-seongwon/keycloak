package org.sw.keycloak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/login**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
//                        .authorizationEndpoint(authorization -> authorization
//                                .authorizationRequestResolver(pkceResolver(null))
//                        )
                        .defaultSuccessUrl("/home", true)
                );

        return http.build();
    }

//    @Bean
//    public OAuth2AuthorizationRequestResolver pkceResolver(
//            @Autowired ClientRegistrationRepository repo) {
//        DefaultOAuth2AuthorizationRequestResolver resolver =
//                new DefaultOAuth2AuthorizationRequestResolver(
//                        repo,
//                        "/oauth2/authorization"
//                );
//        resolver.setAuthorizationRequestCustomizer(
//                OAuth2AuthorizationRequestCustomizers.withPkce());
//        return resolver;
//    }
}