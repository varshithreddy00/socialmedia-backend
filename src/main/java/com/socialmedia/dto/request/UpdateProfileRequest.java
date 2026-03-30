package com.socialmedia.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 255, message = "Bio must not exceed 255 characters")
    private String bio;

    private String profilePictureUrl;

    public UpdateProfileRequest() {}

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}
