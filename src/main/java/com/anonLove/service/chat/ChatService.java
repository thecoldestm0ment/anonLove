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
import com.anonLove.repository.ChatMessageRepository;
import com.anonLove.repository.ChatRoomRepository;
import com.anonLove.repository.CommentRepository;
import com.anonLove.repository.PostRepository;
import com.anonLove.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
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
    public CreateChatRoomResponse createChatRoom(CreateChatRoomRequest request, Long initiatorId) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!post.isAuthor(initiatorId)) {
            throw new CustomException(ErrorCode.NOT_POST_AUTHOR);
        }

        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (chatRoomRepository.existsByCommentId(request.getCommentId())) {
            ChatRoom existingRoom = chatRoomRepository.findByCommentId(request.getCommentId())
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

    public List<ChatMessageResponse> getChatMessages(Long roomId, Long beforeMessageId,
            int size, Long userId) {
        validateChatParticipant(roomId, userId);
        markMessagesAsReadOnRoomEnter(roomId, beforeMessageId, userId);

        Pageable pageable = PageRequest.of(0, size);
        Page<ChatMessage> messagePage = beforeMessageId != null
                ? chatMessageRepository.findPreviousMessages(roomId, beforeMessageId, pageable)
                : chatMessageRepository.findByChatRoomIdOrderByIdDesc(roomId, pageable);

        return toChronologicalResponses(messagePage.getContent(), true);
    }

    public List<ChatMessageResponse> getRecentMessages(Long roomId, Long afterMessageId,
            int size, Long userId) {
        validateChatParticipant(roomId, userId);

        Pageable pageable = PageRequest.of(0, size);
        Page<ChatMessage> messagePage = chatMessageRepository.findMessagesAfterId(roomId, afterMessageId, pageable);

        return toChronologicalResponses(messagePage.getContent(), false);
    }

    public List<ChatRoomListResponse> getChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipant(userId);

        return chatRooms.stream()
                .map(room -> {
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByIdDesc(room.getId())
                            .orElse(null);

                    long unreadCount = chatMessageRepository.countUnreadMessages(room.getId(), userId);

                    ChatRoomListResponse.PostInfo postInfo = room.getPost() != null
                            ? ChatRoomListResponse.PostInfo.builder()
                                    .id(room.getPost().getId())
                                    .title(room.getPost().getTitle())
                                    .build()
                            : null;

                    return ChatRoomListResponse.builder()
                            .roomId(room.getId())
                            .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                            .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt())
                            .unreadCount(unreadCount)
                            .postInfo(postInfo)
                            .build();
                })
                .sorted(Comparator.comparing(ChatRoomListResponse::getLastMessageAt).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        validateChatParticipant(roomId, userId);
        chatMessageRepository.markMessagesAsRead(roomId, userId);
        log.info("Messages marked as read: roomId={}, userId={}", roomId, userId);
    }

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

    @Transactional
    public void leaveChatRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }

        chatRoom.leave(userId);

        if (chatRoom.isBothLeft()) {
            chatRoomRepository.delete(chatRoom);
        }

        log.info("User left chat room: roomId={}, userId={}", roomId, userId);
    }

    private void validateChatParticipant(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isParticipant(userId)) {
            throw new CustomException(ErrorCode.NOT_CHAT_PARTICIPANT);
        }
    }

    private void markMessagesAsReadOnRoomEnter(Long roomId, Long beforeMessageId, Long userId) {
        if (beforeMessageId == null) {
            chatMessageRepository.markMessagesAsRead(roomId, userId);
            log.info("Messages auto-marked as read on room enter: roomId={}, userId={}", roomId, userId);
        }
    }

    private List<ChatMessageResponse> toChronologicalResponses(List<ChatMessage> messages,
            boolean reverseBeforeMapping) {
        List<ChatMessage> orderedMessages = reverseBeforeMapping
                ? messages.stream().collect(Collectors.toList())
                : messages;

        if (reverseBeforeMapping) {
            Collections.reverse(orderedMessages);
        }

        return orderedMessages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }
}
