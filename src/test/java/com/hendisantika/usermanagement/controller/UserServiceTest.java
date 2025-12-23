package com.hendisantika.usermanagement.controller;

import com.hendisantika.usermanagement.dto.ChangePasswordForm;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.exception.CustomFieldValidationException;
import com.hendisantika.usermanagement.exception.UsernameOrIdNotFound;
import com.hendisantika.usermanagement.repository.UserRepository;
import com.hendisantika.usermanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ---------------- getUserById ----------------

    @Test
    void getUserById_success() throws Exception {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UsernameOrIdNotFound.class,
                () -> userService.getUserById(1L)
        );
    }

    // ---------------- createUser ----------------

    @Test
    void createUser_success() throws Exception {
        User user = new User();
        user.setUsername("imane");
        user.setPassword("1234");
        user.setConfirmPassword("1234");

        when(userRepository.findByUsername("imane"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234"))
                .thenReturn("ENCODED");
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(user);

        assertEquals("ENCODED", result.getPassword());
    }

    // ---------------- updateUser ----------------

    @Test
    void updateUser_success() throws Exception {
        User existing = new User();
        existing.setId(1L);

        User updated = new User();
        updated.setId(1L);
        updated.setUsername("newName");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class)))
                .thenReturn(existing);

        User result = userService.updateUser(updated);

        assertEquals("newName", result.getUsername());
    }

    // ---------------- deleteUser ----------------

    @Test
    void deleteUser_success() throws Exception {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }
    @Test
    void changePassword_confirmPasswordMismatch() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setNewPassword("123");
        form.setConfirmPassword("456");

        assertThrows(Exception.class,
                () -> userService.changePassword(form));
    }
    @Test
    void createUser_usernameAlreadyExists() {
        // GIVEN
        User user = new User();
        user.setUsername("imane");
        user.setPassword("1234");
        user.setConfirmPassword("1234");

        when(userRepository.findByUsername("imane"))
                .thenReturn(Optional.of(new User()));

        // WHEN + THEN
        CustomFieldValidationException ex = assertThrows(
                CustomFieldValidationException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Username not available", ex.getMessage());
        assertEquals("username", ex.getFieldName());
    }





}
