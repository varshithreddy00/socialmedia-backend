package com.socialmedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    // No size limit — supports Base64 encoded images
    private String imageUrl;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}