package com.shaqib.billing.security.auth;

import com.shaqib.billing.security.role.Role;
import com.shaqib.billing.security.user.AppUser;
import com.shaqib.billing.security.user.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.shaqib.billing.security.jwt.JwtService;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            AppUserService appUserService,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.appUserService = appUserService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AppUser user = appUserService.register(
                request.registrationToken(),
                request.password()
        );

        RegisterResponse response = new RegisterResponse(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        String token = jwtService.generateToken(
                authentication.getName(),
                role
        );

        LoginResponse response = new LoginResponse(
                authentication.getName(),
                role,
                token
        );

        return ResponseEntity.ok(response);
    }
}