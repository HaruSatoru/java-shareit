package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.utils.Create;
import ru.practicum.shareit.utils.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @PositiveOrZero(groups = Update.class)
    private long id;
    @NotBlank(groups = {Create.class})
    private String name;
    @Email(groups = {Create.class, Update.class})
    @NotNull(groups = {Create.class})
    private String email;
}
