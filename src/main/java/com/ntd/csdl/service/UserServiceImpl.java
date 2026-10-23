package com.ntd.csdl.service;

import com.ntd.csdl.dto.UserDTO;
import com.ntd.csdl.entity.User;
import com.ntd.csdl.entity.UserEnum;
import com.ntd.csdl.repo.UserRepository;
import com.ntd.csdl.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class UserServiceImpl implements  UserService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    public User login(UserDTO usersDTO) {
        return userRepository.findAllByUsername(usersDTO.getUsername()).stream()
                .filter(user -> passwordEncoder.matches(usersDTO.getPassword(), user.getPassword()))
                .findFirst()
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu"));
    }

    @Override
    public User register(UserDTO usersDTO) {
        UserEnum requestedRole = UserEnum.USER;
        if (usersDTO.getRole() != null && !usersDTO.getRole().isBlank()) {
            try {
                requestedRole = UserEnum.valueOf(usersDTO.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new com.ntd.csdl.exception.UserException("Vai trò không hợp lệ");
            }
            if (requestedRole != UserEnum.OWNER && requestedRole != UserEnum.TENANT) {
                throw new com.ntd.csdl.exception.UserException("Chỉ được đăng ký vai trò chủ nhà hoặc khách thuê");
            }
            if (requestedRole == UserEnum.OWNER) {
                requestedRole = UserEnum.OWNER_PENDING;
            }
        }

        if (userRepository.existsByUsername(usersDTO.getUsername())) {
            throw new com.ntd.csdl.exception.UserException("Tên đăng nhập đã tồn tại");
        }
        User users=new User();
        users.setUsername(usersDTO.getUsername());
        String encodedPassword = passwordEncoder.encode(usersDTO.getPassword());
        users.setPassword(encodedPassword);
        users.setRole(requestedRole);
        try {
            return userRepository.save(users);
        } catch (DataIntegrityViolationException ex) {
            // The database constraint also protects against two simultaneous registrations.
            throw new com.ntd.csdl.exception.UserException("Tên đăng nhập đã tồn tại");
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends User> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<User> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public User getOne(Long aLong) {
        return null;
    }

    @Override
    public User getById(Long aLong) {
        return null;
    }

    @Override
    public User getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends User> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends User> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends User> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends User> S save(S entity) {
        return null;
    }

    @Override
    public <S extends User> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<User> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public List<User> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(User entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends User> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<User> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return null;
    }
}
