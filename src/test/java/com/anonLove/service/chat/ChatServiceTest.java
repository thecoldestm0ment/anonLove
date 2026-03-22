package com.anonLove.service.chat;

import com.anonLove.domain.chat.ChatMessage;
import com.anonLove.domain.chat.ChatRoom;
import com.anonLove.domain.chat.MessageType;
import com.anonLove.domain.user.Gender;
import com.anonLove.domain.user.User;
import com.anonLove.dto.response.chat.ChatMessageResponse;
import com.anonLove.dto.response.chat.ChatRoomListResponse;
import com.anonLove.repository.ChatMessageRepository;
import com.anonLove.repository.ChatRoomRepository;
import com.anonLove.repository.CommentRepository;
import com.anonLove.repository.PostRepository;
import com.anonLove.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User initiator;
    private User receiver;
    private ChatRoom room;

    @BeforeEach
    void setUp() {
        initiator = user(1L, "initiator@test.com", "initiator");
        receiver = user(2L, "receiver@test.com", "receiver");
        room = chatRoom(10L, initiator, receiver, LocalDateTime.of(2026, 3, 20, 10, 0));
    }

    @Test
    void getChatMessagesWithoutCursorReturnsLatestMessagesInChronologicalOrder() {
        ChatMessage newest = message(3L, room, initiator, "third", LocalDateTime.of(2026, 3, 20, 10, 3));
        ChatMessage older = message(2L, room, receiver, "second", LocalDateTime.of(2026, 3, 20, 10, 2));

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findByChatRoomIdOrderByIdDesc(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(newest, older)));

        List<ChatMessageResponse> responses = chatService.getChatMessages(10L, null, 50, 1L);

        assertThat(responses).extracting(ChatMessageResponse::getId).containsExactly(2L, 3L);
        verify(chatMessageRepository).markMessagesAsRead(10L, 1L);
    }

    @Test
    void getChatMessagesWithBeforeCursorReturnsOlderMessagesInChronologicalOrder() {
        ChatMessage newerOldMessage = message(5L, room, initiator, "fifth", LocalDateTime.of(2026, 3, 20, 10, 5));
        ChatMessage olderMessage = message(4L, room, receiver, "fourth", LocalDateTime.of(2026, 3, 20, 10, 4));

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findPreviousMessages(eq(10L), eq(6L), any()))
                .thenReturn(new PageImpl<>(List.of(newerOldMessage, olderMessage)));

        List<ChatMessageResponse> responses = chatService.getChatMessages(10L, 6L, 20, 1L);

        assertThat(responses).extracting(ChatMessageResponse::getId).containsExactly(4L, 5L);
        verify(chatMessageRepository, never()).markMessagesAsRead(10L, 1L);
    }

    @Test
    void getRecentMessagesReturnsOnlyMessagesAfterCursorInChronologicalOrder() {
        ChatMessage firstMissed = message(7L, room, initiator, "seventh", LocalDateTime.of(2026, 3, 20, 10, 7));
        ChatMessage secondMissed = message(8L, room, receiver, "eighth", LocalDateTime.of(2026, 3, 20, 10, 8));

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findMessagesAfterId(eq(10L), eq(6L), any()))
                .thenReturn(new PageImpl<>(List.of(firstMissed, secondMissed)));

        List<ChatMessageResponse> responses = chatService.getRecentMessages(10L, 6L, 20, 1L);

        assertThat(responses).extracting(ChatMessageResponse::getId).containsExactly(7L, 8L);
    }

    @Test
    void getChatRoomListSortsByLastMessageTimeDescending() {
        ChatRoom olderRoom = chatRoom(11L, initiator, receiver, LocalDateTime.of(2026, 3, 20, 9, 0));
        ChatRoom newerRoom = chatRoom(12L, initiator, receiver, LocalDateTime.of(2026, 3, 20, 9, 30));

        ChatMessage olderRoomLastMessage = message(3L, olderRoom, initiator, "older-room", LocalDateTime.of(2026, 3, 20, 10, 0));
        ChatMessage newerRoomLastMessage = message(4L, newerRoom, receiver, "newer-room", LocalDateTime.of(2026, 3, 20, 11, 0));

        when(chatRoomRepository.findByParticipant(1L)).thenReturn(List.of(olderRoom, newerRoom));
        when(chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(11L)).thenReturn(Optional.of(olderRoomLastMessage));
        when(chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(12L)).thenReturn(Optional.of(newerRoomLastMessage));
        when(chatMessageRepository.countUnreadMessages(11L, 1L)).thenReturn(0L);
        when(chatMessageRepository.countUnreadMessages(12L, 1L)).thenReturn(0L);

        List<ChatRoomListResponse> responses = chatService.getChatRoomList(1L);

        assertThat(responses).extracting(ChatRoomListResponse::getRoomId).containsExactly(12L, 11L);
    }

    @Test
    void markMessagesAsReadCallsRepositoryForParticipant() {
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));

        chatService.markMessagesAsRead(10L, 1L);

        verify(chatMessageRepository).markMessagesAsRead(10L, 1L);
    }

    private User user(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("password")
                .nickname(nickname)
                .university("Anon University")
                .gender(Gender.MALE)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ChatRoom chatRoom(Long id, User initiator, User receiver, LocalDateTime createdAt) {
        ChatRoom chatRoom = ChatRoom.builder()
                .initiator(initiator)
                .receiver(receiver)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", id);
        ReflectionTestUtils.setField(chatRoom, "createdAt", createdAt);
        return chatRoom;
    }

    private ChatMessage message(Long id, ChatRoom room, User sender, String content, LocalDateTime createdAt) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .messageType(MessageType.TEXT)
                .content(content)
                .build();
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", createdAt);
        return message;
    }
}
