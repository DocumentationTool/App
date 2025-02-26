package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.user.UserProfile;

public class UserResponse extends Response {
    private UserProfile userProfile;

    public UserResponse(UserProfile profile, String response, String errorMessage) {
        super(response, errorMessage);
        this.userProfile = profile;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }


}
