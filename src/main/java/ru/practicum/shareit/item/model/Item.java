package ru.practicum.shareit.item.model;

import lombok.Builder;
import ru.practicum.shareit.request.ItemRequest;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Item {

    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private final User owner;
    private ItemRequest itemRequest;
}
