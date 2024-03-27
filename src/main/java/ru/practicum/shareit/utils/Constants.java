package ru.practicum.shareit.utils;

public class Constants {
    private Constants() {
    }

    public static final String USER_NOT_FOUND_MESSAGE = "User with identifier %d not found";
    public static final String ITEM_NOT_FOUND_MESSAGE = "Item with identifier %d not found";
    public static final String USERS_ITEM_NOT_FOUND_MESSAGE = "Item with identifier %d not found for user with identifier %d";
    public static final String UNKNOWN_ERROR_MESSAGE = "An unknown error occurred, please check the correctness of all request data";
    public static final String WRONG_START_AND_END_BOOKING_DATES_MESSAGE = "End booking date must be after start booking date";
    public static final String ITEM_NOT_AVAILABLE_MESSAGE = "Item with identifier %d is not available for booking";
    public static final String BOOKING_NOT_FOUND_MESSAGE = "Booking with identifier %d not found";
    public static final String USER_CANNOT_BOOK_HIS_ITEM_MESSAGE = "User cannot book their own item";
    public static final String NOT_OWNER_CANNOT_CHANGE_BOOKING_STATUS_MESSAGE = "Only the item owner can change the booking status";
    public static final String USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE = "Cannot change booking status twice";
    public static final String NOT_BOOKING_OR_ITEM_OWNER_CANNOT_GET_BOOKING_MESSAGE = "Only the booking author and item owner can view the booking";
    public static final String UNKNOWN_SEARCHING_STATE_MESSAGE = "Unknown state: %s";
    public static final String TIME_NOT_AVAILABLE_FOR_BOOKING_MESSAGE = "Cannot book item from %s to %s as it is already booked for this time";
}
