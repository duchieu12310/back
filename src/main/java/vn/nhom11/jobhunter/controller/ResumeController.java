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

    @PostMapping("/resumes")
    @ApiMessage("Create a resume")
    public ResponseEntity<ResCreateResumeDTO> create(@Valid @RequestBody Resume resume) throws IdInvalidException {
        // Kiểm tra userId và jobId có tồn tại không
        boolean isIdExist = this.resumeService.checkResumeExistByUserAndJob(resume);
        if (!isIdExist) {
            throw new IdInvalidException("User id/Job id không tồn tại");
        }

        // Tạo resume mới
        return ResponseEntity.status(HttpStatus.CREATED).body(this.resumeService.create(resume));
    }

    @PutMapping("/resumes/{id}")
    @ApiMessage("Cập nhật trạng thái hoặc ghi chú hồ sơ")
    public ResponseEntity<?> updateResume(
            @PathVariable("id") long id,
            @RequestBody Resume resumeUpdate)
            throws IdInvalidException {

        // ✅ Lấy user hiện tại từ token đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);

        long idRole = user.getRole().getId();
        boolean isAdmin = roleService.permissionVsRole(idRole);

        // ✅ Nếu là admin → báo lỗi, không cho cập nhật
        if (isAdmin) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin không được phép cập nhật trạng thái hoặc ghi chú hồ sơ");
        }

        // ✅ Kiểm tra resume có tồn tại không
        Optional<Resume> resumeOptional = this.resumeService.fetchById(id);
        if (resumeOptional.isEmpty()) {
            throw new IdInvalidException("Resume với id = " + id + " không tồn tại");
        }

        Resume existingResume = resumeOptional.get();

        // ✅ Chỉ cập nhật những trường được gửi lên
        if (resumeUpdate.getStatus() != null)
            existingResume.setStatus(resumeUpdate.getStatus());

        if (resumeUpdate.getNote() != null)
            existingResume.setNote(resumeUpdate.getNote());

        ResUpdateResumeDTO res = this.resumeService.update(existingResume);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/resumes")
    @ApiMessage("Update a resume")
    public ResponseEntity<ResUpdateResumeDTO> update(@RequestBody Resume resume) throws IdInvalidException {
        // Kiểm tra resume có tồn tại không
        Optional<Resume> reqResumeOptional = this.resumeService.fetchById(resume.getId());
        if (reqResumeOptional.isEmpty()) {
            throw new IdInvalidException("Resume với id = " + resume.getId() + " không tồn tại");
        }

        Resume reqResume = reqResumeOptional.get();
        reqResume.setStatus(resume.getStatus());
        return ResponseEntity.ok().body(this.resumeService.update(reqResume));
    }

    @DeleteMapping("/resumes/{id}")
    @ApiMessage("Delete a resume by id")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Resume> reqResumeOptional = this.resumeService.fetchById(id);
        if (reqResumeOptional.isEmpty()) {
            throw new IdInvalidException("Resume với id = " + id + " không tồn tại");
        }

        this.resumeService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resumes")
    @ApiMessage("Fetch resumes by company with pagination")
    public ResponseEntity<ResultPaginationDTO> getResumesByCompany(
            @Filter Specification<Resume> spec,
            Pageable pageable) {

        // ✅ Lấy user hiện tại từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);

        long idRole = user.getRole().getId();
        boolean isAdmin = roleService.permissionVsRole(idRole);

        ResultPaginationDTO result;

        if (isAdmin) {
            // ✅ Admin xem tất cả resumes
            result = this.resumeService.fetchAllResume(spec, pageable);
        } else {
            // ✅ User chỉ xem resume thuộc công ty của mình
            if (user.getCompany() == null) {
                // Không có công ty → danh sách rỗng
                result = new ResultPaginationDTO();
            } else {
                long companyId = user.getCompany().getId();
                result = this.resumeService.fetchResumesByCompanyId(companyId, pageable);
            }
        }

        return ResponseEntity.ok(result);
    }
}
