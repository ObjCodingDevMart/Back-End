package org.example.devmarketbackend.global.config

import org.example.devmarketbackend.login.auth.jwt.AuthCreationFilter
import org.example.devmarketbackend.login.auth.jwt.JwtValidationFilter
import org.example.devmarketbackend.login.auth.utils.OAuth2SuccessHandler
import org.example.devmarketbackend.login.auth.utils.OAuth2UserServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val authCreationFilter: AuthCreationFilter,
    private val jwtValidationFilter: JwtValidationFilter,
    private val oAuth2UserService: OAuth2UserServiceImpl,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화
            .csrf { it.disable() }

            // CORS 설정 적용
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }

            // 인증 및 권한 설정
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/health", // health check

                        "/swagger-ui/**",         // Swagger
                        "/v3/api-docs/**",

                        "/users/reissue",         // 토큰 재발급
                        "/users/logout",          // 로그아웃

                        "/token/**",              // 토큰 재발급 및 생성
                        "/oauth2/**",             // 카카오 OAuth 리디렉션
                        "/login/oauth2/**",        // 카카오 OAuth 콜백

                        "/categories/**",         //  로그인 없이 카테고리 조회 가능
                        "/items/**"               //  로그인 없이 상품 조회 가능
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            // 세션 정책: STATELESS (JWT 기반)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // OAuth2 로그인 설정 (UserService 연동)
            .oauth2Login { oauth2 ->
                oauth2
                    //.loginPage("/users/login")
                    .successHandler(oAuth2SuccessHandler)
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(oAuth2UserService)
                    }
            }

            // 필터 체인 적용
            .addFilterBefore(authCreationFilter, AnonymousAuthenticationFilter::class.java)
            .addFilterBefore(jwtValidationFilter, AuthCreationFilter::class.java)

        return http.build()
    }

    // CORS 설정
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",
            "https://coda-likelion.netlify.app/",
            "http://localhost:8080",
            "http://coda-dev-env.eba-pdmadfde.ap-northeast-2.elasticbeanstalk.com"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

