package com.erp.admin.service;

import com.erp.admin.dto.CreateRoleRequest;
import com.erp.admin.dto.RoleDto;
import com.erp.admin.dto.UpdateRoleRequest;
import com.erp.admin.entity.Permission;
import com.erp.admin.entity.Role;
import com.erp.admin.repository.PermissionRepository;
import com.erp.admin.repository.RoleRepository;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<RoleDto> findAll() {
        return roleRepository.findAll().stream()
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
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException("ROLE_001", "Role name already exists: " + request.getName());
        }

            Role role = Role.builder()
                    .name(request.getName().toUpperCase())
                    .description(request.getDescription())
                    .isActive(true)
                    .isSystem(false)
                    .rolePermissions(new java.util.HashSet<>())
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
            String newName = request.getName().toUpperCase();
            if (!newName.equals(role.getName()) && roleRepository.existsByName(newName)) {
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

        roleRepository.delete(role);
        log.info("Deleted role with id: {}", id);
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
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        Set<Permission> permissions = permissionIds.stream()
                .map(pid -> permissionRepository.findById(pid)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", pid)))
                .collect(Collectors.toSet());

        entityManager.createNativeQuery("DELETE FROM role_permissions WHERE role_id = :roleId")
                .setParameter("roleId", roleId)
                .executeUpdate();

        for (Permission p : permissions) {
            entityManager.createNativeQuery(
                    "INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permId)")
                    .setParameter("roleId", roleId)
                    .setParameter("permId", p.getId())
                    .executeUpdate();
        }

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
