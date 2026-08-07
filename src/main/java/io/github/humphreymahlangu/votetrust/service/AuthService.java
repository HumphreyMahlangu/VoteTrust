package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.AuthResponse;
import io.github.humphreymahlangu.votetrust.dto.LoginRequest;
import io.github.humphreymahlangu.votetrust.dto.RegisterRequest;
import io.github.humphreymahlangu.votetrust.entity.AccountRole;
import io.github.humphreymahlangu.votetrust.entity.SecurityAuditEventType;
import io.github.humphreymahlangu.votetrust.entity.SecurityAuditOutcome;
import io.github.humphreymahlangu.votetrust.entity.UserAccount;
import io.github.humphreymahlangu.votetrust.exception.DuplicateResourceException;
import io.github.humphreymahlangu.votetrust.exception.InvalidCredentialsException;
import io.github.humphreymahlangu.votetrust.repository.UserAccountRepository;
import io.github.humphreymahlangu.votetrust.security.JwtService;
import io.github.humphreymahlangu.votetrust.security.SecurityAuditMetadata;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityAuditService securityAuditService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SecurityAuditService securityAuditService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityAuditService = securityAuditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return register(request, SecurityAuditMetadata.system());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, SecurityAuditMetadata metadata) {
        String email = normalizeEmail(request.email());

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            securityAuditService.record(
                    SecurityAuditEventType.USER_REGISTER,
                    SecurityAuditOutcome.FAILURE,
                    null,
                    email,
                    metadata,
                    "Duplicate email rejected"
            );
            throw new DuplicateResourceException("A user account with this email already exists");
        }

        UserAccount userAccount = new UserAccount(
                email,
                passwordEncoder.encode(request.password()),
                AccountRole.VOTER,
                true
        );

        UserAccount savedAccount = userAccountRepository.save(userAccount);
        AuthResponse response = createAuthResponse(savedAccount);
        securityAuditService.record(
                SecurityAuditEventType.USER_REGISTER,
                SecurityAuditOutcome.SUCCESS,
                savedAccount,
                metadata,
                "Voter account registered"
        );
        return response;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        return login(request, SecurityAuditMetadata.system());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, SecurityAuditMetadata metadata) {
        String email = normalizeEmail(request.email());
        UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (userAccount == null) {
            securityAuditService.record(
                    SecurityAuditEventType.USER_LOGIN,
                    SecurityAuditOutcome.FAILURE,
                    null,
                    email,
                    metadata,
                    "Account not found"
            );
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), userAccount.getPasswordHash())) {
            securityAuditService.record(
                    SecurityAuditEventType.USER_LOGIN,
                    SecurityAuditOutcome.FAILURE,
                    userAccount,
                    metadata,
                    "Invalid password"
            );
            throw new InvalidCredentialsException();
        }

        if (!userAccount.isEnabled()) {
            securityAuditService.record(
                    SecurityAuditEventType.USER_LOGIN,
                    SecurityAuditOutcome.FAILURE,
                    userAccount,
                    metadata,
                    "Account disabled"
            );
            throw new InvalidCredentialsException();
        }

        AuthResponse response = createAuthResponse(userAccount);
        securityAuditService.record(
                SecurityAuditEventType.USER_LOGIN,
                SecurityAuditOutcome.SUCCESS,
                userAccount,
                metadata,
                "JWT issued"
        );
        return response;
    }

    private AuthResponse createAuthResponse(UserAccount userAccount) {
        JwtService.TokenResult tokenResult = jwtService.generateAccessToken(userAccount);
        return new AuthResponse(
                tokenResult.token(),
                "Bearer",
                tokenResult.expiresAt(),
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getRole().name()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
