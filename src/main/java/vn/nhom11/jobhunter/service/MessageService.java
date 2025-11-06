package vn.nhom11.jobhunter.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.nhom11.jobhunter.domain.Message;
import vn.nhom11.jobhunter.domain.User;
import vn.nhom11.jobhunter.domain.response.Message.ResMessageDTO;
import vn.nhom11.jobhunter.repository.MessageRepository;
import vn.nhom11.jobhunter.util.error.IdInvalidException;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;

    public MessageService(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    /**
     * Gửi tin nhắn giữa 2 user
     */
    public Message handleSendMessage(Long senderId, Long receiverId, String content) throws IdInvalidException {
        User sender = this.userService.fetchUserById(senderId);
        User receiver = this.userService.fetchUserById(receiverId);

        if (sender == null) {
            throw new IdInvalidException("Người gửi với id = " + senderId + " không tồn tại");
        }

        if (receiver == null) {
            throw new IdInvalidException("Người nhận với id = " + receiverId + " không tồn tại");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);

        return this.messageRepository.save(message);
    }

    /**
     * Lấy danh sách tin nhắn giữa 2 user
     */
    public List<Message> fetchConversation(Long user1Id, Long user2Id) throws IdInvalidException {
        User user1 = this.userService.fetchUserById(user1Id);
        User user2 = this.userService.fetchUserById(user2Id);

        if (user1 == null || user2 == null) {
            throw new IdInvalidException("Một trong hai user không tồn tại");
        }

        List<Message> messages = new ArrayList<>();
        messages.addAll(this.messageRepository.findBySenderAndReceiverOrderByCreatedAtAsc(user1, user2));
        messages.addAll(this.messageRepository.findBySenderAndReceiverOrderByCreatedAtAsc(user2, user1));

        // Sắp xếp theo thời gian gửi
        messages.sort(Comparator.comparing(Message::getCreatedAt));

        return messages;
    }

    /**
     * Lấy tin nhắn theo id
     */
    public Message fetchMessageById(Long id) throws IdInvalidException {
        Optional<Message> optionalMessage = this.messageRepository.findById(id);
        if (optionalMessage.isEmpty()) {
            throw new IdInvalidException("Tin nhắn với id = " + id + " không tồn tại");
        }
        return optionalMessage.get();
    }

    /**
     * Xóa tin nhắn
     */
    public void handleDeleteMessage(Long id) throws IdInvalidException {
        Message message = this.fetchMessageById(id);
        if (message == null) {
            throw new IdInvalidException("Tin nhắn với id = " + id + " không tồn tại");
        }
        this.messageRepository.deleteById(id);
    }

    /**
     * Convert sang DTO để trả ra client
     */
    public ResMessageDTO convertToResMessageDTO(Message message) {
        ResMessageDTO res = new ResMessageDTO();
        ResMessageDTO.UserMessage senderDTO = new ResMessageDTO.UserMessage();
        ResMessageDTO.UserMessage receiverDTO = new ResMessageDTO.UserMessage();

        if (message.getSender() != null) {
            senderDTO.setId(message.getSender().getId());
            senderDTO.setEmail(message.getSender().getEmail());
            senderDTO.setName(message.getSender().getName());
        }

        if (message.getReceiver() != null) {
            receiverDTO.setId(message.getReceiver().getId());
            receiverDTO.setEmail(message.getReceiver().getEmail());
            receiverDTO.setName(message.getReceiver().getName());
        }

        res.setId(message.getId());
        res.setContent(message.getContent());
        res.setCreatedAt(message.getCreatedAt());
        res.setUpdatedAt(message.getUpdatedAt());
        res.setSender(senderDTO);
        res.setReceiver(receiverDTO);

        return res;
    }
}
