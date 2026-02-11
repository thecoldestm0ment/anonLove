package com.anonLove.controller;

import com.anonLove.dto.request.chat.SendMessageRequest;
import com.anonLove.dto.response.chat.ChatMessageResponse;
import com.anonLove.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService; // [수정1] final 붙여서 주입받게 함!

    // [수정2] 프론트엔드가 보내는 주소("/chat.send")로 변경
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {

        Long userId = Long.parseLong(principal.getName());
        Long roomId = request.getRoomId();

        log.info("Received message: roomId={}, senderId={}", roomId, userId);

        // DB 저장
        ChatMessageResponse response = chatService.saveMessage(roomId, userId, request);
        messagingTemplate.convertAndSend("/topic/chat.room." + roomId, response);

        log.info("Message sent to subscriber: /topic/chat.room.{}", roomId);
    }
}