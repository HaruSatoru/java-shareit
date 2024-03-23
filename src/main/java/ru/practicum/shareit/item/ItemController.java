package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utils.Create;
import ru.practicum.shareit.utils.Update;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String HEADER_WITH_USER_ID_NAME = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItem(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                              @Validated({Create.class}) @RequestBody ItemDto itemDto) {
        log.info("Attempt to create an item by user with id = {}", userId);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                              @Validated({Update.class}) @RequestBody ItemDto itemDto, @PathVariable long itemId) {
        log.info("Attempt to update item with id = {} by user with id = {}", itemId, userId);
        itemDto.setId(itemId);

        return itemService.updateItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDto retrieveItem(@RequestHeader(HEADER_WITH_USER_ID_NAME) long userId,
                                           @PathVariable @Positive long itemId) {
        log.info("Attempt to get item with id = {}", itemId);
        return itemService.retrieveItem(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingDto> getUsersItems(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Attempt to get all items of user with id = {}", userId);
        return itemService.getUsersItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByText(@RequestParam String text) {
        log.info("Attempt to search for items using search string: {}", text);

        return itemService.findItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postUserComment(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                      @PathVariable @Positive long itemId, @Valid @RequestBody CommentDto commentDto) {
        log.info("Attempt to leave a comment on item with id = {} by user with id = {}", itemId, userId);
        return itemService.postUserComment(commentDto, userId, itemId);
    }

}
