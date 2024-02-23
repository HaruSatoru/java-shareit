package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.ItemAccessDeniedException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    public List<ItemDto> getAllItemsByUser(Long userId) {
        UserDto userDto = userService.getUserById(userId);
        return itemRepository.getAllItemsByUser(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto getItemById(Long itemId) {
        return itemRepository.getItemById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    public ItemDto addItem(ItemDto itemDto, Long userId) {
        UserDto userDto = userService.getUserById(userId);
        User user = UserMapper.toUser(userDto);
        Item item = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemRepository.addItem(item, user));
    }

    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        User user = UserMapper.toUser(userService.getUserById(userId));
        Item item = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        checkItemOwnership(user, item);
        item = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemRepository.updateItem(itemId, item, user));
    }

    public List<ItemDto> findItems(String text, Long userId) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.findItems(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkItemOwnership(User user, Item item) {
        if (!item.getOwner().equals(user)) {
            throw new ItemAccessDeniedException("User with id = " + user.getId() +
                    " is not the owner of the item with id = " + item.getId());
        }
    }
}