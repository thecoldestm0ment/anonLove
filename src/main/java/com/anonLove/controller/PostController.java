package com.anonLove.controller;

import com.anonLove.dto.request.post.CreatePostRequest;
import com.anonLove.dto.request.post.UpdatePostRequest;
import com.anonLove.dto.response.comment.CommentResponse;
import com.anonLove.dto.response.post.CreatePostResponse;
import com.anonLove.dto.response.post.PostDetailResponse;
import com.anonLove.dto.response.post.PostListResponse;
import com.anonLove.security.CustomUserDetails;
import com.anonLove.service.comment.CommentService;
import com.anonLove.service.post.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPosts(
            @RequestParam(required = false) Integer categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<PostListResponse> response = postService.getPosts(categoryId, userDetails.getUserId(), pageable);

        // 첫 번째 게시글 디버그
        if (response.hasContent()) {
            PostListResponse firstPost = response.getContent().get(0);
            log.info("=== Post List Debug ===");
            log.info("Post ID: {}", firstPost.getId());
            log.info("Created At: {}", firstPost.getCreatedAt());
            log.info("Created At Type: {}", firstPost.getCreatedAt() != null ? firstPost.getCreatedAt().getClass() : "NULL");

            try {
                String json = objectMapper.writeValueAsString(firstPost);
                log.info("=== JSON Response (First Post) ===");
                log.info(json);
            } catch (Exception e) {
                log.error("Failed to serialize to JSON", e);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PostDetailResponse response = postService.getPostDetail(postId, userDetails.getUserId());

        // 디버그 로그
        log.info("=== Post Detail Debug ===");
        log.info("Post ID: {}", response.getId());
        log.info("Created At: {}", response.getCreatedAt());
        log.info("Created At Type: {}", response.getCreatedAt() != null ? response.getCreatedAt().getClass() : "NULL");

        try {
            String json = objectMapper.writeValueAsString(response);
            log.info("=== JSON Response ===");
            log.info(json);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON", e);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreatePostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CreatePostResponse response = postService.createPost(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        postService.updatePost(postId, request, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        postService.deletePost(postId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
