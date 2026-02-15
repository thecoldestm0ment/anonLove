package com.anonLove.service.auth;

import com.anonLove.domain.user.User;
import com.anonLove.dto.request.auth.LoginRequest;
import com.anonLove.dto.request.auth.SignupRequest;
import com.anonLove.dto.response.auth.LoginResponse;
import com.anonLove.dto.response.auth.TokenResponse;
import com.anonLove.dto.response.auth.UserResponse;
import com.anonLove.exception.CustomException;
import com.anonLove.exception.ErrorCode;
import com.anonLove.repository.UserRepository;
import com.anonLove.security.JwtTokenProvider;
import com.anonLove.util.EmailValidator;
import com.anonLove.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_VERIFIED_PREFIX = "otp_verified:";
    private static final long OTP_EXPIRATION = 3;
    private static final long OTP_VERIFIED_EXPIRATION = 5;

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    // 이메일 인증 코드 전송
    @Transactional
    public void sendVerificationEmail(String email) {
        if (!EmailValidator.isUniversityEmail(email)) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String otp = OtpGenerator.generate();
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION, TimeUnit.MINUTES);

        emailService.sendOtp(email, otp);
        log.info("OTP sent to: {}", email);
    }

    // 이메일 인증 코드 검증
    public void verifyOtp(String email, String code) {
        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            throw new CustomException(ErrorCode.OTP_NOT_FOUND);
        }

        if (!storedOtp.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_OTP);
        }

        // OTP 검증 성공 표시 (5분 유효)
        String verifiedKey = OTP_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(verifiedKey, "true", OTP_VERIFIED_EXPIRATION, TimeUnit.MINUTES);

        // 기존 OTP 삭제
        redisTemplate.delete(key);
        log.info("OTP verified for: {}", email);
    }

    @Transactional
    public void signup(SignupRequest request) {
        // OTP 검증 완료 여부 확인
        String verifiedKey = OTP_VERIFIED_PREFIX + request.getEmail();
        String verified = redisTemplate.opsForValue().get(verifiedKey);

        if (verified == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Email verification required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .university(request.getUniversity())
                .gender(request.getGender())
                .build();

        userRepository.save(user);

        // 검증 표시 삭제
        redisTemplate.delete(verifiedKey);

        log.info("User registered: {}", request.getEmail());
    }

    // 로그인 (이메일로 사용자 찾고 비밀번호 검증 > 토큰 생성)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = tokenService.createAccessToken(user.getId());
        String refreshToken = tokenService.createRefreshToken(user.getId());

        log.info("User logged in: {}", user.getEmail());
        return new LoginResponse(user.getId(), accessToken, refreshToken);
    }

    // 토큰 재발급
    public TokenResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        if (!tokenService.validateRefreshToken(userId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = tokenService.createAccessToken(userId);
        String newRefreshToken = tokenService.createRefreshToken(userId);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // 로그아웃 (리프레시 토큰 삭제)
    public void logout(Long userId) {
        tokenService.deleteRefreshToken(userId);
        log.info("User logged out: userId={}", userId);
    }
}
