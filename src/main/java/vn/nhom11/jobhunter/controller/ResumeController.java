package vn.nhom11.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.nhom11.jobhunter.domain.Resume;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.nhom11.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.nhom11.jobhunter.service.ResumeService;
import vn.nhom11.jobhunter.service.RoleService;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;
import vn.nhom11.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserService userService;
    private final RoleService roleService;

    public ResumeController(ResumeService resumeService, UserService userService, RoleService roleService) {
        this.resumeService = resumeService;
        this.userService = userService;
        this.roleService = roleService;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.handleGetUserByUsername(auth.getName());
    }

    @PostMapping("/resumes")
    public ResponseEntity<?> create(@RequestBody Resume resume) throws IdInvalidException {

        if (!resumeService.checkResumeExistByUserAndJob(resume)) {
            throw new IdInvalidException("User/Job không tồn tại");
        }

        return ResponseEntity.ok(resumeService.create(resume));
    }

    @PutMapping("/resumes/{id}")
    public ResponseEntity<?> updateResume(@PathVariable long id, @RequestBody Resume data) throws IdInvalidException {

        User user = currentUser();
        if (roleService.permissionVsRole(user.getRole().getId())) {
            return ResponseEntity.status(403).body("Admin không được cập nhật resume");
        }

        Resume resume = resumeService.fetchById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy resume"));

        if (data.getStatus() != null) resume.setStatus(data.getStatus());
        if (data.getNote() != null) resume.setNote(data.getNote());

        return ResponseEntity.ok(resumeService.update(resume));
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) throws IdInvalidException {

        resumeService.fetchById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy resume"));

        resumeService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resumes")
    public ResponseEntity<?> getResumesByCompany(
            @Filter Specification<Resume> spec,
            Pageable pageable) {

        User user = currentUser();

        if (roleService.permissionVsRole(user.getRole().getId())) {
            return ResponseEntity.ok(resumeService.fetchAllResume(spec, pageable));
        }

        if (user.getCompany() == null) {
            return ResponseEntity.ok(new ResultPaginationDTO());
        }

        return ResponseEntity.ok(
                resumeService.fetchResumesByCompanyId(user.getCompany().getId(), pageable)
        );
    }
}
