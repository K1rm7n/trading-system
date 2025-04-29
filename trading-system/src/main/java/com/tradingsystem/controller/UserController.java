package com.tradingsystem.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingsystem.model.dto.JwtAuthResponseDTO;
import com.tradingsystem.model.dto.LoginRequestDTO;
import com.tradingsystem.model.dto.SignupRequestDTO;
import com.tradingsystem.model.dto.UserDTO;
import com.tradingsystem.model.entity.User;
import com.tradingsystem.security.JwtTokenProvider;
import com.tradingsystem.service.interfaces.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API для аутентификации и управления пользователями")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя по логину и паролю")
    public ResponseEntity<JwtAuthResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthResponseDTO(jwt));
    }

    @PostMapping("/signup")
    @Operation(summary = "Регистрация", description = "Регистрация нового пользователя")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody SignupRequestDTO signupRequest) {
        // Проверяем, существует ли пользователь с таким именем
        if (userService.existsByUsername(signupRequest.getUsername())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Проверяем, существует ли пользователь с таким email
        if (userService.existsByEmail(signupRequest.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Создаем нового пользователя
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));

        User savedUser = userService.createUser(user);

        UserDTO userDTO = convertToDTO(savedUser);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о себе", description = "Возвращает информацию о текущем аутентифицированном пользователе")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(convertToDTO(user));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
