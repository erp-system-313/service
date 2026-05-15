package com.erp.admin.service;

import com.erp.admin.dto.CreateRoleRequest;
import com.erp.admin.dto.RoleDto;
import com.erp.admin.dto.UpdateRoleRequest;
import com.erp.admin.entity.Permission;
import com.erp.admin.entity.Role;
import com.erp.admin.repository.PermissionRepository;
import com.erp.admin.repository.RoleRepository;
import com.erp.admin.repository.UserRepository;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleDto> findAll() {
        return roleRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleDto findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        return toDto(role);
    }

    @Transactional
    public RoleDto create(CreateRoleRequest request) {
        String roleName = request.getName().trim().toUpperCase();

        if (roleRepository.existsByNameIgnoreCase(roleName)) {
            throw new BusinessException("ROLE_001", "Role name already exists: " + roleName);
        }

        Role role = Role.builder()
                .name(roleName)
                .description(request.getDescription())
                .isActive(true)
                .isSystem(false)
                .rolePermissions(new HashSet<>())
                .build();

        role = roleRepository.save(role);
        log.info("Created role with id: {}", role.getId());
        return toDto(role);
    }

    @Transactional
    public RoleDto update(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException("ROLE_002", "System roles cannot be modified");
        }

        if (request.getName() != null) {
            String newName = request.getName().trim().toUpperCase();
            if (!newName.equals(role.getName()) && roleRepository.existsByNameIgnoreCase(newName)) {
                throw new BusinessException("ROLE_001", "Role name already exists: " + newName);
            }
            role.setName(newName);
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        role = roleRepository.save(role);
        log.info("Updated role with id: {}", role.getId());
        return toDto(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException("ROLE_002", "System roles cannot be deleted");
        }

        if (userRepository.existsByRoleIdAndIsActiveTrue(id)) {
            throw new BusinessException("ROLE_003",
                    "Cannot delete a role assigned to active users");
        }

        role.setActive(false);
        roleRepository.save(role);
        log.info("Deactivated role with id: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Long> getPermissionIds(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        return role.getRolePermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException("ROLE_004",
                    "System role permissions cannot be modified");
        }

        Set<Permission> permissions = permissionIds.stream()
                .map(pid -> permissionRepository.findById(pid)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", pid)))
                .collect(Collectors.toSet());

        role.getRolePermissions().clear();
        role.getRolePermissions().addAll(permissions);
        roleRepository.save(role);

        log.info("Assigned {} permissions to role id: {}", permissionIds.size(), roleId);
    }

    private RoleDto toDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(Boolean.TRUE.equals(role.getIsSystem()))
                .isActive(role.isActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .permissionIds(role.getRolePermissions().stream()
                        .map(Permission::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}
