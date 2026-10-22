package com.ntd.csdl.service;

import com.ntd.csdl.dto.ProfileResponseDTO;
import com.ntd.csdl.dto.ProfileUpdateDTO;
import com.ntd.csdl.entity.User;
import com.ntd.csdl.entity.UserProfile;
import com.ntd.csdl.repo.UserProfileRepository;
import com.ntd.csdl.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final MinioAvatarStorageService avatarStorage;

    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfile(String username) {
        User user = findUser(username);
        return toResponse(user, profileRepository.findById(user.getId()).orElse(null));
    }

    @Transactional
    public ProfileResponseDTO updateProfile(String username, ProfileUpdateDTO request) {
        User user = findUser(username);
        UserProfile profile = profileRepository.findById(user.getId()).orElseGet(() -> newProfile(user));
        profile.setDisplayName(clean(request.displayName()));
        profile.setEmail(clean(request.email()));
        profile.setPhone(clean(request.phone()));
        return toResponse(user, profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponseDTO updateAvatar(String username, MultipartFile file) {
        User user = findUser(username);
        UserProfile profile = profileRepository.findById(user.getId()).orElseGet(() -> newProfile(user));
        String previousKey = profile.getAvatarObjectKey();
        profile.setAvatarObjectKey(avatarStorage.store(file));
        UserProfile saved = profileRepository.save(profile);
        if (previousKey != null && !previousKey.isBlank()) {
            avatarStorage.delete(previousKey);
        }
        return toResponse(user, saved);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại."));
    }

    private UserProfile newProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        return profile;
    }

    private ProfileResponseDTO toResponse(User user, UserProfile profile) {
        String avatarUrl = profile == null || profile.getAvatarObjectKey() == null
                ? null
                : avatarStorage.presignedUrl(profile.getAvatarObjectKey());
        String displayName = profile == null || profile.getDisplayName() == null || profile.getDisplayName().isBlank()
                ? user.getUsername()
                : profile.getDisplayName();
        return new ProfileResponseDTO(
                user.getUsername(),
                displayName,
                profile == null ? null : profile.getEmail(),
                profile == null ? null : profile.getPhone(),
                avatarUrl
        );
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
