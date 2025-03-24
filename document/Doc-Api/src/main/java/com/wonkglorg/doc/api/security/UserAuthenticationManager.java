package com.wonkglorg.doc.api.security;

import com.wonkglorg.doc.api.exception.LoginFailedException;
import com.wonkglorg.doc.api.service.UserService;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserAuthenticationManager {

    private final UserService userService;

    public record AuthResponse(String token, String error) {
    }

    public record LoginRequest(String repoId, String userId, String password) {
    }

    public UserAuthenticationManager(@Lazy UserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates the user
     *
     * @param userId   the users id
     * @param password the users password
     * @return true if valid false otherwise
     */
    public Optional<UserProfile> authenticate(final RepoId repoId, final UserId userId, final String password) throws InvalidUserException, InvalidRepoException {

        UserProfile user = userService.getUser(repoId, userId);
        if (user == null) {
            return Optional.empty();
        }

        if (!user.hashMatches(password)) {
            throw new LoginFailedException("Invalid password", HttpStatusCode.valueOf(401));
        }

        return Optional.of(user);
    }

}
