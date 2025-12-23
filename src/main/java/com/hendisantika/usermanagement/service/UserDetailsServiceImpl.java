package com.hendisantika.usermanagement.service;

import com.hendisantika.usermanagement.entity.Role;
import com.hendisantika.usermanagement.repository.UserRepository;
import com.hendisantika.usermanagement.entity.User; // <-- ton entité JPA
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // On récupère l'utilisateur depuis la base de données
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Login Username Invalid."));

        // On transforme les rôles en GrantedAuthority pour Spring Security
        Set<GrantedAuthority> authorities = appUser.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getDescription()))
                .collect(Collectors.toSet());

        // On retourne un UserDetails compatible Spring Security
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .authorities(authorities)
                .build();
    }
}

