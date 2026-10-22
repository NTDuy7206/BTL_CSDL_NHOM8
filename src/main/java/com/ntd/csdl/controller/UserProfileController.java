package com.ntd.csdl.controller;

import com.ntd.csdl.dto.ProfileResponseDTO;
import com.ntd.csdl.dto.ProfileUpdateDTO;
import com.ntd.csdl.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService profileService;

    @GetMapping
    public ProfileResponseDTO getProfile(Authentication authentication) {
        return profileService.getProfile(authentication.getName());
    }

    @PutMapping
    public ProfileResponseDTO updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateDTO request
    ) {
        return profileService.updateProfile(authentication.getName(), request);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponseDTO updateAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return profileService.updateAvatar(authentication.getName(), file);
    }
}
