package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.practicum.shareit.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Attempted to use non-existent user id when creating item: {}", userId);
                    return new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
                });

        Item newItem = ItemMapper.toItem(itemDto, user);
        Item savedItem = itemStorage.save(newItem);

        log.info("Item with id = {} created for user with id = {}", savedItem.getId(), userId);
        return ItemMapper.toItemDto(savedItem);
    }


    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Item currentItem = itemStorage.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException(String.format(ITEM_NOT_FOUND_MESSAGE, itemDto.getId())));

        if (!currentItem.getUser().getId().equals(userId)) {
            log.warn("Attempted to update item with id = {} that does not belong to user with id = {}", itemDto.getId(), userId);
            throw new NotFoundException(String.format(USERS_ITEM_NOT_FOUND_MESSAGE, itemDto.getId(), userId));
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            currentItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            currentItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            currentItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemStorage.save(currentItem);

        log.info("Item information updated for item with id = {} owned by user with id = {}", itemDto.getId(), userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingDto retrieveItem(long itemId, long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId)));

        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format(ITEM_NOT_FOUND_MESSAGE, itemId)));

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getUser().getId().equals(userId)) {
            List<Booking> sortedBookings = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(itemId, userId, Status.APPROVED);

            Booking[] bookings = retrieveLastAndNextBookings(sortedBookings);
            lastBooking = bookings[0];
            nextBooking = bookings[1];
        }

        List<Comment> comments = commentStorage.findByItem_IdOrderByIdAsc(itemId);

        log.info("Received a list of comments for item with id = {} of length {}", itemId, comments.size());
        log.info("Received item with id = {}", itemId);
        return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments);
    }


    public List<ItemWithBookingDto> getUsersItems(long userId) {
        List<Item> items = itemStorage.findByUser_IdOrderByIdAsc(userId);
        List<Long> ids = items.stream().map(Item::getId).collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        Map<Long, List<Comment>> comments = commentStorage.findByItem_IdInOrderByIdAsc(ids)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(), Collectors.toList()));

        Map<Long, List<Booking>> lastBookings = getBookingsByItemIdAndStatusBefore(ids, now);
        Map<Long, List<Booking>> nextBookings = getBookingsByItemIdAndStatusAfter(ids, now);

        List<ItemWithBookingDto> mappedItems = new ArrayList<>();

        for (Item item : items) {
            Booking lastBooking = getFirstBooking(lastBookings.get(item.getId()));
            Booking nextBooking = getFirstBooking(nextBookings.get(item.getId()));

            mappedItems.add(ItemMapper.toItemWithBookingDto(item,
                    lastBooking,
                    nextBooking,
                    comments.getOrDefault(item.getId(), List.of())));
        }

        log.info("Retrieved list of items for user with id = {} of length {}", userId, items.size());
        return mappedItems;
    }

    private Map<Long, List<Booking>> getBookingsByItemIdAndStatusBefore(List<Long> itemIds, LocalDateTime now) {
        return bookingStorage.findByItemIdInAndStartBeforeAndStatus(itemIds, now, Status.APPROVED, Sort.by(DESC, "start"))
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(), Collectors.toList()));
    }

    private Map<Long, List<Booking>> getBookingsByItemIdAndStatusAfter(List<Long> itemIds, LocalDateTime now) {
        return bookingStorage.findByItemIdInAndStartAfterAndStatus(itemIds, now, Status.APPROVED, Sort.by(ASC, "start"))
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(), Collectors.toList()));
    }


    private Booking getFirstBooking(List<Booking> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    @Override
    public List<ItemDto> findItemsByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> searchedItems = itemStorage
                .findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(text, text);

        log.info("Received a list of items of length {} found by search string: {}", searchedItems.size(), text);
        return ItemMapper.toItemDto(searchedItems);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CommentDto postUserComment(CommentDto commentDto, long userId, long itemId) {
        validateBooking(userId, itemId);

        if (commentStorage.existsByItem_IdAndAuthor_Id(itemId, userId)) {
            log.warn("An attempt was made to leave a comment twice for item with id = {} by user with id = {}", itemId, userId);
            throw new AlreadyExistException("Cannot comment twice");
        }

        User userRef = userStorage.getReferenceById(userId);
        Item itemRef = itemStorage.getReferenceById(itemId);

        Comment comment = commentStorage.save(CommentMapper.toComment(commentDto, itemRef, userRef));
        log.info("Added a comment with id = {} for item with id = {} by user with id = {}", comment.getId(), itemId, userId);
        return CommentMapper.toCommentDto(comment);
    }

    private void validateBooking(long userId, long itemId) {
        if (!bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(itemId, userId, Status.APPROVED, LocalDateTime.now())) {
            log.warn("An attempt was made to leave a comment for item with id = {} by user with id = {}, who either did not rent the item or the rental period has not yet expired", itemId, userId);
            throw new IllegalArgumentException("User " + userId + " cannot comment on item " + itemId);
        }
    }


    private Booking[] retrieveLastAndNextBookings(List<Booking> sortedBookings) {
        if (sortedBookings.isEmpty()) {
            return new Booking[]{null, null};
        }

        LocalDateTime currentTime = LocalDateTime.now();
        int nextBookingIndex = determineNextBookingIndex(sortedBookings, currentTime);

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (nextBookingIndex == -1) {
            lastBooking = sortedBookings.get(sortedBookings.size() - 1);
        } else if (nextBookingIndex == 0) {
            nextBooking = sortedBookings.get(nextBookingIndex);
        } else {
            nextBooking = sortedBookings.get(nextBookingIndex);
            lastBooking = sortedBookings.get(nextBookingIndex - 1);
        }

        return new Booking[]{lastBooking, nextBooking};
    }

    private int determineNextBookingIndex(List<Booking> sortedBookings, LocalDateTime currentTime) {
        for (int i = 0; i < sortedBookings.size(); i++) {
            if (sortedBookings.get(i).getStart().isAfter(currentTime)) {
                return i;
            }
        }
        return -1;
    }

}
