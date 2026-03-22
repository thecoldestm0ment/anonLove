package com.anonLove.controller;

import com.anonLove.dto.request.chat.CreateChatRoomRequest;
import com.anonLove.dto.response.chat.ChatMessageResponse;
import com.anonLove.dto.response.chat.ChatRoomListResponse;
import com.anonLove.dto.response.chat.CreateChatRoomResponse;
import com.anonLove.security.CustomUserDetails;
import com.anonLove.service.chat.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<CreateChatRoomResponse> createChatRoom(
            @Valid @RequestBody CreateChatRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CreateChatRoomResponse response = chatService.createChatRoom(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListResponse>> getChatRoomList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ChatRoomListResponse> response = chatService.getChatRoomList(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) @Positive Long beforeMessageId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ChatMessageResponse> response = chatService.getChatMessages(
                roomId, beforeMessageId, size, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms/{roomId}/messages/recent")
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(
            @PathVariable Long roomId,
            @RequestParam @Positive Long afterMessageId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ChatMessageResponse> response = chatService.getRecentMessages(
                roomId, afterMessageId, size, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        chatService.leaveChatRoom(roomId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}
