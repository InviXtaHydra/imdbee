package com.imdbee.security;

import com.imdbee.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public AppUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public AuthUser loadUserByUsername(String username) {
        return users.findByUsernameIgnoreCase(username)
                .map(AuthUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
