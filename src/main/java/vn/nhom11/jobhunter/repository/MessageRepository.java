package vn.nhom11.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.nhom11.jobhunter.domain.Message;
import vn.nhom11.jobhunter.domain.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiverOrderByCreatedAtAsc(User sender, User receiver);

    List<Message> findByReceiverAndSenderOrderByCreatedAtAsc(User receiver, User sender);

}
