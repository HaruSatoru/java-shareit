package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, List<Item>> itemsByUserId = new HashMap<>();
    private long nextItemId = 1;

    @Override
    public List<Item> getAllItemsByUser(Long userId) {
        return itemsByUserId.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return itemsByUserId.values().stream()
                .flatMap(Collection::stream)
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    @Override
    public Item addItem(Item item, User user) {
        long newItemId = nextItemId++;
        item.setId(newItemId);

        itemsByUserId.computeIfAbsent(user.getId(), key -> new ArrayList<>()).add(item);

        return item;
    }

    @Override
    public Item updateItem(Long itemId, Item updItem, User user) {
        return getItemById(itemId)
                .map(item -> {
                    if (updItem.getName() != null)
                        item.setName(updItem.getName());
                    if (updItem.getDescription() != null)
                        item.setDescription(updItem.getDescription());
                    if (updItem.getAvailable() != null)
                        item.setAvailable(updItem.getAvailable());
                    return item;
                })
                .orElse(null);
    }

    @Override
    public List<Item> findItems(String text) {
        String searchText = text.toLowerCase();
        List<Item> foundItems = new ArrayList<>();
        for (List<Item> itemList : itemsByUserId.values()) {
            for (Item item : itemList) {
                if ((item.getName().toLowerCase().contains(searchText) || item.getDescription().toLowerCase().contains(searchText))
                        && item.getAvailable()) {
                    foundItems.add(item);
                }
            }
        }
        return foundItems;
    }
}
