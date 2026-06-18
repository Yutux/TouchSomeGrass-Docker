package com.exemple.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour les messages envoyés AUX clients (broadcast)
        config.enableSimpleBroker("/topic", "/queue");
        
        // Préfixe pour les messages envoyés PAR les clients
        config.setApplicationDestinationPrefixes("/app");
        
        // Préfixe pour les messages privés (utilisateur spécifique)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket avec fallback SockJS
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:5173")
            //.setAllowedOriginPatterns("http://localhost:*")
            .withSockJS();
    }
}