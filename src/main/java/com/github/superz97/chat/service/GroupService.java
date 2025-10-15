package com.github.superz97.chat.service;

import com.github.superz97.chat.dto.response.GroupDTO;
import com.github.superz97.chat.entity.Group;
import com.github.superz97.chat.exception.BadRequestException;
import com.github.superz97.chat.exception.ForbiddenException;
import com.github.superz97.chat.exception.ResourceNotFoundException;
import com.github.superz97.chat.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    @Transactional
    public Group createGroup(String creatorId, GroupDTO.CreateGroupRequest request) {
        log.info("Creating group: {} by user: {}", request.getName(), creatorId);

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(creatorId)
                .maxMembers(request.getMaxMembers())
                .createdAt(LocalDateTime.now())
                .settings(Group.GroupSettings.builder().build())
                .build();

        group.getMemberIds().add(creatorId);
        group.getAdminIds().add(creatorId);

        // Add initial members if provided
        if (request.getMemberIds() != null) {
            request.getMemberIds().forEach(memberId -> {
                if (group.getMaxMembers() == null ||
                        group.getMemberIds().size() < group.getMaxMembers()) {
                    group.getMemberIds().add(memberId);
                }
            });
        }

        Group savedGroup = groupRepository.save(group);

        // Update all members
        savedGroup.getMemberIds().forEach(memberId ->
                userService.addGroupToUser(memberId, savedGroup.getId()));

        return savedGroup;
    }

    public Group getGroupById(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
    }

    public List<Group> getUserGroups(String userId) {
        return groupRepository.findByMemberId(userId);
    }

    @Transactional
    public Group updateGroup(String groupId, String userId, GroupDTO.UpdateGroupRequest request) {
        log.info("Updating group: {} by user: {}", groupId, userId);

        Group group = getGroupById(groupId);

        if (!isAdmin(groupId, userId)) {
            throw new ForbiddenException("Only admins can update the group");
        }

        if (request.getName() != null) {
            group.setName(request.getName());
        }

        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        if (request.getAvatarUrl() != null) {
            group.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getMaxMembers() != null) {
            group.setMaxMembers(request.getMaxMembers());
        }

        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @Transactional
    public Group updateGroupSettings(String groupId, String userId,
                                     GroupDTO.UpdateGroupSettingsRequest request) {
        log.info("Updating group settings: {} by user: {}", groupId, userId);

        Group group = getGroupById(groupId);

        if (!isAdmin(groupId, userId)) {
            throw new ForbiddenException("Only admins can update group settings");
        }

        Group.GroupSettings settings = group.getSettings();

        if (request.getAllowMemberInvites() != null) {
            settings.setAllowMemberInvites(request.getAllowMemberInvites());
        }

        if (request.getAllowMemberMessages() != null) {
            settings.setAllowMemberMessages(request.getAllowMemberMessages());
        }

        if (request.getOnlyAdminsCanPost() != null) {
            settings.setOnlyAdminsCanPost(request.getOnlyAdminsCanPost());
        }

        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @Transactional
    public void addMember(String groupId, String userId, String memberIdToAdd) {
        log.info("Adding member: {} to group: {}", memberIdToAdd, groupId);

        Group group = getGroupById(groupId);

        // Check permissions
        if (!group.getSettings().isAllowMemberInvites() && !isAdmin(groupId, userId)) {
            throw new ForbiddenException("Only admins can add members to this group");
        }

        if (!isMember(groupId, userId)) {
            throw new ForbiddenException("You must be a member to add others");
        }

        // Check max members
        if (group.getMaxMembers() != null &&
                group.getMemberIds().size() >= group.getMaxMembers()) {
            throw new BadRequestException("Group has reached maximum member capacity");
        }

        // Check if already a member
        if (group.getMemberIds().contains(memberIdToAdd)) {
            throw new BadRequestException("User is already a member of this group");
        }

        group.getMemberIds().add(memberIdToAdd);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        userService.addGroupToUser(memberIdToAdd, groupId);
    }

    @Transactional
    public void removeMember(String groupId, String userId, String memberIdToRemove) {
        log.info("Removing member: {} from group: {}", memberIdToRemove, groupId);

        Group group = getGroupById(groupId);

        // Only admins can remove others, or users can remove themselves
        if (!isAdmin(groupId, userId) && !userId.equals(memberIdToRemove)) {
            throw new ForbiddenException("Only admins can remove other members");
        }

        // Cannot remove creator
        if (memberIdToRemove.equals(group.getCreatorId())) {
            throw new BadRequestException("Cannot remove the group creator");
        }

        group.getMemberIds().remove(memberIdToRemove);
        group.getAdminIds().remove(memberIdToRemove);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        userService.removeGroupFromUser(memberIdToRemove, groupId);
    }

    @Transactional
    public void promoteToAdmin(String groupId, String userId, String memberIdToPromote) {
        log.info("Promoting member: {} to admin in group: {}", memberIdToPromote, groupId);

        Group group = getGroupById(groupId);

        if (!isAdmin(groupId, userId)) {
            throw new ForbiddenException("Only admins can promote other members");
        }

        if (!group.getMemberIds().contains(memberIdToPromote)) {
            throw new BadRequestException("User is not a member of this group");
        }

        group.getAdminIds().add(memberIdToPromote);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);
    }

    @Transactional
    public void demoteAdmin(String groupId, String userId, String adminIdToDemote) {
        log.info("Demoting admin: {} in group: {}", adminIdToDemote, groupId);

        Group group = getGroupById(groupId);

        if (!isAdmin(groupId, userId)) {
            throw new ForbiddenException("Only admins can demote other admins");
        }

        // Cannot demote creator
        if (adminIdToDemote.equals(group.getCreatorId())) {
            throw new BadRequestException("Cannot demote the group creator");
        }

        group.getAdminIds().remove(adminIdToDemote);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(String groupId, String userId) {
        log.info("Deleting group: {} by user: {}", groupId, userId);

        Group group = getGroupById(groupId);

        if (!group.getCreatorId().equals(userId)) {
            throw new ForbiddenException("Only the creator can delete the group");
        }

        group.setActive(false);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        // Remove group from all users
        group.getMemberIds().forEach(memberId ->
                userService.removeGroupFromUser(memberId, groupId));
    }

    public boolean isMember(String groupId, String userId) {
        return groupRepository.isMember(groupId, userId);
    }

    public boolean isAdmin(String groupId, String userId) {
        return groupRepository.isAdmin(groupId, userId);
    }

    public boolean canPost(String groupId, String userId) {
        Group group = getGroupById(groupId);

        if (!group.getSettings().isAllowMemberMessages()) {
            return false;
        }

        if (group.getSettings().isOnlyAdminsCanPost()) {
            return isAdmin(groupId, userId);
        }

        return isMember(groupId, userId);
    }

    public List<Group> searchGroups(String searchTerm) {
        return groupRepository.searchByName(searchTerm);
    }

    public GroupDTO toDTO(Group group) {
        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .creatorId(group.getCreatorId())
                .memberIds(group.getMemberIds())
                .adminIds(group.getAdminIds())
                .avatarUrl(group.getAvatarUrl())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .active(group.isActive())
                .maxMembers(group.getMaxMembers())
                .currentMemberCount(group.getMemberIds().size())
                .settings(GroupDTO.GroupSettingsDTO.builder()
                        .allowMemberInvites(group.getSettings().isAllowMemberInvites())
                        .allowMemberMessages(group.getSettings().isAllowMemberMessages())
                        .onlyAdminsCanPost(group.getSettings().isOnlyAdminsCanPost())
                        .build())
                .build();
    }

}
