package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build()
        );
    }

    public ResponseEntity<Object> searchItems(String text,
                                              Long userId,
                                              Integer from,
                                              Integer size) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> createComment(CommentDto commentDto,
                                                Long itemId,
                                                Long userId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }

    public ResponseEntity<Object> updateItem(ItemDto itemDto,
                                             Long itemId,
                                             Long userId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItems(Long userId,
                                           Integer from,
                                           Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto,
                                             Long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> getItem(Long itemId,
                                          Long userId) {
        return get("/" + itemId, userId);
    }

    public void deleteItem(Long itemId) {
        delete("/" + itemId);
    }
}