package com.github.superz97.chat.controller;

import com.github.superz97.chat.dto.api.ApiResponse;
import com.github.superz97.chat.dto.response.ChannelDTO;
import com.github.superz97.chat.entity.Channel;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.service.ChannelService;
import com.github.superz97.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelDTO>> createChannel(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChannelDTO.CreateChannelRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Channel channel = channelService.createChannel(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Channel created successfully",
                channelService.toDTO(channel)));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ApiResponse<ChannelDTO>> getChannel(@PathVariable String channelId) {
        Channel channel = channelService.getChannelById(channelId);
        return ResponseEntity.ok(ApiResponse.success(channelService.toDTO(channel)));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<ChannelDTO>>> getPublicChannels() {
        List<ChannelDTO> channels = channelService.getPublicChannels().stream()
                .map(channelService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ChannelDTO>>> getMyChannels(
            @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        List<ChannelDTO> channels = channelService.getUserChannels(user.getId()).stream()
                .map(channelService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ApiResponse<ChannelDTO>> updateChannel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId,
            @RequestBody ChannelDTO.UpdateChannelRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Channel channel = channelService.updateChannel(channelId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Channel updated successfully",
                channelService.toDTO(channel)));
    }

    @PostMapping("/{channelId}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId,
            @RequestBody ChannelDTO.AddMemberRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.addMember(channelId, user.getId(), request.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", null));
    }

    @DeleteMapping("/{channelId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId,
            @PathVariable String userId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.removeMember(channelId, user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @PostMapping("/{channelId}/join")
    public ResponseEntity<ApiResponse<Void>> joinChannel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.addMember(channelId, user.getId(), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Joined channel successfully", null));
    }

    @PostMapping("/{channelId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveChannel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.removeMember(channelId, user.getId(), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Left channel successfully", null));
    }

    @PostMapping("/{channelId}/admins")
    public ResponseEntity<ApiResponse<Void>> promoteToAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId,
            @RequestBody ChannelDTO.PromoteToAdminRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.promoteToAdmin(channelId, user.getId(), request.getUserId());
        return ResponseEntity.ok(ApiResponse.success("User promoted to admin", null));
    }

    @DeleteMapping("/{channelId}/admins/{userId}")
    public ResponseEntity<ApiResponse<Void>> demoteAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId,
            @PathVariable String userId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.demoteAdmin(channelId, user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Admin demoted successfully", null));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<ApiResponse<Void>> deleteChannel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String channelId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        channelService.deleteChannel(channelId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Channel deleted successfully", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ChannelDTO>>> searchChannels(
            @RequestParam String query) {
        List<ChannelDTO> channels = channelService.searchChannels(query).stream()
                .map(channelService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

}
