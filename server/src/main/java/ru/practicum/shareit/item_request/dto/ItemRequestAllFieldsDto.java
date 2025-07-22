package ru.practicum.shareit.item_request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemRequestItemDto;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
@Builder
public class ItemRequestAllFieldsDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private Collection<ItemRequestItemDto> items;
}