package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByOwnerIdOrderById(Long userId, Pageable pageable);

    @Query("""
            select i
            from Item i
            where (upper(i.name) like upper(concat('%', ?1, '%'))
            or upper(i.description) like upper(concat('%', ?1, '%')))
            and i.available = true
            """)
    Page<Item> search(String text, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "request"})
    Collection<Item> findAllByRequest_IdIn(Collection<Long> requestsIds);

    @EntityGraph(attributePaths = {"owner", "request"})
    Collection<Item> findAllByRequest_Id(Long requestId);
}