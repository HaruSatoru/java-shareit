package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

@Transactional(readOnly = true)
public interface UserStorage extends JpaRepository<User, Long> {
}