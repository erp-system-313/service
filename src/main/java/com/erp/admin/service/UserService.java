package com.erp.admin.service;

import com.erp.admin.dto.CreateUserRequest;
import com.erp.admin.dto.UpdateUserRequest;
import com.erp.admin.dto.UserDto;
import com.erp.admin.entity.Role;
import com.erp.admin.entity.User;
import com.erp.admin.repository.RoleRepository;
import com.erp.admin.repository.UserRepository;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<UserDto> findAll(int page, int size, String roleName, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;
        if (roleName != null && !roleName.isEmpty()) {
            users = userRepository.findByRoleName(roleName, pageable);
        } else if (Boolean.TRUE.equals(isActive)) {
            users = userRepository.findAllActive(pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return PageResponse.from(users.map(this::toDto));
    }

    public UserDto findById(Long id) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDto(user);
    }

    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return toDto(user);
    }

    @Transactional
    public UserDto create(CreateUserRequest request, Long currentUserId, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("USER_001", "Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .build();

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));
            user.setRole(role);
        }

        user = userRepository.save(user);
        log.info("Created user with id: {}", user.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "User", user.getId(), null, ipAddress, "User created");

        return toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest request, Long currentUserId, String ipAddress) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("USER_001", "Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));
            user.setRole(role);
        }

user = userRepository.save(user);
        log.info("Updated user with id: {}", user.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "User", user.getId(), null, ipAddress, "User updated");

        return toDto(user);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Soft deleted user with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "User", id, null, ipAddress, "User deactivated");
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .employeeId(user.getEmployee() != null ? user.getEmployee().getId() : null)
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
