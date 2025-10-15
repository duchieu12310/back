package vn.nhom11.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.nhom11.jobhunter.domain.CompanyRegistration;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.service.CompanyRegistrationService;
import vn.nhom11.jobhunter.service.RoleService;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;
import vn.nhom11.jobhunter.util.constant.RegistrationStatus;

@RestController
@RequestMapping("/api/v1/company-registrations")
public class CompanyRegistrationController {

    private final CompanyRegistrationService registrationService;
    private final UserService userService;
    private final RoleService roleService;

    public CompanyRegistrationController(
            CompanyRegistrationService registrationService,
            UserService userService,
            RoleService roleService) {
        this.registrationService = registrationService;
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * 👤 Người dùng gửi yêu cầu đăng ký công ty mới
     */
    @PostMapping
    @ApiMessage("Create new company registration request")
    public ResponseEntity<CompanyRegistration> createRegistration(
            @Valid @RequestBody CompanyRegistration reqRegistration) {

        // Gán user đang đăng nhập cho yêu cầu
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.handleGetUserByUsername(username);
        reqRegistration.setUser(currentUser);

        CompanyRegistration created = registrationService.handleCreateRegistration(reqRegistration);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 🔍 Lấy danh sách yêu cầu đăng ký công ty (có phân quyền)
     * - Admin: xem tất cả
     * - User: chỉ xem của chính mình
     */
    @GetMapping
    @ApiMessage("Fetch company registration requests with pagination")
    public ResponseEntity<ResultPaginationDTO> getRegistrations(
            @Filter Specification<CompanyRegistration> spec, Pageable pageable) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);

        long roleId = user.getRole().getId();
        boolean isAdmin = roleService.permissionVsRole(roleId);

        ResultPaginationDTO result;
        if (isAdmin) {
            result = registrationService.handleGetRegistrations(spec, pageable);
        } else {
            result = registrationService.fetchRegistrationsByUser(username, pageable);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ Admin phê duyệt yêu cầu
     */
    @PutMapping("/{id}/status")
    @ApiMessage("Approve company registration request")
    public ResponseEntity<?> approveRegistration(@PathVariable("id") Long id) {
        CompanyRegistration updated = registrationService.handleUpdateStatus(id, RegistrationStatus.APPROVED, null);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not found");
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * ❌ Admin từ chối yêu cầu (có lý do)
     */
    @PutMapping("/{id}/reject")
    @ApiMessage("Reject company registration request")
    public ResponseEntity<?> rejectRegistration(
            @PathVariable("id") Long id,
            @RequestBody String rejectionReason) {
        CompanyRegistration updated = registrationService.handleUpdateStatus(id, RegistrationStatus.REJECTED,
                rejectionReason);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not found");
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * 🔎 Lấy chi tiết một yêu cầu theo ID
     */
    @GetMapping("/{id}")
    @ApiMessage("Fetch company registration detail")
    public ResponseEntity<CompanyRegistration> getRegistrationById(@PathVariable Long id) {
        Optional<CompanyRegistration> registration = registrationService.findById(id);
        if (registration.isPresent()) {
            return ResponseEntity.ok(registration.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * 🗑️ Xóa yêu cầu đăng ký công ty (nếu cần)
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Delete company registration request")
    public ResponseEntity<?> deleteRegistration(@PathVariable("id") Long id) {
        registrationService.handleDeleteRegistration(id);
        return ResponseEntity.ok().build();
    }
}
