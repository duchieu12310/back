package vn.nhom11.jobhunter.domain.response.resume;

import java.time.Instant;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vn.nhom11.jobhunter.util.constant.ResumeStateEnum;

@Getter
@Setter
public class ResFetchResumeDTO {
    private long id;
    private String email;
    private String url;

    @Enumerated(EnumType.STRING)
    private ResumeStateEnum status;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    private UserResume user;
    private JobResume job;

    // --- Inner DTO classes ---
    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserResume {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class JobResume {
        private long id;
        private String name;
        private String location;
        private double salary;
        private String level;
        private CompanyResume company; // ← thêm company vào job
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CompanyResume {
        private long id;
        private String name;
        private String address;
        private String logo;
        private String description;
    }
}
