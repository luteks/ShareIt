package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
                    SELECT c
                    FROM Comment c
                    join fetch c.item i
                    join fetch c.author a
                    WHERE i.id = :id
            """)
    Collection<Comment> findAllByItemId(@Param("id") Long itemId);

    List<Comment> findAllByItemIdIn(Collection<Long> itemIds);

    @Query("""
                    SELECT c
                    FROM Comment c
                    join fetch c.item i
                    join fetch c.author a
                    WHERE c.item IN :items
            """)
    Collection<Comment> findAllByItems(@Param("items") Collection<Item> items);
}