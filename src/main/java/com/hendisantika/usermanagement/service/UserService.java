package com.hendisantika.usermanagement.service;

import com.hendisantika.usermanagement.dto.ChangePasswordForm;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.exception.CustomFieldValidationException;
import com.hendisantika.usermanagement.exception.UsernameOrIdNotFound;
import com.hendisantika.usermanagement.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository repository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.repository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // ---------------- READ ----------------
    @Transactional(readOnly = true)
    public Iterable<User> getAllUsers() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) throws UsernameOrIdNotFound {
        Optional<User> userOptional = repository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new UsernameOrIdNotFound("User id does not exist.");
        }
    }

    // ---------------- CREATE ----------------
    public User createUser(User user) throws Exception {
        validateUsernameAndPassword(user);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    private void validateUsernameAndPassword(User user) throws Exception {
        Optional<User> existingUser = repository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new CustomFieldValidationException("Username not available", "username");
        }

        if (user.getConfirmPassword() == null || user.getConfirmPassword().isEmpty()) {
            throw new CustomFieldValidationException("Confirm Password is required", "confirmPassword");
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            throw new CustomFieldValidationException("Password and Confirm Password are not the same", "password");
        }
    }

    // ---------------- UPDATE ----------------
    public User updateUser(User fromUser) throws Exception {
        User toUser = getUserById(fromUser.getId());
        mapUser(fromUser, toUser);
        return repository.save(toUser);
    }

    protected void mapUser(User from, User to) {
        to.setUsername(from.getUsername());
        to.setFirstName(from.getFirstName());
        to.setLastName(from.getLastName());
        to.setEmail(from.getEmail());
        to.setRoles(from.getRoles());
    }

    // ---------------- DELETE ----------------
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void deleteUser(Long id) throws UsernameOrIdNotFound {
        User user = getUserById(id);
        repository.delete(user);
    }

    // ---------------- CHANGE PASSWORD ----------------
    public User changePassword(ChangePasswordForm form) throws Exception {
        User user = getUserById(form.getId());

        if (!isLoggedUserADMIN() && !bCryptPasswordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new Exception("Current Password invalid.");
        }

        if (bCryptPasswordEncoder.matches(form.getNewPassword(), user.getPassword())) {
            throw new Exception("New password must be different from the current password.");
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new Exception("New Password and Confirm Password do not match.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(form.getNewPassword()));
        return repository.save(user);
    }

    // ---------------- UTILITIES ----------------
    private boolean isLoggedUserADMIN() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails loggedUser = (UserDetails) principal;
            return loggedUser.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        }
        return false;
    }

    public User getLoggedUser() throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails loggedUser = (UserDetails) principal;
            Optional<User> userOptional = repository.findByUsername(loggedUser.getUsername());
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else {
                throw new Exception("Logged-in user not found in DB.");
            }
        }
        throw new Exception("No logged-in user found.");
    }
}
