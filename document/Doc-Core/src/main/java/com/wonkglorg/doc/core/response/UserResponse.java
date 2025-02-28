package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.user.UserProfile;

/**
 * The response indicating a user
 */
public class UserResponse extends Response {
    private UserProfile userProfile;

    public UserResponse(UserProfile profile, String response, Exception error) {
        super(response, error);
        this.userProfile = profile;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }


}
