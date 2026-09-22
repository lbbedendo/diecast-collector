package com.diecastcollector.api.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Small helper so controllers/services don't reach into SecurityContextHolder directly. */
@Component
public class CurrentUser {

    public Long id() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new IllegalStateException("No authenticated user in the current security context");
        }
        return userId;
    }
}
