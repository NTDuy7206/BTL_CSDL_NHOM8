CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL,
    display_name VARCHAR(120) NULL,
    email VARCHAR(190) NULL,
    phone VARCHAR(30) NULL,
    avatar_object_key VARCHAR(255) NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
