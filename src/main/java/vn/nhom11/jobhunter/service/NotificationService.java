package vn.nhom11.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.nhom11.jobhunter.domain.NotificationDTO.NotificationDTO;
import vn.nhom11.jobhunter.repository.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final PermissionRepository permissionRepository;
    private final ResumeRepository resumeRepository;
    private final RoleRepository roleRepository;
    private final SubscriberRepository subscriberRepository;
    private final CompanyRegistrationRepository companyRegistrationRepository;

    private static final ZoneId ZONE = ZoneId.systemDefault();

    public List<NotificationDTO> getAllNotificationsIn24h() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from24h = now.minusHours(24);

        List<NotificationDTO> notifications = new ArrayList<>();

        addCompany(notifications, from24h);
        addJob(notifications, from24h);
        addPermission(notifications, from24h);
        addResume(notifications, from24h);
        addRole(notifications, from24h);
        addSubscriber(notifications, from24h);
        addCompanyRegistration(notifications, from24h);

        return notifications;
    }

    // ========================== UTIL ==========================
    private boolean isIn24h(Instant createdAt, Instant updatedAt, LocalDateTime from24h) {
        LocalDateTime created = createdAt != null ? LocalDateTime.ofInstant(createdAt, ZONE) : null;
        LocalDateTime updated = updatedAt != null ? LocalDateTime.ofInstant(updatedAt, ZONE) : null;

        boolean c = created != null && created.isAfter(from24h);
        boolean u = updated != null && updated.isAfter(from24h);
        return c || u;
    }

    private String buildMessage(String type, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            return "Đã cập nhật " + type + " bởi " + updatedBy;
        }
        return "Đã tạo mới " + type + " bởi " + createdBy;
    }

    // ========================== 1. COMPANY ==========================
    private void addCompany(List<NotificationDTO> list, LocalDateTime from24h) {
        companyRepository.findAll().forEach(c -> {
            if (isIn24h(c.getCreatedAt(), c.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        c.getCreatedBy(),
                        c.getCreatedAt(),
                        c.getUpdatedBy(),
                        c.getUpdatedAt(),
                        buildMessage("Công ty", c.getCreatedAt(), c.getUpdatedAt(), c.getCreatedBy(),
                                c.getUpdatedBy())));
            }
        });
    }

    // ========================== 2. JOB ==========================
    private void addJob(List<NotificationDTO> list, LocalDateTime from24h) {
        jobRepository.findAll().forEach(j -> {
            if (isIn24h(j.getCreatedAt(), j.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        j.getCreatedBy(),
                        j.getCreatedAt(),
                        j.getUpdatedBy(),
                        j.getUpdatedAt(),
                        buildMessage("Công việc", j.getCreatedAt(), j.getUpdatedAt(), j.getCreatedBy(),
                                j.getUpdatedBy())));
            }
        });
    }

    // ========================== 3. PERMISSION ==========================
    private void addPermission(List<NotificationDTO> list, LocalDateTime from24h) {
        permissionRepository.findAll().forEach(p -> {
            if (isIn24h(p.getCreatedAt(), p.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        p.getCreatedBy(),
                        p.getCreatedAt(),
                        p.getUpdatedBy(),
                        p.getUpdatedAt(),
                        buildMessage("Quyền", p.getCreatedAt(), p.getUpdatedAt(), p.getCreatedBy(), p.getUpdatedBy())));
            }
        });
    }

    // ========================== 4. RESUME ==========================
    private void addResume(List<NotificationDTO> list, LocalDateTime from24h) {
        resumeRepository.findAll().forEach(r -> {
            if (isIn24h(r.getCreatedAt(), r.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        r.getCreatedBy(),
                        r.getCreatedAt(),
                        r.getUpdatedBy(),
                        r.getUpdatedAt(),
                        buildMessage("Hồ sơ", r.getCreatedAt(), r.getUpdatedAt(), r.getCreatedBy(), r.getUpdatedBy())));
            }
        });
    }

    // ========================== 5. ROLE ==========================
    private void addRole(List<NotificationDTO> list, LocalDateTime from24h) {
        roleRepository.findAll().forEach(r -> {
            if (isIn24h(r.getCreatedAt(), r.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        r.getCreatedBy(),
                        r.getCreatedAt(),
                        r.getUpdatedBy(),
                        r.getUpdatedAt(),
                        buildMessage("Vai trò", r.getCreatedAt(), r.getUpdatedAt(), r.getCreatedBy(),
                                r.getUpdatedBy())));
            }
        });
    }

    // ========================== 6. SUBSCRIBER ==========================
    private void addSubscriber(List<NotificationDTO> list, LocalDateTime from24h) {
        subscriberRepository.findAll().forEach(s -> {
            if (isIn24h(s.getCreatedAt(), s.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        s.getCreatedBy(),
                        s.getCreatedAt(),
                        s.getUpdatedBy(),
                        s.getUpdatedAt(),
                        buildMessage("Người đăng ký", s.getCreatedAt(), s.getUpdatedAt(), s.getCreatedBy(),
                                s.getUpdatedBy())));
            }
        });
    }

    // ========================== 7. COMPANY REGISTRATION ==========================
    private void addCompanyRegistration(List<NotificationDTO> list, LocalDateTime from24h) {
        companyRegistrationRepository.findAll().forEach(cr -> {
            if (isIn24h(cr.getCreatedAt(), cr.getUpdatedAt(), from24h)) {
                list.add(new NotificationDTO(
                        cr.getCreatedBy(),
                        cr.getCreatedAt(),
                        cr.getUpdatedBy(),
                        cr.getUpdatedAt(),
                        buildMessage("Yêu cầu đăng ký công ty", cr.getCreatedAt(), cr.getUpdatedAt(), cr.getCreatedBy(),
                                cr.getUpdatedBy())));
            }
        });
    }
}
