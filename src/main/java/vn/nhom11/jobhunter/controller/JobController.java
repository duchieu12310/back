package vn.nhom11.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.nhom11.jobhunter.domain.Job;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.nhom11.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.nhom11.jobhunter.service.JobService;
import vn.nhom11.jobhunter.service.RoleService;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;
import vn.nhom11.jobhunter.util.error.IdInvalidException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final JobService jobService;
    private final UserService userService;

    private final RoleService roleService;

    public JobController(JobService jobService, UserService userService, RoleService roleService) {
        this.jobService = jobService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create a job")
    public ResponseEntity<ResCreateJobDTO> create(@Valid @RequestBody Job job) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.jobService.create(job));
    }

    @PutMapping("/jobs")
    @ApiMessage("Update a job")
    public ResponseEntity<ResUpdateJobDTO> update(@Valid @RequestBody Job job) throws IdInvalidException {
        Optional<Job> currentJob = this.jobService.fetchJobById(job.getId());
        if (!currentJob.isPresent()) {
            throw new IdInvalidException("Job not found");
        }

        return ResponseEntity.ok()
                .body(this.jobService.update(job, currentJob.get()));
    }

    @DeleteMapping("/jobs/{id}")
    @ApiMessage("Delete a job by id")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> currentJob = this.jobService.fetchJobById(id);
        if (!currentJob.isPresent()) {
            throw new IdInvalidException("Job not found");
        }
        this.jobService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/jobs/{id}")
    @ApiMessage("Get a job by id")
    public ResponseEntity<Job> getJob(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> currentJob = this.jobService.fetchJobById(id);
        if (!currentJob.isPresent()) {
            throw new IdInvalidException("Job not found");
        }

        return ResponseEntity.ok().body(currentJob.get());
    }

    @GetMapping("/jobs")
    @ApiMessage("Get jobs with pagination")
    public ResponseEntity<ResultPaginationDTO> getAllJob(
            @Filter Specification<Job> spec,
            Pageable pageable) {

        // Lấy thông tin user hiện tại từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.handleGetUserByUsername(username);
        long idRole = user.getRole().getId();

        System.out.println("ID của role: " + idRole);
        System.out.println("ID user: " + user.getId());
        System.out.println("Username: " + username);

        ResultPaginationDTO result;

        // Đếm quyền của role hiện tại
        long countPermissionsByRoleId = roleService.countPermissionsByRoleId(idRole);
        System.out.println("Số quyền của role: " + countPermissionsByRoleId);

        boolean permissionVsRole = roleService.permissionVsRole(idRole);
        System.out.println("Role có toàn quyền hay không: " + permissionVsRole);

        if (permissionVsRole) {
            // Admin xem tất cả job
            result = this.jobService.fetchAll(spec, pageable);
        } else {
            // User thường chỉ xem job do mình tạo
            result = this.jobService.fetchJobsByCreatedBy(username, pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobs/all")
    @ApiMessage("Get all jobs (ignore role check)")
    public ResponseEntity<ResultPaginationDTO> getAllJobsUnfiltered(
            @Filter Specification<Job> spec,
            Pageable pageable) {
        ResultPaginationDTO result = this.jobService.fetchAll(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobs/by-created/{username}")
    @ApiMessage("Get jobs by createdBy")
    public ResponseEntity<ResultPaginationDTO> getJobsByCreatedBy(
            @PathVariable("username") String username,
            Pageable pageable) {

        return ResponseEntity.ok()
                .body(this.jobService.fetchJobsByCreatedBy(username, pageable));
    }

}
