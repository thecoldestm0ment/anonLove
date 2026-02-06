package com.anonLove.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateChatRoomRequest {

    @NotNull(message = "Post ID is required")
    @JsonProperty("post_id")
    private Long postId;

    @NotNull(message = "Comment ID is required")
    @JsonProperty("comment_id")
    private Long commentId;

    @NotNull(message = "Receiver ID is required")
    @JsonProperty("receiver_id")
    private Long receiverId;
}
