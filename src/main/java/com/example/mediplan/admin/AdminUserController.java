package com.example.mediplan.admin;

import com.example.mediplan.admin.dto.AdminBulkActionRequest;
import com.example.mediplan.admin.dto.AdminChangeRoleRequest;
import com.example.mediplan.admin.dto.AdminCreateUserRequest;
import com.example.mediplan.admin.dto.AdminResetPasswordRequest;
import com.example.mediplan.admin.dto.AdminUpdateUserRequest;
import com.example.mediplan.admin.dto.AdminUserDetailsDTO;
import com.example.mediplan.admin.dto.AdminUserListItemDTO;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")

public class AdminUserController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "fullName", "email", "role", "active", "provider");

    private final AdminUserService adminUserService;
    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Page<AdminUserListItemDTO> listUsers(@RequestParam(value = "q", required = false) String q,
                                                @RequestParam(value = "role", required = false) Role role,
                                                @RequestParam(value = "active", required = false) Boolean active,
                                                @RequestParam(value = "provider", required = false) String provider,
                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "20") int size,
                                                @RequestParam(value = "sort", required = false) String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), parseSort(sort));
        return adminUserService.listUsers(q, role, active, provider, pageable);
    }

    @GetMapping("/{id}")
    public AdminUserDetailsDTO getUser(@PathVariable String id) {
        return adminUserService.getUser(id);
    }

    @PostMapping
    public ResponseEntity<AdminUserDetailsDTO> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        AdminUserDetailsDTO created = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public AdminUserDetailsDTO updateUser(@PathVariable String id, @Valid @RequestBody AdminUpdateUserRequest request) {
        return adminUserService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        adminUserService.deleteUser(id, hard);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        adminUserService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable String id) {
        adminUserService.reactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/role")
    public AdminUserDetailsDTO changeRole(@PathVariable String id, @Valid @RequestBody AdminChangeRoleRequest request) {
        return adminUserService.changeRole(id, request);
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable String id, @Valid @RequestBody AdminResetPasswordRequest request) {
        adminUserService.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk/deactivate")
    public ResponseEntity<Void> bulkDeactivate(@Valid @RequestBody AdminBulkActionRequest request) {
        adminUserService.bulkDeactivate(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk/reactivate")
    public ResponseEntity<Void> bulkReactivate(@Valid @RequestBody AdminBulkActionRequest request) {
        adminUserService.bulkReactivate(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk/delete")
    public ResponseEntity<Void> bulkDelete(@Valid @RequestBody AdminBulkActionRequest request) {
        adminUserService.bulkDelete(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(@RequestParam(value = "q", required = false) String q,
                                         @RequestParam(value = "role", required = false) Role role,
                                         @RequestParam(value = "active", required = false) Boolean active,
                                         @RequestParam(value = "provider", required = false) String provider,
                                         @RequestParam(value = "sort", required = false) String sort) {
        Sort exportSort = parseSort(sort);
        byte[] content = adminUserService.exportCsv(q, role, active, provider, exportSort);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            throw new BusinessRuleException("Champ de tri invalide");
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            try {
                direction = Sort.Direction.fromString(parts[1]);
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException("Direction de tri invalide");
            }
        }
        return Sort.by(direction, property);
    }
}
