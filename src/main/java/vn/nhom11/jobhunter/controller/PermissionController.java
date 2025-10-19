package vn.nhom11.jobhunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.nhom11.jobhunter.domain.Permission;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.service.PermissionService;
import vn.nhom11.jobhunter.service.RoleService;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;
import vn.nhom11.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class PermissionController {

    private final PermissionService permissionService;
    private final UserService userService;
    private final RoleService roleService;

    public PermissionController(
            PermissionService permissionService,
            UserService userService,
            RoleService roleService) {
        this.permissionService = permissionService;
        this.userService = userService;
        this.roleService = roleService;
    }

    // ---------------- CREATE ----------------
    @PostMapping("/permissions")
    @ApiMessage("Create a permission")
    public ResponseEntity<Permission> create(@Valid @RequestBody Permission p) throws IdInvalidException {
        if (this.permissionService.isPermissionExist(p)) {
            throw new IdInvalidException("Permission đã tồn tại.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.create(p));
    }

    // ---------------- UPDATE ----------------
    @PutMapping("/permissions")
    @ApiMessage("Update a permission")
    public ResponseEntity<Permission> update(@Valid @RequestBody Permission p) throws IdInvalidException {
        if (this.permissionService.fetchById(p.getId()) == null) {
            throw new IdInvalidException("Permission với id = " + p.getId() + " không tồn tại.");
        }

        if (this.permissionService.isPermissionExist(p) && this.permissionService.isSameName(p)) {
            throw new IdInvalidException("Permission đã tồn tại.");
        }

        return ResponseEntity.ok().body(this.permissionService.update(p));
    }

    // ---------------- DELETE ----------------
    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete a permission")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        if (this.permissionService.fetchById(id) == null) {
            throw new IdInvalidException("Permission với id = " + id + " không tồn tại.");
        }
        this.permissionService.delete(id);
        return ResponseEntity.ok().build();
    }

    // ---------------- GET LIST ----------------
    @GetMapping("/permissions")
    @ApiMessage("Fetch permissions")
    public ResponseEntity<ResultPaginationDTO> getPermissions(
            @Filter Specification<Permission> spec, Pageable pageable) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);
        long idRole = user.getRole().getId();

        System.out.println("ID role: " + idRole);
        System.out.println("Username: " + username);

        long countPermissionsByRoleId = roleService.countPermissionsByRoleId(idRole);
        System.out.println("Số lượng permission trong role: " + countPermissionsByRoleId);

        boolean permissionVsRole = roleService.permissionVsRole(idRole);
        System.out.println("Kiểm tra quyền của role: " + permissionVsRole);

        // Nếu là admin thì xem toàn bộ
        if (permissionVsRole) {
            return ResponseEntity.ok(this.permissionService.getPermissions(spec, pageable));
        } else {
            // Người dùng thường chỉ xem permission thuộc role của mình
            ResultPaginationDTO result = this.permissionService.getPermissionsByRoleId(idRole, pageable);
            return ResponseEntity.ok(result);
        }
    }
}
