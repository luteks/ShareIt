package ru.practicum.shareit.item_request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item_request.model.ItemRequest;


public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @EntityGraph(attributePaths = {"requestor"})
    Page<ItemRequest> findAllByRequestor_IdOrderByCreatedDesc(Long requestorId, Pageable pageable);

    @EntityGraph(attributePaths = {"requestor"})
    Page<ItemRequest> findAllByRequestor_IdNotOrderByCreatedDesc(Long requestorId, Pageable pageable);
}