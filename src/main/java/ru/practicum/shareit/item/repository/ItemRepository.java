package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> getAllItemsByUser(Long userId);

    Optional<Item> getItemById(Long itemId);

    Item addItem(Item item, User user);

    Item updateItem(Long itemId, Item item, User user);

    List<Item> findItems(String text);

}
