package vn.nhom11.jobhunter.domain.response.Message;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMessageDTO {
    private Long id;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private UserMessage sender;
    private UserMessage receiver;

    @Getter
    @Setter
    public static class UserMessage {
        private Long id;
        private String name;
        private String email;
    }
}
