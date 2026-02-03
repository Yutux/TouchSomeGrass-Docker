package com.exemple.security.dtos;

import com.exemple.security.entities.GroupInvitation.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInvitationDTO {
    
    private int id;
    private int groupId;
    private String groupName;
    private boolean groupIsPrivate;
    private int userId;
    private String userFirstname;
    private String userLastname;
    private String userEmail;
    private int invitedById;
    private String invitedByFirstname;
    private String invitedByLastname;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    // Constructeur pour les invitations reçues (avec infos du groupe et de l'inviteur)
    
    public GroupInvitationDTO(
            int id,
            int groupId,
            String groupName,
            boolean groupIsPrivate,
            int invitedById,
            String invitedByFirstname,
            String invitedByLastname,
            InvitationStatus status,
            LocalDateTime createdAt,
            LocalDateTime respondedAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupIsPrivate = groupIsPrivate;
        this.invitedById = invitedById;
        this.invitedByFirstname = invitedByFirstname;
        this.invitedByLastname = invitedByLastname;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    // Constructeur pour les invitations envoyées (avec infos de l'utilisateur invité)
    public GroupInvitationDTO(
            int id,
            int userId,
            String userFirstname,
            String userLastname,
            String userEmail,
            InvitationStatus status,
            LocalDateTime createdAt,
            LocalDateTime respondedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.userFirstname = userFirstname;
        this.userLastname = userLastname;
        this.userEmail = userEmail;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }
}