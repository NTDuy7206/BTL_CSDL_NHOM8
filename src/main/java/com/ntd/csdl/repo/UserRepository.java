package com.ntd.csdl.repo;

import com.ntd.csdl.entity.User;
import com.ntd.csdl.entity.UserEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRoleOrderByIdAsc(UserEnum role);
}
