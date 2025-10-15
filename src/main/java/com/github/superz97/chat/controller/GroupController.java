package com.github.superz97.chat.controller;

import com.github.superz97.chat.dto.api.ApiResponse;
import com.github.superz97.chat.dto.response.GroupDTO;
import com.github.superz97.chat.entity.Group;
import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.service.GroupService;
import com.github.superz97.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupDTO>> createGroup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody GroupDTO.CreateGroupRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Group group = groupService.createGroup(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Group created successfully",
                groupService.toDTO(group)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDTO>> getGroup(@PathVariable String groupId) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(ApiResponse.success(groupService.toDTO(group)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> getMyGroups(
            @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        List<GroupDTO> groups = groupService.getUserGroups(user.getId()).stream()
                .map(groupService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDTO>> updateGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId,
            @RequestBody GroupDTO.UpdateGroupRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Group group = groupService.updateGroup(groupId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully",
                groupService.toDTO(group)));
    }

    @PutMapping("/{groupId}/settings")
    public ResponseEntity<ApiResponse<GroupDTO>> updateGroupSettings(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId,
            @RequestBody GroupDTO.UpdateGroupSettingsRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        Group group = groupService.updateGroupSettings(groupId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Group settings updated successfully",
                groupService.toDTO(group)));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId,
            @RequestBody GroupDTO.AddMemberRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        groupService.addMember(groupId, user.getId(), request.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", null));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId,
            @PathVariable String userId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        groupService.removeMember(groupId, user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        groupService.removeMember(groupId, user.getId(), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Left group successfully", null));
    }

    @PostMapping("/{groupId}/admins")
    public ResponseEntity<ApiResponse<Void>> promoteToAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId,
            @RequestBody GroupDTO.PromoteToAdminRequest request) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        groupService.promoteToAdmin(groupId, user.getId(), request.getUserId());
        return ResponseEntity.ok(ApiResponse.success("User promoted to admin", null));
    }

    @DeleteMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<ApiResponse<Void>> demoteAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId,
            @PathVariable String userId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        groupService.demoteAdmin(groupId, user.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Admin demoted successfully", null));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String groupId) {
        String keycloakId = jwt.getSubject();
        User user = userService.getUserByKeycloakId(keycloakId);

        groupService.deleteGroup(groupId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GroupDTO>>> searchGroups(
            @RequestParam String query) {
        List<GroupDTO> groups = groupService.searchGroups(query).stream()
                .map(groupService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

}
