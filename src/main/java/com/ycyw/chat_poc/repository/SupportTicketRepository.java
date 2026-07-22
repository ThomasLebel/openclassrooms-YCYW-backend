package com.ycyw.chat_poc.repository;

import com.ycyw.chat_poc.models.ConversationStatus;
import com.ycyw.chat_poc.models.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByStatusOrderByCreatedAtAsc(ConversationStatus status);

    @Query("SELECT t FROM SupportTicket t WHERE t.userPseudo = :pseudo OR t.agentPseudo = :pseudo ORDER BY t.updatedAt DESC")
    List<SupportTicket> findByUserPseudoOrAgentPseudo(@Param("pseudo") String pseudo);
}
