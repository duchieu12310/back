package vn.nhom11.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.node.FilterNode;

import vn.nhom11.jobhunter.domain.Company;
import vn.nhom11.jobhunter.domain.Job;
import vn.nhom11.jobhunter.domain.Resume;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.ResultPaginationDTO;
import vn.nhom11.jobhunter.domain.response.resume.ResCreateResumeDTO;
import vn.nhom11.jobhunter.domain.response.resume.ResFetchResumeDTO;
import vn.nhom11.jobhunter.domain.response.resume.ResUpdateResumeDTO;
import vn.nhom11.jobhunter.repository.JobRepository;
import vn.nhom11.jobhunter.repository.ResumeRepository;
import vn.nhom11.jobhunter.repository.UserRepository;
import vn.nhom11.jobhunter.util.SecurityUtil;
import vn.nhom11.jobhunter.util.constant.ResumeStateEnum;

@Service
public class ResumeService {
    @Autowired
    FilterBuilder fb;

    @Autowired
    private FilterParser filterParser;

    @Autowired
    private FilterSpecificationConverter filterSpecificationConverter;

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            JobRepository jobRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public Optional<Resume> fetchById(long id) {
        return this.resumeRepository.findById(id);
    }

    public boolean checkResumeExistByUserAndJob(Resume resume) {
        if (resume.getUser() == null)
            return false;
        Optional<User> userOptional = this.userRepository.findById(resume.getUser().getId());
        if (userOptional.isEmpty())
            return false;

        if (resume.getJob() == null)
            return false;
        Optional<Job> jobOptional = this.jobRepository.findById(resume.getJob().getId());
        if (jobOptional.isEmpty())
            return false;

        return true;
    }

    public ResCreateResumeDTO create(Resume resume) {
        resume = this.resumeRepository.save(resume);

        ResCreateResumeDTO res = new ResCreateResumeDTO();
        res.setId(resume.getId());
        res.setCreatedBy(resume.getCreatedBy());
        res.setCreatedAt(resume.getCreatedAt());
        return res;
    }

    public ResUpdateResumeDTO update(Resume resume) {
        resume = this.resumeRepository.save(resume);
        ResUpdateResumeDTO res = new ResUpdateResumeDTO();
        res.setUpdatedAt(resume.getUpdatedAt());
        res.setUpdatedBy(resume.getUpdatedBy());
        return res;
    }

    public void delete(long id) {
        this.resumeRepository.deleteById(id);
    }

    /**
     * ✅ Hàm ánh xạ đầy đủ Resume → ResFetchResumeDTO
     */
    public ResFetchResumeDTO getResume(Resume resume) {
        ResFetchResumeDTO res = new ResFetchResumeDTO();
        res.setId(resume.getId());
        res.setEmail(resume.getEmail());
        res.setUrl(resume.getUrl());
        res.setStatus(resume.getStatus());
        res.setCreatedAt(resume.getCreatedAt());
        res.setCreatedBy(resume.getCreatedBy());
        res.setUpdatedAt(resume.getUpdatedAt());
        res.setUpdatedBy(resume.getUpdatedBy());

        // User
        if (resume.getUser() != null) {
            res.setUser(new ResFetchResumeDTO.UserResume(
                    resume.getUser().getId(),
                    resume.getUser().getName()));
        }

        // Job + Company
        if (resume.getJob() != null) {
            Job job = resume.getJob();
            Company company = job.getCompany();

            ResFetchResumeDTO.CompanyResume companyDTO = null;
            if (company != null) {
                companyDTO = new ResFetchResumeDTO.CompanyResume(
                        company.getId(),
                        company.getName(),
                        company.getAddress(),
                        company.getLogo(),
                        company.getDescription());
            }

            ResFetchResumeDTO.JobResume jobDTO = new ResFetchResumeDTO.JobResume(
                    job.getId(),
                    job.getName(),
                    job.getLocation(),
                    job.getSalary(),
                    job.getLevel() != null ? job.getLevel().name() : null,
                    companyDTO);

            res.setJob(jobDTO);
        }

        return res;
    }

    public ResultPaginationDTO fetchResumesByCompanyId(long companyId, Pageable pageable) {
        Specification<Resume> spec = (root, query, cb) -> cb.equal(root.get("job").get("company").get("id"), companyId);
        return this.fetchAllResume(spec, pageable);
    }

    public ResultPaginationDTO fetchAllResume(Specification<Resume> spec, Pageable pageable) {
        Page<Resume> page = this.resumeRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        List<ResFetchResumeDTO> results = page.getContent()
                .stream().map(this::getResume)
                .collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        rs.setMeta(meta);
        rs.setResult(results);
        return rs;
    }

    public ResUpdateResumeDTO updateStatus(long resumeId, ResumeStateEnum newStatus, String note) {
        Optional<Resume> optionalResume = this.resumeRepository.findById(resumeId);
        if (optionalResume.isEmpty()) {
            throw new RuntimeException("Không tìm thấy hồ sơ có id = " + resumeId);
        }

        Resume resume = optionalResume.get();

        if (newStatus == ResumeStateEnum.APPROVED) {
            resume.setStatus(newStatus);
            resume.setNote(note != null && !note.isBlank() ? note : "Đã được chấp nhận. Liên hệ ứng viên qua email.");
        } else if (newStatus == ResumeStateEnum.REJECTED) {
            resume.setStatus(newStatus);
            resume.setNote(note != null && !note.isBlank() ? note : "Không đạt yêu cầu.");
        } else {
            resume.setStatus(newStatus);
            resume.setNote(note);
        }

        resume = this.resumeRepository.save(resume);

        ResUpdateResumeDTO res = new ResUpdateResumeDTO();
        res.setUpdatedAt(resume.getUpdatedAt());
        res.setUpdatedBy(resume.getUpdatedBy());
        return res;
    }

    public ResultPaginationDTO fetchResumeByUser(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        FilterNode node = filterParser.parse("email='" + email + "'");
        FilterSpecification<Resume> spec = filterSpecificationConverter.convert(node);

        Page<Resume> page = this.resumeRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        List<ResFetchResumeDTO> results = page.getContent()
                .stream().map(this::getResume)
                .collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        rs.setMeta(meta);
        rs.setResult(results);
        return rs;
    }
}
