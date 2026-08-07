package io.github.humphreymahlangu.votetrust.service;

import io.github.humphreymahlangu.votetrust.dto.AdminBootstrapRequest;
import io.github.humphreymahlangu.votetrust.dto.AuthResponse;
import io.github.humphreymahlangu.votetrust.entity.AccountRole;
import io.github.humphreymahlangu.votetrust.entity.UserAccount;
import io.github.humphreymahlangu.votetrust.exception.AdminBootstrapException;
import io.github.humphreymahlangu.votetrust.exception.DuplicateResourceException;
import io.github.humphreymahlangu.votetrust.exception.InvalidBootstrapTokenException;
import io.github.humphreymahlangu.votetrust.repository.UserAccountRepository;
import io.github.humphreymahlangu.votetrust.security.AdminBootstrapProperties;
import io.github.humphreymahlangu.votetrust.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminBootstrapService {

    private final AdminBootstrapProperties adminBootstrapProperties;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminBootstrapService(
            AdminBootstrapProperties adminBootstrapProperties,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminBootstrapProperties = adminBootstrapProperties;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse bootstrapFirstAdmin(AdminBootstrapRequest request, String bootstrapToken) {
        if (!adminBootstrapProperties.enabled()) {
            throw new AdminBootstrapException("Admin bootstrap is disabled");
        }

        if (!tokenMatches(bootstrapToken)) {
            throw new InvalidBootstrapTokenException();
        }

        if (userAccountRepository.existsByRole(AccountRole.ADMIN)) {
            throw new DuplicateResourceException("An admin account already exists");
        }

        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A user account with this email already exists");
        }

        UserAccount adminAccount = userAccountRepository.save(new UserAccount(
                email,
                passwordEncoder.encode(request.password()),
                AccountRole.ADMIN,
                true
        ));

        JwtService.TokenResult tokenResult = jwtService.generateAccessToken(adminAccount);
        return new AuthResponse(
                tokenResult.token(),
                "Bearer",
                tokenResult.expiresAt(),
                adminAccount.getId(),
                adminAccount.getEmail(),
                adminAccount.getRole().name()
        );
    }

    private boolean tokenMatches(String bootstrapToken) {
        if (!StringUtils.hasText(bootstrapToken) || !StringUtils.hasText(adminBootstrapProperties.token())) {
            return false;
        }

        byte[] expected = adminBootstrapProperties.token().getBytes(StandardCharsets.UTF_8);
        byte[] provided = bootstrapToken.getBytes(StandardCharsets.UTF_8);
        return expected.length == provided.length && MessageDigest.isEqual(expected, provided);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
