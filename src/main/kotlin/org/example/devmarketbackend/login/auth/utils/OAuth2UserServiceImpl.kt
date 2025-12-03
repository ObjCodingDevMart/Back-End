package org.example.devmarketbackend.login.auth.utils

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * OAuth2 사용자 정보를 가져오는 기본 구현을 유지하되, 확장을 위한 클래스
 */
@Service
class OAuth2UserServiceImpl : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        return super.loadUser(userRequest)
    }
}

