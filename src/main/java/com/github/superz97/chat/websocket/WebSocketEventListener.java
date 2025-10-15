package com.github.superz97.chat.websocket;

import com.github.superz97.chat.entity.User;
import com.github.superz97.chat.service.UserService;
import com.github.superz97.chat.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserService userService;
    private final WebSocketService webSocketService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            try {
                String username = principal.getName();
                User user = userService.getUserByUsername(username);

                log.info("User connected: {} ({})", username, user.getId());

                // Set user status to online
                userService.setUserOnline(user.getId());

                // Broadcast user online status
                webSocketService.broadcastUserOnline(user.getId(), username);

            } catch (Exception e) {
                log.error("Error handling WebSocket connect event", e);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            try {
                String username = principal.getName();
                User user = userService.getUserByUsername(username);

                log.info("User disconnected: {} ({})", username, user.getId());

                // Set user status to offline
                userService.setUserOffline(user.getId());

                // Broadcast user offline status
                webSocketService.broadcastUserOffline(user.getId(), username);

            } catch (Exception e) {
                log.error("Error handling WebSocket disconnect event", e);
            }
        }
    }

}
