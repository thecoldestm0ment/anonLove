package com.anonLove.service.chat;

import com.anonLove.domain.chat.ChatMessage;
import com.anonLove.domain.chat.ChatRoom;
import com.anonLove.domain.chat.MessageType;
import com.anonLove.domain.comment.Comment;
import com.anonLove.domain.post.Post;
import com.anonLove.domain.user.User;
import com.anonLove.dto.request.chat.CreateChatRoomRequest;
import com.anonLove.dto.request.chat.SendMessageRequest;
import com.anonLove.dto.response.chat.ChatMessageResponse;
import com.anonLove.dto.response.chat.ChatRoomListResponse;
import com.anonLove.dto.response.chat.CreateChatRoomResponse;
import com.anonLove.exception.CustomException;
import com.anonLove.exception.ErrorCode;
import com.anonLove.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    // 채팅방 생성
    public CreateChatRoomResponse createChatRoom(CreateChatRoomRequest request, Long initiatorId) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 게시글 작성자만 채팅 시작 가능
        if (!post.isAuthor(initiatorId)) {
            throw new CustomException(ErrorCode.NOT_POST_AUTHOR);
        }

        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 중복 채팅방 확인
        if (chatRoomRepository.existsByCommentId(request.getCommentId())) {
            ChatRoom existingRoom = chatRoomRepository
                    .findByCommentId(request.getCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            return new CreateChatRoomResponse(existingRoom.getId());
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .post(post)
                .comment(comment)
                .initiator(initiator)
                .receiver(receiver)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        log.info("Chat room created: roomId={}, postId={}, commentId={}",
                savedRoom.getId(), request.getPostId(), request.getCommentId());

        return new CreateChatRoomResponse(savedRoom.getId());
    }

    // 채팅 메시지 조회
    public List<ChatMessageResponse> getChatMessages(Long roomId, Long lastMessageId,
                                                     int size, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 참여자 확인
        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by("id").descending());

        Page<ChatMessage> messagePage;
        if (lastMessageId != null) {
            messagePage = chatMessageRepository.findRecentMessages(roomId, lastMessageId, pageable);
        } else {
            messagePage = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageable);
        }

        // List로 변환
        List<ChatMessageResponse> responses = messagePage.getContent().stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());

        Collections.reverse(responses);

        return responses;
    }

    // 사용자가 참여 중인 모든 채팅방 목록 조회
    public List<ChatRoomListResponse> getChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant(userId);

        return chatRooms.stream()
                .map(room -> {
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                            .orElse(null);

                    long unreadCount = chatMessageRepository.countUnreadMessages(room.getId(), userId);

                    return ChatRoomListResponse.builder()
                            .roomId(room.getId())
                            .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                            .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt())
                            .unreadCount(unreadCount)
                            .postInfo(ChatRoomListResponse.PostInfo.builder()
                                    .id(room.getPost().getId())
                                    .title(room.getPost().getTitle())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 채팅 메시지 읽음 처리
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }
        chatMessageRepository.markMessagesAsRead(roomId, userId);
        log.info("Messages marked as read: roomId={}, userId={}", roomId, userId);
    }

    // 채팅 메시지 저장
    @Transactional
    public ChatMessageResponse saveMessage(Long roomId, Long userId, SendMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(MessageType.TEXT)
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return ChatMessageResponse.from(savedMessage);
    }
}