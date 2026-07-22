package com.ycyw.chat_poc.repository;

import com.ycyw.chat_poc.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySupportTicket_IdOrderBySentAtAsc(Long supportTicketId);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.supportTicket.id = :ticketId
            AND m.readAt IS NULL
            AND m.senderPseudo != :pseudo
            """)
    int countUnread(@Param("ticketId") long ticketId, @Param("pseudo") String pseudo);

    @Modifying
    @Query("""
            UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP
            WHERE m.supportTicket.id = :ticketId
            AND m.senderPseudo != :readerPseudo
            AND m.readAt IS NULL
            """)
    void markAllAsRead(@Param("ticketId") long ticketId, @Param("readerPseudo") String readerPseudo);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE (m.supportTicket.userPseudo = :pseudo OR m.supportTicket.agentPseudo = :pseudo)
            AND m.senderPseudo != :pseudo
            AND m.readAt IS NULL
            """)
    int countTotalUnread(@Param("pseudo") String pseudo);
}
