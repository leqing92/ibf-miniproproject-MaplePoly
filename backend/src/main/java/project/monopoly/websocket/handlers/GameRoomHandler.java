package project.monopoly.websocket.handlers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.monopoly.websocket.models.Message;

public class GameRoomHandler extends TextWebSocketHandler{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> roomPlayerSelections = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> gameDetailMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(GameRoomHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(session);        
    }
    /*
       {
            "type": "message",
            "sender": "username",
            "timestamp" : 
            "content": "Hello, everyone!"
        }
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String clientMessage = textMessage.getPayload();
        System.out.println("Received message: " + clientMessage);
        
        Message message = objectMapper.readValue(clientMessage, Message.class);

        String roomId = getRoomId(session);
        
        switch (message.getType()) {
            case "message":
                broadcastMessage(roomId, message);
                break;
            case "join":
                broadcastMessage(roomId, message);
                break;
            case "leave":
                broadcastMessage(roomId, message);
                break;            
            case "delete":
                broadcastMessage(roomId, message);
                break;
            case "start":
                broadcastMessage(roomId, message);
                break;
            case "select":
                handleSelectMessage(roomId, message);
                break;
            case "currentStateRequest":
                sendCurrentState(session, roomId);
                break;
            case "updateGameDetail":
                handleGameDetail(session, roomId, message);
                break;
            default:
                logger.warn("\ntype: {}\npayload: {}", message.getType(), message.getContent());                
                break;
        }
    }    

    private void broadcastMessage(String roomId, Message message) throws Exception {
        List<WebSocketSession> sessionsInRoom = roomSessions.getOrDefault(roomId, new CopyOnWriteArrayList<>());
        String jsonMessage = objectMapper.writeValueAsString(message);
        for (WebSocketSession sess : sessionsInRoom) {
            if (sess.isOpen()) {
                sess.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    private void handleSelectMessage(String roomId, Message message) throws Exception {
        
        Map<String, String> playerSelections = roomPlayerSelections.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        playerSelections.put(message.getSender(), message.getContent());
        
        broadcastMessage(roomId, message);
    }

    private void sendCurrentState(WebSocketSession session, String roomId) throws Exception {
        Map<String, String> playerSelections = roomPlayerSelections.get(roomId);
        if (playerSelections != null) {
            Message currentStateMessage = new Message();
            currentStateMessage.setType("currentState");
            currentStateMessage.setSender("system");
            currentStateMessage.setTimestamp(new Date());
            currentStateMessage.setContent(objectMapper.writeValueAsString(playerSelections));

            String jsonMessage = objectMapper.writeValueAsString(currentStateMessage);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    // oninit, owner will send to here and gt a map for the room
    private void handleGameDetail(WebSocketSession session, String roomId, Message message) throws Exception {
        //create 1st
        Map<String, String> roomGameDetail = gameDetailMap.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        //parse map sent from owner
        Map<String, String> gameDetails = objectMapper.readValue(message.getContent(), Map.class);        
        roomGameDetail.putAll(gameDetails);

        Message updatedGameDetailMessage = new Message();
        updatedGameDetailMessage.setType("updateGameDetail");
        updatedGameDetailMessage.setSender("system");
        updatedGameDetailMessage.setTimestamp(new Date());
        updatedGameDetailMessage.setContent(objectMapper.writeValueAsString(roomGameDetail));
        
        broadcastMessage(roomId, updatedGameDetailMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String roomId = getRoomId(session);
        List<WebSocketSession> sessionsInRoom = roomSessions.get(roomId);
        if (sessionsInRoom != null) {
            sessionsInRoom.remove(session);
            if (sessionsInRoom.isEmpty()) {
                roomSessions.remove(roomId);
                roomPlayerSelections.remove(roomId);
            }
        }
    }

    // @SuppressWarnings("null")
    private String getRoomId(WebSocketSession session) {
        // extract the roomId from the session URI
        return session.getUri().getPath().split("/")[3];
    }
}