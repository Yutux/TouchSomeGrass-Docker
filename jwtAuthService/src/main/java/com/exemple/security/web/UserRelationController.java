package com.exemple.security.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exemple.security.dtos.*;
import com.exemple.security.entities.*;
import com.exemple.security.enums.GroupRole;
import com.exemple.security.services.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user-relations")
@RequiredArgsConstructor
public class UserRelationController {
    
    private final UserRelationService userRelationService;
    private final MessageService messageService;
    private final CommentService commentService;
    private final GroupService groupService;
    private final ConversationService conversationService;
    
    // ========================================
    // 🔐 FAVORIS SPOTS (AVEC AUTH)
    // ========================================
    
    @PostMapping("/favorites/spots/{spotId}")
    public ResponseEntity<AuthenticationResponseDto> addFavoriteSpot(
            @PathVariable int spotId,
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.addFavoriteSpotFromAuth(authHeader, spotId);
    }
    
    @DeleteMapping("/favorites/spots/{spotId}")
    public ResponseEntity<AuthenticationResponseDto> removeFavoriteSpot(
            @PathVariable int spotId,
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.removeFavoriteSpotFromAuth(authHeader, spotId);
    }
    
    @GetMapping("/favorites/spots")
    public ResponseEntity<List<Spot>> getFavoriteSpots(
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.getFavoriteSpotsFromAuth(authHeader);
    }
    
    // ========================================
    // 🔐 FAVORIS HIKING SPOTS (AVEC AUTH)
    // ========================================
    
    @PostMapping("/favorites/hiking-spots/{hikingSpotId}")
    public ResponseEntity<AuthenticationResponseDto> addFavoriteHikingSpot(
            @PathVariable int hikingSpotId,
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.addFavoriteHikingSpotFromAuth(authHeader, hikingSpotId);
    }
    
    @DeleteMapping("/favorites/hiking-spots/{hikingSpotId}")
    public ResponseEntity<AuthenticationResponseDto> removeFavoriteHikingSpot(
            @PathVariable int hikingSpotId,
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.removeFavoriteHikingSpotFromAuth(authHeader, hikingSpotId);
    }
    
    @GetMapping("/favorites/hiking-spots")
    public ResponseEntity<List<HikingSpot>> getFavoriteHikingSpots(
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.getFavoriteHikingSpotsFromAuth(authHeader);
    }
    
    // ========================================
    // 🔐 AMIS (AVEC AUTH)
    // ========================================
    
    @PostMapping("/friends/{friendId}")
    public ResponseEntity<AuthenticationResponseDto> addFriend(
            @PathVariable int friendId,
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.addFriendFromAuth(authHeader, friendId);
    }
    
    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<AuthenticationResponseDto> removeFriend(
            @PathVariable int friendId,
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.removeFriendFromAuth(authHeader, friendId);
    }
    
    @GetMapping("/friends")
    public ResponseEntity<List<UserApp>> getFriends(
            @RequestHeader("Authorization") String authHeader) {
        return userRelationService.getFriendsFromAuth(authHeader);
    }
    
    // ========================================
    // 🔐 MESSAGES (AVEC AUTH)
    // ========================================
    
    @GetMapping("/messages/sent")
    public ResponseEntity<MessageResponseDto> getSentMessages(
            @RequestHeader("Authorization") String authHeader) {
        return messageService.getSentMessagesFromAuth(authHeader);
    }
    
    @GetMapping("/messages/unread")
    public ResponseEntity<MessageResponseDto> getUnreadMessages(
            @RequestHeader("Authorization") String authHeader) {
        return messageService.getUnreadMessagesFromAuth(authHeader);
    }
    
    @GetMapping("/messages/unread/count")
    public ResponseEntity<Long> countUnreadMessages(
            @RequestHeader("Authorization") String authHeader) {
        return messageService.countUnreadMessagesFromAuth(authHeader);
    }
    
    // ========================================
    // 🔐 COMMENTAIRES (AVEC AUTH)
    // ========================================
    
    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @RequestBody CreateCommentDto request,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.createCommentFromAuth(authHeader, request);
    }
    
    @PostMapping("/comments/reply")
    public ResponseEntity<CommentResponseDto> replyToComment(
            @RequestBody ReplyToCommentDto request,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.replyToCommentFromAuth(authHeader, request);
    }
    
    @GetMapping("/comments")
    public ResponseEntity<CommentResponseDto> getUserComments(
            @RequestHeader("Authorization") String authHeader) {
        return commentService.getUserCommentsFromAuth(authHeader);
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> deleteOwnComment(
            @PathVariable int commentId,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.deleteOwnCommentFromAuth(authHeader, commentId);
    }
    
    // ========================================
    // 🔓 COMMENTAIRES PUBLICS (SANS AUTH)
    // ========================================
    
    @GetMapping("/spots/{spotId}/comments")
    public ResponseEntity<CommentResponseDto> getSpotComments(@PathVariable int spotId) {
        return commentService.getSpotComments(spotId);
    }
    
    @GetMapping("/hiking-spots/{hikingSpotId}/comments")
    public ResponseEntity<CommentResponseDto> getHikingSpotComments(@PathVariable int hikingSpotId) {
        return commentService.getHikingSpotComments(hikingSpotId);
    }
    
    @GetMapping("/spots/{spotId}/rating")
    public ResponseEntity<Double> getSpotAverageRating(@PathVariable int spotId) {
        return ResponseEntity.ok(commentService.getSpotAverageRating(spotId));
    }
    
    @GetMapping("/hiking-spots/{hikingSpotId}/rating")
    public ResponseEntity<Double> getHikingSpotAverageRating(@PathVariable int hikingSpotId) {
        return ResponseEntity.ok(commentService.getHikingSpotAverageRating(hikingSpotId));
    }
    
    // ========================================
    // 🔐 COMMENTAIRES - GESTION PROPRIÉTAIRE (AVEC AUTH)
    // ========================================
    
    @GetMapping("/spots/{spotId}/comments/all")
    public ResponseEntity<CommentResponseDto> getAllSpotCommentsAsOwner(
            @PathVariable int spotId,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.getAllSpotCommentsAsOwnerFromAuth(authHeader, spotId);
    }
    
    @GetMapping("/hiking-spots/{hikingSpotId}/comments/all")
    public ResponseEntity<CommentResponseDto> getAllHikingSpotCommentsAsOwner(
            @PathVariable int hikingSpotId,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.getAllHikingSpotCommentsAsOwnerFromAuth(authHeader, hikingSpotId);
    }
    
    @PutMapping("/comments/{commentId}/hide")
    public ResponseEntity<CommentResponseDto> hideComment(
            @PathVariable int commentId,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.hideCommentFromAuth(authHeader, commentId);
    }
    
    @PutMapping("/comments/{commentId}/show")
    public ResponseEntity<CommentResponseDto> showComment(
            @PathVariable int commentId,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.showCommentFromAuth(authHeader, commentId);
    }
    
    @DeleteMapping("/comments/{commentId}/owner")
    public ResponseEntity<CommentResponseDto> deleteCommentAsOwner(
            @PathVariable int commentId,
            @RequestHeader("Authorization") String authHeader) {
        return commentService.deleteCommentAsOwnerFromAuth(authHeader, commentId);
    }
    
    // ========================================
    // 🔐 GROUPES (AVEC AUTH)
    // ========================================
    
    @PostMapping("/groups")
    public ResponseEntity<GroupResponseDto> createGroup(
            @RequestBody CreateGroupDto request,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.createGroupFromAuth(authHeader, request);
    }

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.getGroupMembersFromAuth(authHeader, groupId);
    }
    
    @GetMapping("/groups")
    public ResponseEntity<GroupResponseDto> getUserGroups(
            @RequestHeader("Authorization") String authHeader) {
        return groupService.getUserGroupsFromAuth(authHeader);
    }
    
    @PostMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponseDto> addMemberToGroup(
            @PathVariable int groupId,
            @PathVariable int userId,
            @RequestParam(required = false) GroupRole role,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.addMemberToGroupFromAuth(authHeader, groupId, userId, role);
    }
    
    @DeleteMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponseDto> removeMemberFromGroup(
            @PathVariable int groupId,
            @PathVariable int userId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.removeMemberFromGroupFromAuth(authHeader, groupId, userId);
    }
    
    @PutMapping("/groups/{groupId}/members/{userId}/role")
    public ResponseEntity<GroupResponseDto> changeMemberRole(
            @PathVariable int groupId,
            @PathVariable int userId,
            @RequestParam GroupRole newRole,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.changeMemberRoleFromAuth(authHeader, groupId, userId, newRole);
    }
    
    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<GroupResponseDto> leaveGroup(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.leaveGroupFromAuth(authHeader, groupId);
    }
    
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<GroupResponseDto> deleteGroup(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return groupService.deleteGroupFromAuth(authHeader, groupId);
    }
    
    // ========================================
    // 🔓 GROUPES PUBLICS (SANS AUTH)
    // ========================================
    
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroupById(@PathVariable int groupId) {
        return groupService.getGroupById(groupId);
    }
    
    @GetMapping("/groups/public")
    public ResponseEntity<GroupResponseDto> getPublicGroups() {
        return groupService.getPublicGroups();
    }
    
    @GetMapping("/groups/search")
    public ResponseEntity<GroupResponseDto> searchPublicGroups(@RequestParam String name) {
        return groupService.searchPublicGroups(name);
    }
    
    // ========================================
    // 🔐 CONVERSATIONS (AVEC AUTH)
    // ========================================
    
    @PostMapping("/conversations/private")
    public ResponseEntity<ConversationResponseDto> createPrivateConversation(
            @RequestBody CreateConversationDto request,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.createPrivateConversationFromAuth(authHeader, request);
    }
    
    @PostMapping("/conversations/group")
    public ResponseEntity<ConversationResponseDto> createGroupConversation(
            @RequestBody CreateConversationDto request,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.createGroupConversationFromAuth(authHeader, request);
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<ConversationResponseDto> getUserConversations(
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.getUserConversationsFromAuth(authHeader);
    }
    
    @GetMapping("/groups/{groupId}/conversations")
    public ResponseEntity<ConversationResponseDto> getGroupConversations(
            @PathVariable int groupId,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.getGroupConversationsFromAuth(authHeader, groupId);
    }
    
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponseDto> getConversationById(
            @PathVariable int conversationId,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.getConversationByIdFromAuth(authHeader, conversationId);
    }
    
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponseDto> deleteConversation(
            @PathVariable int conversationId,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.deleteConversationFromAuth(authHeader, conversationId);
    }
    
    // ========================================
    // 🔐 MESSAGES DANS CONVERSATIONS (AVEC AUTH)
    // ========================================
    
    @PostMapping("/conversations/messages")
    public ResponseEntity<MessageResponseDto> sendMessageInConversation(
            @RequestBody SendConversationMessageDto request,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.sendMessageInConversationFromAuth(authHeader, request);
    }
    
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponseDto> getConversationMessages(
            @PathVariable int conversationId,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.getConversationMessagesFromAuth(authHeader, conversationId);
    }
    
    @PutMapping("/conversations/{conversationId}/messages/read")
    public ResponseEntity<MessageResponseDto> markConversationMessagesAsRead(
            @PathVariable int conversationId,
            @RequestHeader("Authorization") String authHeader) {
        return conversationService.markConversationMessagesAsReadFromAuth(authHeader, conversationId);
    }
}