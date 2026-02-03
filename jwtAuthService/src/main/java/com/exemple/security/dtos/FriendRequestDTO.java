package com.exemple.security.dtos;

import com.exemple.security.entities.FriendRequest.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDTO {
    
    private Integer id;
    
    // Sender (celui qui envoie la demande)
    private Integer senderId;
    private String senderFirstname;
    private String senderLastname;
    private String senderEmail;
    private String senderAvatar;
    
    // Receiver (celui qui reçoit la demande)
    private Integer receiverId;
    private String receiverFirstname;
    private String receiverLastname;
    private String receiverEmail;
    private String receiverAvatar;
    
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}