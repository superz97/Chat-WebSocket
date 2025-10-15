package com.github.superz97.chat.service;

import com.github.superz97.chat.dto.request.UserProfileUpdateRequest;
import com.github.superz97.chat.dto.response.UserDTO;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.enums.UserStatus;
import com.github.superz97.chat.exception.DuplicateResourceException;
import com.github.superz97.chat.exception.ResourceNotFoundException;
import com.github.superz97.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String keycloakId, String username, String email) {
        log.info("Creating user with keycloakId: {}, username: {} ", keycloakId, username);
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        User user = User.builder()
                .keycloakId(keycloakId)
                .username(username)
                .email(email)
                .status(UserStatus.OFFLINE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
    }

    public User findOrCreateUser(String keycloakId, String username, String email) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> createUser(keycloakId, username, email));
    }

    @Transactional
    public User updateProfile(String userId, UserProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User updateStatus(String userId, UserStatus status) {
        log.info("Updating status for user: {} to {}", userId, status);

        User user = getUserById(userId);
        user.setStatus(status);
        user.setLastSeen(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void setUserOnline(String userId) {
        updateStatus(userId, UserStatus.ONLINE);
    }

    @Transactional
    public void setUserOffline(String userId) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.OFFLINE);
        user.setLastSeen(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> getOnlineUsers() {
        return userRepository.findOnlineUsers();
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchByUsername(searchTerm);
    }

    public List<User> getUsersByIds(List<String> userIds) {
        return userRepository.findByIdIn(userIds.stream().collect(Collectors.toSet()));
    }

    @Transactional
    public void addChannelToUser(String userId, String channelId) {
        User user = getUserById(userId);
        user.getChannelIds().add(channelId);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void removeChannelFromUser(String userId, String channelId) {
        User user = getUserById(userId);
        user.getChannelIds().remove(channelId);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void addGroupToUser(String userId, String groupId) {
        User user = getUserById(userId);
        user.getGroupIds().add(groupId);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void removeGroupFromUser(String userId, String groupId) {
        User user = getUserById(userId);
        user.getGroupIds().remove(groupId);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void blockUser(String userId, String blockedUserId) {
        User user = getUserById(userId);
        user.getBlockedUserIds().add(blockedUserId);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(String userId, String blockedUserId) {
        User user = getUserById(userId);
        user.getBlockedUserIds().remove(blockedUserId);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public boolean isUserBlocked(String userId, String targetUserId) {
        User user = getUserById(userId);
        return user.getBlockedUserIds().contains(targetUserId);
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .status(user.getStatus())
                .lastSeen(user.getLastSeen())
                .createdAt(user.getCreatedAt())
                .channelIds(user.getChannelIds())
                .groupIds(user.getGroupIds())
                .build();
    }

}
