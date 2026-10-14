package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemId(Long itemId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.item.id IN ?1")
    List<Comment> findByItemIdIn(List<Long> itemIds);
}
