package com.exemple.security.enums;
public enum GroupRole {
    OWNER,          // Propriétaire du groupe - tous les droits
    ADMIN,          // Administrateur - peut gérer les membres et créer des conversations
    MODERATOR,      // Modérateur - peut créer des conversations et modérer les messages
    MEMBER          // Membre simple - peut participer aux conversations
}