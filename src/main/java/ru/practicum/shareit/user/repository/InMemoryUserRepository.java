package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> uniqueEmailsSet = new HashSet<>();

    private long userId = 0;

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (uniqueEmailsSet.contains(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        userId++;
        user.setId(userId);
        users.put(user.getId(), user);
        uniqueEmailsSet.add(user.getEmail());
        return user;
    }

    @Override
    public User updateUser(Long id, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        User oldUser = users.get(id);
        if (oldUser == null) {
            throw new NoSuchElementException("User not found");
        }
        if (!oldUser.getEmail().equals(user.getEmail()) && uniqueEmailsSet.contains(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        users.put(id, user);
        uniqueEmailsSet.remove(oldUser.getEmail());
        uniqueEmailsSet.add(user.getEmail());
        return user;
    }

    @Override
    public boolean deleteUserById(Long id) {
        User user = users.remove(id);
        if (user != null) {
            uniqueEmailsSet.remove(user.getEmail());
            return true;
        }
        return false;
    }

    @Override
    public Boolean isEmailExist(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return uniqueEmailsSet.contains(email);
    }
}
