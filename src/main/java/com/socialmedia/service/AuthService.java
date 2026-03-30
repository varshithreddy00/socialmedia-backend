package com.socialmedia.service;

import com.socialmedia.dto.request.LoginRequest;
import com.socialmedia.dto.request.RegisterRequest;
import com.socialmedia.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
