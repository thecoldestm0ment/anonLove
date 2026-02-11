package com.anonLove.dto.response.chat;

import com.anonLove.domain.chat.ChatMessage;
import com.anonLove.domain.chat.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long id;

    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("message_type")
    private MessageType messageType;

    private String content;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("is_read")
    private boolean isRead;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .fileUrl(message.getFileUrl())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}