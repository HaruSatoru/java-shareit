package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(NewBookingRequest bookingDto, long userId);

    BookingDto updateBookingStatus(long userId, long bookingId, boolean approved);

    BookingDto retrieveBooking(long userId, long bookingId);

    List<BookingDto> retrieveBookingsByBookerId(long userId, String state);

    List<BookingDto> retrieveBookingsByOwnerId(long userId, String state);
}