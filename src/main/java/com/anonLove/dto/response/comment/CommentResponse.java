package com.anonLove.dto.response.comment;

import com.anonLove.domain.comment.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String content;

    @JsonProperty("is_filtered")
    private boolean isFiltered;

    @JsonProperty("is_mine")
    private boolean isMine;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment, Long viewerId) {
        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .isFiltered(comment.isFiltered())
                .isMine(comment.getUser().getId().equals(viewerId))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
