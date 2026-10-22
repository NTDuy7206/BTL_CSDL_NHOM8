package com.ntd.csdl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "avatar_object_key", length = 255)
    private String avatarObjectKey;
}
