package vn.nhom11.jobhunter.controller;

import java.util.Optional;

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
import vn.nhom11.jobhunter.domain.Company;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.service.CompanyService;
import vn.nhom11.jobhunter.service.RoleService;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {
    private final CompanyService companyService;
    private final UserService userService;
    private final RoleService roleService;

    public CompanyController(CompanyService companyService, UserService userService, RoleService roleService) {
        this.companyService = companyService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostMapping("/companies")
    @ApiMessage("Create company")
    public ResponseEntity<?> createCompany(@Valid @RequestBody Company reqCompany) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.companyService.handleCreateCompany(reqCompany));
    }

    @GetMapping("/companies")
    @ApiMessage("Fetch companies with pagination")
    public ResponseEntity<ResultPaginationDTO> getCompany(
            @Filter Specification<Company> spec, Pageable pageable) {

        // Lấy thông tin user hiện tại từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);
        long idRole = user.getRole().getId();
        System.out.println("ID cua role " + idRole);
        System.out.println(user.getId());
        System.out.println(username);
        ResultPaginationDTO result;
        long countPermissionsByRoleId = roleService.countPermissionsByRoleId(idRole);
        System.out.println(countPermissionsByRoleId);
        boolean permissionVsrole = roleService.permissionVsRole(idRole);
        System.out.println("312312312312312312313");
        System.out.println(permissionVsrole);

        if (permissionVsrole) {
            // Admin xem tất cả
            result = this.companyService.handleGetCompany(spec, pageable);
        } else {
            // User thường chỉ xem company do mình tạo
            result = this.companyService.fetchCompanyById(user.getCompany().getId(), pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/companies/all")
    @ApiMessage("Fetch all companies (admin only)")
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(
            @Filter Specification<Company> spec, Pageable pageable) {
        ResultPaginationDTO result = this.companyService.handleGetCompany(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/companies")
    @ApiMessage("Update company")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company reqCompany) {
        Company updatedCompany = this.companyService.handleUpdateCompany(reqCompany);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/companies/{id}")
    @ApiMessage("Delete company")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/companies/{id}")
    @ApiMessage("Fetch company by id")
    public ResponseEntity<Company> fetchCompanyById(@PathVariable("id") long id) {
        Optional<Company> cOptional = this.companyService.findById(id);
        return ResponseEntity.ok().body(cOptional.get());
    }
}
