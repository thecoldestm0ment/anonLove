package com.anonLove.domain.post;

import com.anonLove.domain.chat.ChatRoom;
import com.anonLove.domain.comment.Comment;
import com.anonLove.domain.common.BaseTimeEntity;
import com.anonLove.domain.user.Gender;
import com.anonLove.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_type")
    private VisibilityType visibilityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_gender")
    private TargetGender targetGender;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<ChatRoom> chatRooms = new ArrayList<>();

    @Builder
    public Post(User user, Category category, String title, String content,
            VisibilityType visibilityType, TargetGender targetGender) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.visibilityType = visibilityType != null ? visibilityType : VisibilityType.ALL;
        this.targetGender = targetGender != null ? targetGender : TargetGender.ALL;
    }

    public void update(Category category, String title, String content,
            VisibilityType visibilityType, TargetGender targetGender) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.visibilityType = visibilityType;
        this.targetGender = targetGender;
    }

    public boolean isAuthor(Long userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isVisibleTo(User viewer) {
        // 같은 학교 숨기기
        if (this.visibilityType == VisibilityType.HIDE_SAME_UNI) {
            if (this.user.getUniversity().equals(viewer.getUniversity())) {
                return false;
            }
        }

        // 성별 필터링
        if (this.targetGender == TargetGender.MALE && viewer.getGender() != Gender.MALE) {
            return false;
        }
        if (this.targetGender == TargetGender.FEMALE && viewer.getGender() != Gender.FEMALE) {
            return false;
        }

        return true;
    }
}