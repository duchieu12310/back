package vn.nhom11.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.nhom11.jobhunter.domain.Message;
import vn.nhom11.jobhunter.domain.response.Message.ResMessageDTO;
import vn.nhom11.jobhunter.service.MessageService;
import vn.nhom11.jobhunter.service.UserService;
import vn.nhom11.jobhunter.util.annotation.ApiMessage;
import vn.nhom11.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    // ✅ DTO nhận dữ liệu JSON
    public static class SendMessageRequest {
        @NotNull(message = "senderId không được để trống")
        public Long senderId;

        @NotNull(message = "receiverId không được để trống")
        public Long receiverId;

        @NotBlank(message = "Nội dung tin nhắn không được để trống")
        public String content;
    }

    /**
     * ✅ Gửi tin nhắn giữa 2 user (nhập bằng JSON)
     */
    @PostMapping("")
    @ApiMessage("Gửi tin nhắn giữa 2 người dùng")
    public ResponseEntity<ResMessageDTO> sendMessage(@Valid @RequestBody SendMessageRequest request)
            throws IdInvalidException {

        Message savedMessage = messageService.handleSendMessage(
                request.senderId,
                request.receiverId,
                request.content);

        ResMessageDTO res = messageService.convertToResMessageDTO(savedMessage);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * ✅ Lấy tin nhắn theo ID
     */
    @GetMapping("/{id}")
    @ApiMessage("Lấy tin nhắn theo ID")
    public ResponseEntity<ResMessageDTO> getMessageById(@PathVariable("id") Long id)
            throws IdInvalidException {

        Message message = messageService.fetchMessageById(id);
        ResMessageDTO res = messageService.convertToResMessageDTO(message);
        return ResponseEntity.ok(res);
    }

    /**
     * ✅ Xóa tin nhắn
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa tin nhắn theo ID")
    public ResponseEntity<Void> deleteMessage(@PathVariable("id") Long id)
            throws IdInvalidException {

        messageService.handleDeleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ Lấy danh sách tin nhắn giữa 2 user
     */
    @GetMapping("/conversation")
    @ApiMessage("Lấy danh sách tin nhắn giữa 2 người dùng")
    public ResponseEntity<List<ResMessageDTO>> getConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) throws IdInvalidException {

        List<Message> messages = messageService.fetchConversation(user1Id, user2Id);
        List<ResMessageDTO> resList = messages.stream()
                .map(messageService::convertToResMessageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resList);
    }
}
