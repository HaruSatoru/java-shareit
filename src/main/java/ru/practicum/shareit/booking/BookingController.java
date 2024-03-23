package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;
    private static final String DEFAULT_SEARCH_VALUE = "ALL";
    private static final String HEADER_WITH_USER_ID_NAME = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody NewBookingRequest bookingDto,
                                    @RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Attempt to book item with id = {} by user with id = {}", bookingDto.getItemId(), userId);
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBookingStatus(@PathVariable @Positive long bookingId, @RequestParam boolean approved,
                                          @RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Attempt to change booking status with id = {} by user with id = {}", bookingId, userId);
        return bookingService.updateBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto retrieveBooking(@PathVariable @Positive long bookingId,
                                      @RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Attempt to retrieve booking with id = {} by user with id = {}", bookingId, userId);
        return bookingService.retrieveBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> retrieveBookingsByBookerId(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                       @RequestParam(defaultValue = DEFAULT_SEARCH_VALUE) String state) {
        log.info("Attempt to retrieve all bookings with {} status by booking author with id = {}", state, userId);
        return bookingService.retrieveBookingsByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> retrieveBookingsByOwnerId(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                      @RequestParam(defaultValue = DEFAULT_SEARCH_VALUE) String state) {
        log.info("Attempt to retrieve all bookings with {} status by item owner with id = {}", state, userId);
        return bookingService.retrieveBookingsByOwnerId(userId, state);
    }
}

