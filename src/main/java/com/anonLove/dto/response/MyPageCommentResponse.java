package com.anonLove.dto.response;

import com.anonLove.domain.comment.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyPageCommentResponse {
    private Long id;
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("post_info")
    private PostInfo post;

    @Getter
    @Builder
    public static class PostInfo {
        private Long id;
        private String title;
        private String category;
    }

    public static MyPageCommentResponse from(Comment comment) {
        return MyPageCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .post(PostInfo.builder()
                        .id(comment.getPost().getId())
                        .title(comment.getPost().getTitle())
                        .category(comment.getPost().getCategory() != null ? comment.getPost().getCategory().getName()
                                : null)
                        .build())
                .build();
    }
}
