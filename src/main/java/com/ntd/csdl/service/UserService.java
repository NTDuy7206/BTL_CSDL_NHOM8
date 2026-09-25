package com.ntd.csdl.service;

import com.ntd.csdl.dto.UserDTO;
import com.ntd.csdl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserService extends JpaRepository<User, Long> {
    User register(UserDTO userDTO);
    User login(UserDTO userDTO);
}
