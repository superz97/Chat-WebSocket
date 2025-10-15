package com.github.superz97.chat.service;

import com.github.superz97.chat.dto.response.ChannelDTO;
import com.github.superz97.chat.entity.Channel;
import com.github.superz97.chat.enums.ChannelType;
import com.github.superz97.chat.exception.BadRequestException;
import com.github.superz97.chat.exception.DuplicateResourceException;
import com.github.superz97.chat.exception.ForbiddenException;
import com.github.superz97.chat.exception.ResourceNotFoundException;
import com.github.superz97.chat.repository.ChannelRepository;
import com.github.superz97.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserService userService;

    @Transactional
    public Channel createChannel(String creatorId, ChannelDTO.CreateChannelRequest request) {
        log.info("Creating channel: {} by user: {}", request.getName(), creatorId);

        if (channelRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Channel", "name", request.getName());
        }

        Channel channel = Channel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(creatorId)
                .type(request.getType() != null ? request.getType() : ChannelType.PUBLIC)
                .maxMembers(request.getMaxMembers())
                .createdAt(LocalDateTime.now())
                .build();

        channel.getMemberIds().add(creatorId);
        channel.getAdminIds().add(creatorId);

        Channel savedChannel = channelRepository.save(channel);
        userService.addChannelToUser(creatorId, savedChannel.getId());

        return savedChannel;
    }

    public Channel getChannelById(String channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "id", channelId));
    }

    public Channel getChannelByName(String name) {
        return channelRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", "name", name));
    }

    public List<Channel> getPublicChannels() {
        return channelRepository.findPublicChannels();
    }

    public List<Channel> getUserChannels(String userId) {
        return channelRepository.findByMemberId(userId);
    }

    @Transactional
    public Channel updateChannel(String channelId, String userId, ChannelDTO.UpdateChannelRequest request) {
        log.info("Updating channel: {} by user: {}", channelId, userId);

        Channel channel = getChannelById(channelId);

        if (!isAdmin(channelId, userId)) {
            throw new ForbiddenException("Only admins can update the channel");
        }

        if (request.getName() != null && !request.getName().equals(channel.getName())) {
            if (channelRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Channel", "name", request.getName());
            }
            channel.setName(request.getName());
        }

        if (request.getDescription() != null) {
            channel.setDescription(request.getDescription());
        }

        if (request.getAvatarUrl() != null) {
            channel.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getMaxMembers() != null) {
            channel.setMaxMembers(request.getMaxMembers());
        }

        channel.setUpdatedAt(LocalDateTime.now());
        return channelRepository.save(channel);
    }

    @Transactional
    public void addMember(String channelId, String userId, String memberIdToAdd) {
        log.info("Adding member: {} to channel: {}", memberIdToAdd, channelId);

        Channel channel = getChannelById(channelId);

        // Check permissions for private channels
        if (channel.getType() == ChannelType.PRIVATE && !isMember(channelId, userId)) {
            throw new ForbiddenException("Only members can add others to private channels");
        }

        // Check max members
        if (channel.getMaxMembers() != null &&
                channel.getMemberIds().size() >= channel.getMaxMembers()) {
            throw new BadRequestException("Channel has reached maximum member capacity");
        }

        // Check if already a member
        if (channel.getMemberIds().contains(memberIdToAdd)) {
            throw new BadRequestException("User is already a member of this channel");
        }

        channel.getMemberIds().add(memberIdToAdd);
        channel.setUpdatedAt(LocalDateTime.now());
        channelRepository.save(channel);

        userService.addChannelToUser(memberIdToAdd, channelId);
    }

    @Transactional
    public void removeMember(String channelId, String userId, String memberIdToRemove) {
        log.info("Removing member: {} from channel: {}", memberIdToRemove, channelId);

        Channel channel = getChannelById(channelId);

        // Only admins can remove others, or users can remove themselves
        if (!isAdmin(channelId, userId) && !userId.equals(memberIdToRemove)) {
            throw new ForbiddenException("Only admins can remove other members");
        }

        // Cannot remove creator
        if (memberIdToRemove.equals(channel.getCreatorId())) {
            throw new BadRequestException("Cannot remove the channel creator");
        }

        channel.getMemberIds().remove(memberIdToRemove);
        channel.getAdminIds().remove(memberIdToRemove);
        channel.setUpdatedAt(LocalDateTime.now());
        channelRepository.save(channel);

        userService.removeChannelFromUser(memberIdToRemove, channelId);
    }

    @Transactional
    public void promoteToAdmin(String channelId, String userId, String memberIdToPromote) {
        log.info("Promoting member: {} to admin in channel: {}", memberIdToPromote, channelId);

        Channel channel = getChannelById(channelId);

        if (!isAdmin(channelId, userId)) {
            throw new ForbiddenException("Only admins can promote other members");
        }

        if (!channel.getMemberIds().contains(memberIdToPromote)) {
            throw new BadRequestException("User is not a member of this channel");
        }

        channel.getAdminIds().add(memberIdToPromote);
        channel.setUpdatedAt(LocalDateTime.now());
        channelRepository.save(channel);
    }

    @Transactional
    public void demoteAdmin(String channelId, String userId, String adminIdToDemote) {
        log.info("Demoting admin: {} in channel: {}", adminIdToDemote, channelId);

        Channel channel = getChannelById(channelId);

        if (!isAdmin(channelId, userId)) {
            throw new ForbiddenException("Only admins can demote other admins");
        }

        // Cannot demote creator
        if (adminIdToDemote.equals(channel.getCreatorId())) {
            throw new BadRequestException("Cannot demote the channel creator");
        }

        channel.getAdminIds().remove(adminIdToDemote);
        channel.setUpdatedAt(LocalDateTime.now());
        channelRepository.save(channel);
    }

    @Transactional
    public void deleteChannel(String channelId, String userId) {
        log.info("Deleting channel: {} by user: {}", channelId, userId);

        Channel channel = getChannelById(channelId);

        if (!channel.getCreatorId().equals(userId)) {
            throw new ForbiddenException("Only the creator can delete the channel");
        }

        channel.setActive(false);
        channel.setUpdatedAt(LocalDateTime.now());
        channelRepository.save(channel);

        // Remove channel from all users
        channel.getMemberIds().forEach(memberId ->
                userService.removeChannelFromUser(memberId, channelId));
    }

    public boolean isMember(String channelId, String userId) {
        return channelRepository.isMember(channelId, userId);
    }

    public boolean isAdmin(String channelId, String userId) {
        return channelRepository.isAdmin(channelId, userId);
    }

    public List<Channel> searchChannels(String searchTerm) {
        return channelRepository.searchByName(searchTerm);
    }

    public ChannelDTO toDTO(Channel channel) {
        return ChannelDTO.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .creatorId(channel.getCreatorId())
                .type(channel.getType())
                .memberIds(channel.getMemberIds())
                .adminIds(channel.getAdminIds())
                .avatarUrl(channel.getAvatarUrl())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .active(channel.isActive())
                .maxMembers(channel.getMaxMembers())
                .currentMemberCount(channel.getMemberIds().size())
                .build();
    }

}
