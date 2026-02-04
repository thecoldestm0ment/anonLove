package com.anonLove.dto.response.post;

import com.anonLove.domain.post.Post;
import com.anonLove.dto.response.comment.CommentResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String category;

    @JsonProperty("is_mine")
    private boolean isMine;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private List<CommentResponse> comments;

    public static PostDetailResponse from(Post post, boolean isMine, List<CommentResponse> comments) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory() != null ? post.getCategory().getName() : null)
                .isMine(isMine)
                .createdAt(post.getCreatedAt())
                .comments(comments)
                .build();
    }
}
