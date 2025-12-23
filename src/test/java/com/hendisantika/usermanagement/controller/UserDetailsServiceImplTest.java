package com.hendisantika.usermanagement.controller;
import com.hendisantika.usermanagement.entity.Role;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.exception.CustomFieldValidationException;
import com.hendisantika.usermanagement.repository.UserRepository;
import com.hendisantika.usermanagement.service.UserDetailsServiceImpl;
import com.hendisantika.usermanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_success() {
        // GIVEN
        User user = new User();
        user.setUsername("imane");
        user.setPassword("secret");

        Role role = new Role();
        role.setDescription("ROLE_ADMIN");
        user.setRoles(Collections.singleton(role));

        when(userRepository.findByUsername("imane")).thenReturn(Optional.of(user));

        // WHEN
        UserDetails userDetails = userDetailsService.loadUserByUsername("imane");

        // THEN
        assertNotNull(userDetails);
        assertEquals("imane", userDetails.getUsername());
        assertEquals("secret", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByUsername("imane");
    }

    @Test
    void loadUserByUsername_notFound() {
        // GIVEN
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // THEN
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown")
        );

        verify(userRepository).findByUsername("unknown");
    }
    @Test
    void createUser_existingUsername_throwsException() throws Exception {
        // GIVEN
        User user = new User();
        user.setUsername("existingUser");
        user.setPassword("pass");
        user.setConfirmPassword("pass");

        // Simule un utilisateur déjà existant
        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(user));

        // WHEN & THEN
        assertThrows(CustomFieldValidationException.class, () -> {
            userService.createUser(user);
        });
    }
}
