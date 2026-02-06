package com.anonLove.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListResponse {
    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("last_message")
    private String lastMessage;

    @JsonProperty("last_message_at")
    private LocalDateTime lastMessageAt;

    @JsonProperty("unread_count")
    private long unreadCount;

    @JsonProperty("post_info")
    private PostInfo postInfo;

    @Getter
    @Builder
    public static class PostInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("title")
        private String title;
    }
}
