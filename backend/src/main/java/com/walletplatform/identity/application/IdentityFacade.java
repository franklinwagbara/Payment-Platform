package com.walletplatform.identity.application;

import com.walletplatform.identity.application.service.UserService;
import com.walletplatform.identity.domain.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdentityFacade {

    private final UserService userService;

    public IdentityFacade(UserService userService) {
        this.userService = userService;
    }

    public User getUserById(UUID userId) {
        return userService.getUserById(userId);
    }

    public User getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }
}

