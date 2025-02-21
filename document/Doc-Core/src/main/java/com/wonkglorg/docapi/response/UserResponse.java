package com.wonkglorg.docapi.response;

import com.wonkglorg.docapi.user.UserProfile;

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
