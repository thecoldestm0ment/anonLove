package com.anonLove.repository;

import com.anonLove.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 댓글에 이미 채팅방이 있는지 확인 (OneToOne)
    boolean existsByCommentId(Long commentId);

    // 댓글의 채팅방 조회 (OneToOne)
    Optional<ChatRoom> findByCommentId(Long commentId);

    // 사용자가 참여 중인 채팅방 목록 (나간 채팅방 제외)
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE (cr.initiator.id = :userId AND cr.initiatorLeft = false) " +
            "OR (cr.receiver.id = :userId AND cr.receiverLeft = false) " +
            "ORDER BY cr.createdAt DESC")
    List<ChatRoom> findByParticipant(@Param("userId") Long userId);

    // 게시글 삭제 시 해당 게시글의 채팅방 post FK를 null로 설정
    @Modifying
    @Query("UPDATE ChatRoom cr SET cr.post = null WHERE cr.post.id = :postId")
    void nullifyPostByPostId(@Param("postId") Long postId);

    // 댓글 삭제 시 해당 댓글의 채팅방 comment FK를 null로 설정
    @Modifying
    @Query("UPDATE ChatRoom cr SET cr.comment = null WHERE cr.comment.id IN :commentIds")
    void nullifyCommentByCommentIds(@Param("commentIds") List<Long> commentIds);
}