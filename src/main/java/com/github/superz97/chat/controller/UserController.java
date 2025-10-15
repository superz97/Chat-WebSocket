package com.github.superz97.chat.controller;

import com.github.superz97.chat.dto.api.ApiResponse;
import com.github.superz97.chat.dto.request.UserProfileUpdateRequest;
import com.github.superz97.chat.dto.request.UserStatusUpdateRequest;
import com.github.superz97.chat.dto.response.UserDTO;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        User user = userService.findOrCreateUser(keycloakId, username, email);
        return ResponseEntity.ok(ApiResponse.success(userService.toDTO(user)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(userService.toDTO(user)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(userService.toDTO(user)));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserProfileUpdateRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        User updatedUser = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                userService.toDTO(updatedUser)));
    }

    @PutMapping("/me/status")
    public ResponseEntity<ApiResponse<UserDTO>> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserStatusUpdateRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        User updatedUser = userService.updateStatus(user.getId(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully",
                userService.toDTO(updatedUser)));
    }

    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getOnlineUsers() {
        List<UserDTO> users = userService.getOnlineUsers().stream()
                .map(userService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @RequestParam String query) {
        List<UserDTO> users = userService.searchUsers(query).stream()
                .map(userService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String userId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        userService.blockUser(user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("User blocked successfully", null));
    }

    @DeleteMapping("/block/{userId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String userId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        userService.unblockUser(user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("User unblocked successfully", null));
    }

}
