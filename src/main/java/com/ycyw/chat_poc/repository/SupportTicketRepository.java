package com.ycyw.chat_poc.repository;

import com.ycyw.chat_poc.models.ConversationStatus;
import com.ycyw.chat_poc.models.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByStatusOrderByCreatedAtAsc(ConversationStatus status);
    List<SupportTicket> findByUserPseudoOrderByUpdatedAtAsc(String userPseudo);
    List<SupportTicket> findByAgentPseudoOrderByUpdatedAtAsc(String agentPseudo);
}
