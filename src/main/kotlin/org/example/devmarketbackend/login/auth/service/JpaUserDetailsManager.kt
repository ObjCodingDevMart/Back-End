package org.example.devmarketbackend.login.auth.service

import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.jwt.CustomUserDetails
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service

/**
 * // JpaUserDetailsManager
 * // - Spring Security의 UserDetailsManager 인터페이스를 JPA 기반으로 구현
 * // - 소셜 로그인(카카오)에서 providerId를 username으로 사용
 * // - 신규 사용자 생성(createUser), 존재 여부(userExists) 확인에 집중
 */
@Service
class JpaUserDetailsManager(
    private val userRepository: UserRepository,   // // 사용자 조회/저장용 JPA 레포지토리
    private val passwordEncoder: PasswordEncoder // // (참고) 현재 소셜 로그인이라 직접 사용하지 않지만, 비밀번호 기반 로그인으로 확장 시 필요
) : UserDetailsManager {

    /**
     * // Security가 인증 과정에서 호출하는 메서드
     * // - providerId를 username으로 간주하고 사용자 정보를 조회
     * // - 찾은 User 엔티티를 CustomUserDetails로 변환하여 반환
     */
    override fun loadUserByUsername(providerId: String): UserDetails {
        val user = userRepository.findByProviderId(providerId)
            .orElseThrow {
                // // 없는 사용자면 우리 공통 예외로 변환 (API 응답 표준화에 맞춤)
                println("// 유저 정보 없음 (provider_id): $providerId")
                GeneralException.of(ErrorCode.USER_NOT_FOUND)
            }
        // // DB 엔티티 → Security에서 쓰는 UserDetails로 변환
        return CustomUserDetails.fromEntity(user)
    }
    // // 권장 형태(참고): 한 줄로 예외 공급
    // // .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

    /**
     * // 사용자 생성
     * // - 소셜 로그인 최초 성공 시 '신규 가입' 처리에서 호출됨
     * // - 여기서는 CustomUserDetails를 받아서 User 엔티티로 저장
     */
    override fun createUser(user: UserDetails) {
        println("// 사용자 생성 시도 중 (provider_id): ${user.username}")

        // // providerId(provider_id)가 이미 존재하면 생성 불가
        if (userExists(user.username)) {
            println("// 이미 존재하는 사용자 (provider_id): ${user.username}")
            // // 메시지/코드 정책에 맞는 ErrorCode 사용 (필요 시 별도 USER_ALREADY_EXISTS 등 정의 권장)
            throw GeneralException.of(ErrorCode.ALREADY_USED_NICKNAME)
        }

        try {
            // // CustomUserDetails → User 엔티티 변환 후 저장
            val newUser = (user as CustomUserDetails).toEntity()

            // // (참고) 비밀번호 기반 사용자라면 여기서 passwordEncoder.encode(...) 필요
            // // newUser.setPassword(passwordEncoder.encode(rawPassword));

            userRepository.save(newUser)
            println("// 사용자 생성 완료 (provider_id): ${user.username}")
        } catch (e: ClassCastException) {
            // // 예상 타입이 아니면 내부 서버 에러로 래핑
            println("// UserDetails → CustomUserDetails 변환 실패 (provider_id): ${user.username}")
            e.printStackTrace()
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * // 사용자 존재 여부 확인
     * // - providerId 기준으로 존재하는지 빠르게 체크
     */
    override fun userExists(providerId: String): Boolean {
        println("사용자 존재 여부 확인 (provider_id): $providerId")
        return userRepository.existsByProviderId(providerId)
    }

    /**
     * // 사용자 정보 업데이트 (현재 미구현)
     * // - 소셜 로그인 시 서버에서 직접 갱신할 데이터 범위가 명확해진 뒤 구현 권장
     */
    override fun updateUser(user: UserDetails) {
        println("사용자 정보 업데이트는 지원되지 않음 (provider_id): ${user.username}")
        throw UnsupportedOperationException("사용자 업데이트 기능은 아직 지원되지 않습니다.")
    }

    /**
     * // 사용자 삭제 (현재 미구현)
     * // - 실제 삭제 대신 '탈퇴 플래그'로 관리하는 소프트 삭제 전략을 권장
     */
    override fun deleteUser(providerId: String) {
        println("사용자 삭제는 지원되지 않음 (provider_id): $providerId")
        throw UnsupportedOperationException("사용자 삭제 기능은 아직 지원되지 않습니다.")
    }

    /**
     * // 비밀번호 변경 (소셜 로그인은 비밀번호를 사용하지 않음)
     * // - 자체 회원 가입/로그인 기능을 추가할 때 구현
     */
    override fun changePassword(oldPassword: String, newPassword: String) {
        println("비밀번호 변경은 지원되지 않음.")
        throw UnsupportedOperationException("비밀번호 변경 기능은 아직 지원되지 않습니다.")
    }
}

