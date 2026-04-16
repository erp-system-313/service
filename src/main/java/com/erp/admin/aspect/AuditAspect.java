package com.erp.admin.aspect;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.UserPrincipal;
import com.erp.common.annotation.Auditable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.HashMap;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String entityType = auditable.entityType();
        Long entityId = null;
        Map<String, Object> changes = new HashMap<>();

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Long) {
                    entityId = (Long) args[i];
                } else if (args[i] instanceof com.erp.admin.dto.CreateUserRequest) {
                    var request = (com.erp.admin.dto.CreateUserRequest) args[i];
                    changes.put("email", request.getEmail());
                } else if (args[i] instanceof com.erp.hr.dto.CreateEmployeeRequest) {
                    var request = (com.erp.hr.dto.CreateEmployeeRequest) args[i];
                    changes.put("email", request.getEmail());
                    changes.put("department", request.getDepartment());
                }
            }
        }

        Object result = joinPoint.proceed();

        if (result != null) {
            if (result instanceof com.erp.admin.dto.UserDto) {
                entityId = ((com.erp.admin.dto.UserDto) result).getId();
            } else if (result instanceof com.erp.hr.dto.EmployeeDto) {
                entityId = ((com.erp.hr.dto.EmployeeDto) result).getId();
            }
        }

        String ipAddress = getClientIpAddress();

        try {
            auditLogService.log(
                    getCurrentUser(),
                    action,
                    entityType,
                    entityId,
                    changes.isEmpty() ? null : changes,
                    ipAddress,
                    String.format("%s %s", action, entityType)
            );
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}", action, entityType, e.getMessage());
        }

        return result;
    }

    private com.erp.admin.entity.User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return com.erp.admin.entity.User.builder()
                    .id(userPrincipal.getId())
                    .email(userPrincipal.getEmail())
                    .build();
        }
        return null;
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return null;
    }
}
