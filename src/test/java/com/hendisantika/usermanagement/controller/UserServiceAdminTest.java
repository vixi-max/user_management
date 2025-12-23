package com.hendisantika.usermanagement.controller;
import com.hendisantika.usermanagement.dto.ChangePasswordForm;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.repository.UserRepository;
import com.hendisantika.usermanagement.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changePassword_asAdmin_success() throws Exception {
        // ---------------- GIVEN ----------------
        User user = new User();
        user.setId(1L);
        user.setPassword("OLD_ENCODED");

        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setNewPassword("newPass");
        form.setConfirmPassword("newPass");

        // Mock du repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("NEW_ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Crée un UserDetails réel pour ADMIN
        org.springframework.security.core.userdetails.User adminUser =
                new org.springframework.security.core.userdetails.User(
                        "admin",
                        "password",
                        Collections.singleton(() -> "ROLE_ADMIN")
                );

        // Mock Authentication et SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(adminUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // ---------------- WHEN ----------------
        User result = userService.changePassword(form);

        // ---------------- THEN ----------------
        assertEquals("NEW_ENCODED", result.getPassword());
        verify(userRepository).save(any(User.class));
    }
}


