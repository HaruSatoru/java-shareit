package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.status.SearchingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.utils.Constants.USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingDto createBooking(NewBookingRequest bookingDto, long userId) {
        validateBookingTime(bookingDto.getStart(), bookingDto.getEnd());

        User user = getUserById(userId);
        Item item = getItemById(bookingDto.getItemId());

        if (!item.isAvailable()) {
            log.warn("Attempt by user with id = {} to book unavailable item with id = {}", userId, item.getId());
            throw new NotAvailableException(String.format(Constants.ITEM_NOT_AVAILABLE_MESSAGE, item.getId()));
        }

        if (item.getUser().getId() == userId) {
            log.warn("Attempt by user with id = {} to book their own item with id = {}", userId, item.getId());
            throw new SecurityException(Constants.USER_CANNOT_BOOK_HIS_ITEM_MESSAGE);
        }

        checkTimeOverlap(bookingDto.getStart(), bookingDto.getEnd(), item.getId());

        Booking createdBooking = bookingStorage.save(BookingMapper.toBooking(bookingDto, user, item));
        log.info("Created booking with id = {} by user with id = {} for item with id = {}", createdBooking.getId(), userId, bookingDto.getItemId());
        return BookingMapper.toBookingDto(createdBooking);
    }

    private User getUserById(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Attempt to book item by user with non-existing id = {}", userId);
                    return new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
                });
    }

    private Item getItemById(long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Attempt to book non-existing item with id = {}", itemId);
                    return new NotFoundException(String.format(Constants.ITEM_NOT_FOUND_MESSAGE, itemId));
                });
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingDto updateBookingStatus(long userId, long bookingId, boolean approved) {
        Booking booking = getBookingById(bookingId);

        validateOwnership(userId, booking.getItem().getId());

        validateBookingStatus(booking);

        updateBookingStatus(booking, approved);

        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    private void validateOwnership(long userId, long itemId) {
        if (!itemStorage.existsByUser_IdAndId(userId, itemId)) {
            log.warn("Attempt by user with id = {} to change booking status for item with id = {}, which is not owned by them", itemId, userId);
            throw new SecurityException(Constants.NOT_OWNER_CANNOT_CHANGE_BOOKING_STATUS_MESSAGE);
        }
    }

    private void validateBookingStatus(Booking booking) {
        if (!booking.getStatus().equals(Status.WAITING)) {
            log.warn("Attempt to change booking status twice for booking with id = {} by user with id = {}", booking.getId(), booking.getBooker().getId());
            throw new IllegalArgumentException(USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE);
        }
    }

    private void updateBookingStatus(Booking booking, boolean approved) {
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        log.info("Updated booking status for booking with id = {} to {}", booking.getId(), booking.getStatus());
    }


    @Override
    public BookingDto retrieveBooking(long userId, long bookingId) {
        Booking booking = getBookingById(bookingId);

        validateBookingOwnership(userId, booking);

        return BookingMapper.toBookingDto(booking);
    }

    private void validateBookingOwnership(long userId, Booking booking) {
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isItemOwner = booking.getItem().getUser().getId().equals(userId);

        if (!isBooker && !isItemOwner) {
            log.warn("Attempt to retrieve booking with id = {} by user with id = {}, who is neither the author of the booking nor the owner of the item", booking.getId(), userId);
            throw new SecurityException(Constants.NOT_BOOKING_OR_ITEM_OWNER_CANNOT_GET_BOOKING_MESSAGE);
        }
    }

    @Override
    public List<BookingDto> retrieveBookingsByBookerId(long userId, String state) {
        List<Booking> bookings;
        SearchingState searchingState = getSearchingState(state);

        validateUserExistence(userId);

        switch (searchingState) {
            case ALL:
                bookings = bookingStorage.findByBooker_IdOrderByStartDesc(userId);
                break;
            case PAST:
                bookings = bookingStorage.findByBooker_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingStorage.findByBooker_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case CURRENT:
                bookings = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingStorage.findByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingStorage.findByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                bookings = List.of();
        }

        log.info("Received list of bookings by author with id = {} with status {} of length {}", userId, state,
                bookings.size());
        return BookingMapper.toBookingDto(bookings);
    }

    @Override
    public List<BookingDto> retrieveBookingsByOwnerId(long userId, String state) {
        SearchingState searchingState = getSearchingState(state);
        validateUserExistence(userId);

        List<Booking> bookings;
        switch (searchingState) {
            case ALL:
                bookings = bookingStorage.findByItem_User_IdOrderByStartDesc(userId);
                break;
            case REJECTED:
            case WAITING:
                bookings = bookingStorage.findByItem_User_IdAndStatusOrderByStartDesc(userId, getStatusForState(searchingState));
                break;
            case FUTURE:
                bookings = bookingStorage.findByItem_User_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingStorage.findByItem_User_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case CURRENT:
                bookings = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            default:
                bookings = Collections.emptyList();
        }

        log.info("Received list of bookings by item owner with id = {} with status {} of length {}", userId, state, bookings.size());
        return BookingMapper.toBookingDto(bookings);
    }

    private Status getStatusForState(SearchingState state) {
        return state == SearchingState.REJECTED ? Status.REJECTED : Status.WAITING;
    }


    private Booking getBookingById(long bookingId) {
        Optional<Booking> booking = bookingStorage.findById(bookingId);

        if (booking.isEmpty()) {
            log.warn("Attempt to retrieve booking by non-existing id = {}", bookingId);
            throw new NotFoundException(String.format(Constants.BOOKING_NOT_FOUND_MESSAGE, bookingId));
        }

        return booking.get();
    }

    private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            log.warn("Attempt to create booking with incorrect dates: start = {}, end = {}", start, end);
            throw new IllegalArgumentException(Constants.WRONG_START_AND_END_BOOKING_DATES_MESSAGE);
        }
    }

    private void validateUserExistence(long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Attempt to retrieve bookings of non-existing user with id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private SearchingState getSearchingState(String state) {
        try {
            return SearchingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.warn("Attempt to retrieve list of bookings with non-existing status {}", state);
            throw new IllegalArgumentException(String.format(Constants.UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }
    }

    private void checkTimeOverlap(LocalDateTime bookingStart, LocalDateTime bookingEnd, long itemId) {
        List<Booking> overlappingBookings = bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(
                itemId, bookingStart, Status.APPROVED);

        for (Booking booking : overlappingBookings) {
            boolean overlaps = bookingStart.isBefore(booking.getEnd()) && bookingEnd.isAfter(booking.getStart());
            if (overlaps) {
                log.warn("Attempt to create booking for item with id = {}, overlapping in time with already approved booking with id = {}", itemId, booking.getId());
                throw new AlreadyExistException(String.format(Constants.TIME_NOT_AVAILABLE_FOR_BOOKING_MESSAGE,
                        bookingStart.format(FORMATTER), bookingEnd.format(FORMATTER)));
            }
        }
    }

}