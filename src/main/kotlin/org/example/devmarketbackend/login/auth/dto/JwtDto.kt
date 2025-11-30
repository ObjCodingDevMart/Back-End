package org.example.devmarketbackend.login.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@Getter
@Setter
public class JwtDto {
    private String accessToken;
    private String refreshToken;
    private Long ttl;

    public JwtDto(String accessToken, String refreshToken,Long ttl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.ttl = ttl;
    }
}
