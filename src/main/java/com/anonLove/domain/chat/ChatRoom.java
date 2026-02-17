package com.anonLove.domain.chat;

import com.anonLove.domain.common.BaseTimeEntity;
import com.anonLove.domain.comment.Comment;
import com.anonLove.domain.post.Post;
import com.anonLove.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "initiator_left")
    private boolean initiatorLeft;

    @Column(name = "receiver_left")
    private boolean receiverLeft;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Builder
    public ChatRoom(Post post, Comment comment, User initiator, User receiver) {
        this.post = post;
        this.comment = comment;
        this.initiator = initiator;
        this.receiver = receiver;
    }

    public boolean isParticipant(Long userId) {
        return initiator.getId().equals(userId) || receiver.getId().equals(userId);
    }

    public void leave(Long userId) {
        if (initiator.getId().equals(userId)) {
            this.initiatorLeft = true;
        } else if (receiver.getId().equals(userId)) {
            this.receiverLeft = true;
        }
    }

    public boolean isBothLeft() {
        return initiatorLeft && receiverLeft;
    }

    public void clearPost() {
        this.post = null;
    }

    public void clearComment() {
        this.comment = null;
    }
}