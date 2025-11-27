package vn.nhom11.jobhunter.domain.NotificationDTO;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class NotificationDTO {

    private String createdBy;

    @JsonSerialize(using = InstantToStringSerializer.class)
    private Instant createdAt;

    private String updatedBy;

    @JsonSerialize(using = InstantToStringSerializer.class)
    private Instant updatedAt;

    private String message;

    public NotificationDTO(String createdBy, Instant createdAt, String updatedBy, Instant updatedAt, String message) {
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.message = message;
    }

    // Custom serializer
    public static class InstantToStringSerializer extends StdSerializer<Instant> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        public InstantToStringSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(FORMATTER.format(value));
            }
        }
    }
}
