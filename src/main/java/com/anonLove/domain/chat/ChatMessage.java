package com.anonLove.domain.chat;

import com.anonLove.domain.common.BaseTimeEntity;
import com.anonLove.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_message_room_id_id", columnList = "room_id,id"),
                @Index(name = "idx_chat_message_room_id_is_read_sender_id", columnList = "room_id,is_read,sender_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_url", length = 512)
    private String fileUrl;

    @Column(name = "is_read")
    private boolean isRead;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, MessageType messageType,
                       String content, String fileUrl) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageType = messageType != null ? messageType : MessageType.TEXT;
        this.content = content;
        this.fileUrl = fileUrl;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
