package com.anonLove.repository;

import com.anonLove.domain.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방별 메시지 조회 (최신순)
    Page<ChatMessage> findByChatRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

    // lastMessageId 이전 메시지 조회
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :roomId AND m.id < :lastMessageId " +
            "ORDER BY m.id DESC")
    Page<ChatMessage> findPreviousMessages(@Param("roomId") Long roomId,
                                           @Param("lastMessageId") Long lastMessageId,
                                           Pageable pageable);

    // lastMessageId 이후 메시지 조회
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :roomId AND m.id > :lastMessageId " +
            "ORDER BY m.id ASC")
    Page<ChatMessage> findMessagesAfterId(@Param("roomId") Long roomId,
                                          @Param("lastMessageId") Long lastMessageId,
                                          Pageable pageable);

    // 채팅방의 마지막 메시지
    Optional<ChatMessage> findTopByChatRoomIdOrderByIdDesc(Long roomId);

    // 읽지 않은 메시지 개수
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom.id = :roomId " +
            "AND m.isRead = false " +
            "AND m.sender.id != :userId")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
    // 메시지 읽음 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
            "WHERE m.chatRoom.id = :roomId AND m.sender.id != :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
