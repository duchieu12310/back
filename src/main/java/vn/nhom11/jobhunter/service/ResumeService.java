package vn.nhom11.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
        return resumeRepository.findById(id);
    }

   
    public boolean checkResumeExistByUserAndJob(Resume resume) {
        return resume.getUser() != null &&
                resume.getJob() != null &&
                userRepository.existsById(resume.getUser().getId()) &&
                jobRepository.existsById(resume.getJob().getId());
    }

    public ResCreateResumeDTO create(Resume resume) {
        resume = resumeRepository.save(resume);

        ResCreateResumeDTO dto = new ResCreateResumeDTO();
        dto.setId(resume.getId());
        dto.setCreatedBy(resume.getCreatedBy());
        dto.setCreatedAt(resume.getCreatedAt());
        return dto;
    }

    public ResUpdateResumeDTO update(Resume resume) {
        resume = resumeRepository.save(resume);

        ResUpdateResumeDTO dto = new ResUpdateResumeDTO();
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setUpdatedBy(resume.getUpdatedBy());
        return dto;
    }

    public void delete(long id) {
        resumeRepository.deleteById(id);
    }

    
    public ResFetchResumeDTO getResume(Resume resume) {
        ResFetchResumeDTO dto = new ResFetchResumeDTO();

        dto.setId(resume.getId());
        dto.setEmail(resume.getEmail());
        dto.setUrl(resume.getUrl());
        dto.setStatus(resume.getStatus());
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setCreatedBy(resume.getCreatedBy());
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setUpdatedBy(resume.getUpdatedBy());

        if (resume.getUser() != null) {
            dto.setUser(new ResFetchResumeDTO.UserResume(
                    resume.getUser().getId(),
                    resume.getUser().getName()));
        }

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

            dto.setJob(new ResFetchResumeDTO.JobResume(
                    job.getId(),
                    job.getName(),
                    job.getLocation(),
                    job.getSalary(),
                    job.getLevel() != null ? job.getLevel().name() : null,
                    companyDTO));
        }

        return dto;
    }

   
    private ResultPaginationDTO buildPagination(Page<Resume> page, Pageable pageable) {
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ResultPaginationDTO dto = new ResultPaginationDTO();
        dto.setMeta(meta);
        dto.setResult(page.getContent().stream().map(this::getResume).collect(Collectors.toList()));
        return dto;
    }

    public ResultPaginationDTO fetchResumesByCompanyId(long companyId, Pageable pageable) {
        Specification<Resume> spec =
                (root, query, cb) -> cb.equal(root.get("job").get("company").get("id"), companyId);

        Page<Resume> page = resumeRepository.findAll(spec, pageable);
        return buildPagination(page, pageable);
    }

    public ResultPaginationDTO fetchAllResume(Specification<Resume> spec, Pageable pageable) {
        Page<Resume> page = resumeRepository.findAll(spec, pageable);
        return buildPagination(page, pageable);
    }

    public ResUpdateResumeDTO updateStatus(long resumeId, ResumeStateEnum newStatus, String note) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ id = " + resumeId));

        resume.setStatus(newStatus);

        if (newStatus == ResumeStateEnum.APPROVED)
            resume.setNote(note == null || note.isBlank() ? "Được chấp nhận." : note);
        else if (newStatus == ResumeStateEnum.REJECTED)
            resume.setNote(note == null || note.isBlank() ? "Không đạt yêu cầu." : note);
        else
            resume.setNote(note);

        resumeRepository.save(resume);

        ResUpdateResumeDTO dto = new ResUpdateResumeDTO();
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setUpdatedBy(resume.getUpdatedBy());
        return dto;
    }

    /** Rút gọn fetch theo user */
    public ResultPaginationDTO fetchResumeByUser(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        FilterNode node = filterParser.parse("email='" + email + "'");
        FilterSpecification<Resume> spec = filterSpecificationConverter.convert(node);

        Page<Resume> page = resumeRepository.findAll(spec, pageable);
        return buildPagination(page, pageable);
    }
}
