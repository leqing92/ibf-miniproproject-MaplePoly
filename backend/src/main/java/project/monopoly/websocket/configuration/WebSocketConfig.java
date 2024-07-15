package project.monopoly.websocket.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import project.monopoly.websocket.handlers.GameHandler;
import project.monopoly.websocket.handlers.GameRoomHandler;

// https://medium.com/@parthiban.rajalingam/introduction-to-web-sockets-using-spring-boot-and-angular-b11e7363f051

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{
    @Value("${allow.origin}")
    String allowOrigin;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(GameRoomHandler(), "/app/room/{gid}").setAllowedOrigins(allowOrigin);
        registry.addHandler(GameHandler(), "/app/game/{gid}").setAllowedOrigins(allowOrigin);
    }

    @Bean
    public WebSocketHandler GameHandler() {
        return new GameHandler();
    }

    @Bean
    public WebSocketHandler GameRoomHandler() {
        return new GameRoomHandler();
    }
    
}