package com.ycyw.chat_poc.repository;

import com.ycyw.chat_poc.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findBySupportTicket_IdOrderBySentAtAsc(Long supportTicketId);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.supportTicket.id = :ticketId
            AND m.readAt  IS NULL
            AND m.isFromAgent = :fromAgent
            """)
    int countUnread(@Param("ticketId") long ticketId, @Param("fromAgent") boolean fromAgent);

    @Modifying
    @Query("""
            UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP
            WHERE m.supportTicket.id = :ticketId
            AND m.isFromAgent = :fromAgent
            AND m.readAt IS NULL
            """)
    void markAllAsRead(@Param("ticketId") long ticketId, @Param("fromAgent") boolean fromAgent);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.supportTicket.userPseudo = :pseudo
            AND m.isFromAgent = true
            AND m.readAt IS NULL
            """)
    int countTotalUnreadForUser(@Param("pseudo") String pseudo);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.supportTicket.agentPseudo = :pseudo
            AND m.isFromAgent = false
            AND m.readAt IS NULL
            """)
    int countTotalUnreadForAgent(@Param("pseudo") String pseudo);
}
