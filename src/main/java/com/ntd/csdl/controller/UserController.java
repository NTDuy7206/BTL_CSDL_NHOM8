package com.ntd.csdl.controller;

import com.ntd.csdl.dto.AuthResponseDTO;
import com.ntd.csdl.dto.UserDTO;
import com.ntd.csdl.entity.User;
import com.ntd.csdl.security.CustomUserDetailsService;
import com.ntd.csdl.security.JwtService;
import com.ntd.csdl.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserDTO usersDTO) {
        User registeredUser = userService.register(usersDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody UserDTO usersDTO) {
        User user = userService.login(usersDTO);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        AuthResponseDTO response = new AuthResponseDTO(token, user);
        return ResponseEntity.ok(response);
    }
}
