package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int DEFAULT_START_PAGE = 0;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        List<UserDto> allUsers = new ArrayList<>();

        Pageable page = PageRequest.of(DEFAULT_START_PAGE, DEFAULT_PAGE_SIZE, Sort.by("id"));

        while (true) {
            Page<User> userPage = userStorage.findAll(page);
            List<UserDto> userDtos = userPage.stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
            allUsers.addAll(userDtos);

            if (!userPage.hasNext()) {
                break;
            }

            page = userPage.nextPageable();
        }

        log.info("Retrieved a list of users with size {}", allUsers.size());
        return allUsers;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto user) {
        User savedUser = userStorage.save(UserMapper.toUser(user));
        log.info("Created user with id = {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId)));

        log.info("Retrieved user with id = {}", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto user) {
        User currentUser = userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, user.getId())));

        if (user.getEmail() != null) {
            currentUser.setEmail(user.getEmail());
        }

        if (user.getName() != null) {
            currentUser.setName(user.getName());
        }

        User updatedUser = userStorage.save(currentUser);
        log.info("Updated information for user with id = {}", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        try {
            userStorage.deleteById(userId);
            log.info("Deleted user with id = {}", userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Attempted to delete non-existent user with id = {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }
    }
}
