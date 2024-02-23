package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userRepository.getAllUsers())
            userDtoList.add(UserMapper.toUserDto(user));
        return userDtoList;
    }

    public UserDto getUserById(Long userId) {
        try {
            Optional<User> user = userRepository.getUserById(userId);
            return UserMapper.toUserDto(user.orElseThrow(() -> new UserNotFoundException(userId)));
        } catch (NullPointerException e) {
            throw new UserNotFoundException(userId);
        }
    }

    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (userRepository.isEmailExist(user.getEmail()))
            throw new DuplicateEmailException();
        return UserMapper.toUserDto(userRepository.addUser(user));
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userDto.setEmail(userDto.getEmail() == null ? user.getEmail() : userDto.getEmail());
        userDto.setName(userDto.getName() == null ? user.getName() : userDto.getName());

        if (!user.getEmail().equals(userDto.getEmail()) && userRepository.isEmailExist(userDto.getEmail())) {
            throw new DuplicateEmailException();
        }

        User updatedUser = UserMapper.toUser(userDto);
        updatedUser.setId(userId);
        return UserMapper.toUserDto(userRepository.updateUser(userId, updatedUser));
    }


    public boolean deleteUserById(Long userId) {
        return userRepository.deleteUserById(userId);
    }
}
