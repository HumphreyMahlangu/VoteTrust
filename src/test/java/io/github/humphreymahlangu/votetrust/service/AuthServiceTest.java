package io.github.humphreymahlangu.votetrust.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.humphreymahlangu.votetrust.dto.AuthResponse;
import io.github.humphreymahlangu.votetrust.dto.LoginRequest;
import io.github.humphreymahlangu.votetrust.dto.RegisterRequest;
import io.github.humphreymahlangu.votetrust.entity.AccountRole;
import io.github.humphreymahlangu.votetrust.entity.UserAccount;
import io.github.humphreymahlangu.votetrust.exception.DuplicateResourceException;
import io.github.humphreymahlangu.votetrust.exception.InvalidCredentialsException;
import io.github.humphreymahlangu.votetrust.repository.UserAccountRepository;
import io.github.humphreymahlangu.votetrust.security.JwtService;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userAccountRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerCreatesVoterAccountWithNormalizedEmailAndHashedPassword() throws Exception {
        RegisterRequest request = new RegisterRequest(" NewVoter@Example.COM ", "VeryStrongPassword");
        UUID userId = UUID.randomUUID();

        when(userAccountRepository.existsByEmailIgnoreCase("newvoter@example.com")).thenReturn(false);
        when(passwordEncoder.encode("VeryStrongPassword")).thenReturn("hashed-password");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount userAccount = invocation.getArgument(0);
            setId(userAccount, userId);
            return userAccount;
        });
        when(jwtService.generateAccessToken(any(UserAccount.class)))
                .thenReturn(new JwtService.TokenResult("signed.jwt", Instant.parse("2026-08-07T10:15:00Z")));

        AuthResponse response = authService.register(request);

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(accountCaptor.capture());
        UserAccount savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getEmail()).isEqualTo("newvoter@example.com");
        assertThat(savedAccount.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedAccount.getRole()).isEqualTo(AccountRole.VOTER);
        assertThat(savedAccount.isEnabled()).isTrue();
        assertThat(response.accessToken()).isEqualTo("signed.jwt");
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("voter@example.com", "VeryStrongPassword");
        when(userAccountRepository.existsByEmailIgnoreCase("voter@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userAccountRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void loginRejectsInvalidPasswordWithGenericError() {
        LoginRequest request = new LoginRequest("voter@example.com", "WrongPassword");
        UserAccount userAccount = new UserAccount("voter@example.com", "hashed-password", AccountRole.VOTER, true);

        when(userAccountRepository.findByEmailIgnoreCase("voter@example.com")).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("WrongPassword", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateAccessToken(any());
    }

    private void setId(UserAccount userAccount, UUID userId) throws Exception {
        Field idField = UserAccount.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(userAccount, userId);
    }
}
