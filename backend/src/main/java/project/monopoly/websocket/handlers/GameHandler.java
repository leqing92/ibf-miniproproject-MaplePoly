package project.monopoly.websocket.handlers;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.monopoly.game.models.GameState;
import project.monopoly.game.models.PlayerState;
import project.monopoly.game.models.Property;
import project.monopoly.game.models.PropertyState;
import project.monopoly.game.models.TurnState;
import project.monopoly.game.service.GameStateService;
import project.monopoly.game.service.PropertyService;
import project.monopoly.security.repository.UserInfoRepository;
import project.monopoly.websocket.models.GameAction;

public class GameHandler extends TextWebSocketHandler {

    @Autowired
    GameStateService gameStateSvc;

    @Autowired
    PropertyService propertySvc;

    @Autowired
    UserInfoRepository userInfoRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, TurnState> gameStateMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(GameHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String gid = getRoomId(session);
        roomSessions.computeIfAbsent(gid, k -> new CopyOnWriteArrayList<>()).add(session);
        gameStateMap.computeIfAbsent(gid, k -> {
            GameState gameState = gameStateSvc.getGameStateBygid(gid);
            TurnState turnState = gameState.getTurnStates();

            return turnState;
        });
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("payload: {}", payload);
        
        GameAction action = objectMapper.readValue(payload, GameAction.class);
        String gid = getRoomId(session);

        if(gameStateSvc.getGameStateBygid(gid).isEnd()){
            GameAction msg = new GameAction();
            msg.setAction("message");
            msg.setPlayer("System");
            msg.setGid(gid);
            msg.setTimestamp(new Date());
            String content = "The game has ended";
            msg.setContent(content);

            broadcastMessage(msg, gid);
            return;
        }

        // Handle different game actions
        switch (action.getAction()) {
            case "currentStateRequest":
                handleCurrentState(session, gid);
                break;
            case "rollDice":
                handleRollDice(session, action);
                break;
            case "buyProperty":
                handleBuyProperty(session, action);
                break;
            case "build":
                handleBuild(action);
                break;
            case "sell":
                handleSell(action, gid);
                break;
            case "endTurn":
                handleEndTurn(session, action);
                break;
            case "surrender":
            // handle lose
                handleSurrender(session, action);
                break;
            case "chat":
                broadcastMessage(action, gid);
                break;
            default:
                logger.warn("\n\nUnhandled action: {}", action.getAction());
                break;
        }
    }

    private void handleCurrentState(WebSocketSession session, String gid) throws Exception {        
        GameAction gameAction = new GameAction();
        gameAction.setAction("currentStateRequest");
        gameAction.setPlayer("System");
        gameAction.setGid(gid);
        TurnState currentTurnState = gameStateMap.get(gid);
        gameAction.setContent(objectMapper.writeValueAsString(currentTurnState));
        gameAction.setTimestamp(new Date());

        TextMessage message = new TextMessage(objectMapper.writeValueAsString(gameAction));
        session.sendMessage(message);
    }

    private void handleRollDice(WebSocketSession session, GameAction action) throws Exception {
        String gid = action.getGid();
        String player = action.getPlayer();
        GameAction gameAction = new GameAction();
        PlayerState currentPlayerState = getPlayerStateByName(gid, player);
        
        gameAction.setPlayer("System");
        gameAction.setGid(gid);

        // check if hasMoved
        if(getPlayerStateByName(gid, player).isHasMoved()){
            gameAction.setAction("message");  
            gameAction.setContent("You had moved in this turn");

            TextMessage message = new TextMessage(objectMapper.writeValueAsString(gameAction));

            session.sendMessage(message);

            gameAction.setAction("payRent");  //tumpang this function for update playerstate
            gameAction.setContent(objectMapper.writeValueAsString(currentPlayerState));
            message = new TextMessage(objectMapper.writeValueAsString(gameAction));
            session.sendMessage(message);
            return;
        }
        
        int diceNo = gameStateSvc.getGameStateBasicInfoByGid(gid).getDiceNo();
        int rollDice = new Random().nextInt(6 * diceNo) + 1;
        // int rollDice = 4;

        // TurnState currentTurnState = gameStateMap.get(gid);

        boolean passedGo = currentPlayerState.move(rollDice);        
        
        gameAction.setAction("rollDice");  
        gameAction.setContent(objectMapper.writeValueAsString(currentPlayerState));
        gameAction.setTimestamp(new Date());

        broadcastMessage(gameAction, gid);
        //handle pass go
        if(passedGo){
            handlePassGo(currentPlayerState, gid);
        }

        //handle payrent
        PropertyState propertyState = getPropertyStates(gid).get(currentPlayerState.getPosition());
        if(propertyState.isOwned() && !propertyState.getOwner().equals(player)){
            handlePayRent(session, currentPlayerState, propertyState, gid, rollDice);
        }

        //handle pay tax
        if(currentPlayerState.getPosition() == 4 || currentPlayerState.getPosition() == 38){
            handlePayTax(session, currentPlayerState, gid);            
        }

        //handle on fate/chance
        if(currentPlayerState.getPosition() == 2 || currentPlayerState.getPosition() == 7 || currentPlayerState.getPosition() == 17 ||
        currentPlayerState.getPosition() == 22 || currentPlayerState.getPosition() == 33 || currentPlayerState.getPosition() == 36){
            handleFate(currentPlayerState, gid);
        }
        //handle jail
        // step on jail
        if(currentPlayerState.getPosition() == 10){
            currentPlayerState.addJailTurn();
            gameAction.setAction("jailTurn");
            gameAction.setContent("Your step on jail " + currentPlayerState.getJailTurn() + " time.");
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(gameAction));

            session.sendMessage(message);
        }
        //step on gotoJail or step 2nd time on jail
        if(currentPlayerState.getPosition() == 30 || currentPlayerState.getJailTurn() == 2){
            handleToJail(session, currentPlayerState, gid);
        }
    }

    private void handlePassGo(PlayerState playerState, String gid) throws Exception {
        int salary = gameStateSvc.getGameStateBasicInfoByGid(gid).getSalary();
        playerState.passGo(salary);
        GameAction gameAction = new GameAction();
        gameAction.setAction("passGo");
        gameAction.setPlayer(playerState.getName());
        gameAction.setGid(gid);
        gameAction.setContent(objectMapper.writeValueAsString(getPlayerStates(gid)));
        gameAction.setTimestamp(new Date());

        // logger.info(gameAction.toString());
        broadcastMessage(gameAction, gid);
    }

    private void handleBuyProperty(WebSocketSession session, GameAction action) throws Exception {
        String gid = action.getGid();
        int index = Integer.parseInt(action.getContent());
        PropertyState propertyState = getProperStateById(gid, index);
        PlayerState playerState = getPlayerStateByName(gid, action.getPlayer());
        Property property = propertySvc.getPropertyBasicById(index);
        int cost = property.getCost();
        int rent = property.getRent().get(0);
        // System.out.println("\n\nrent: " + rent);
        GameAction gameAction = new GameAction();
            gameAction.setAction("buyProperty");
            gameAction.setPlayer("System");
            gameAction.setGid(gid);
            gameAction.setTimestamp(new Date());

        if(!propertyState.isOwned() && playerState.getMoney() >= cost){
            propertyState.setOwned(true);
            propertyState.setOwner(playerState.getName());
            propertyState.setStar(0);
            propertyState.setRent(rent);
            propertyState.setCanBuy(false);
            
            playerState.addProperty(property.getId(), property.getCost());
            
            String content = objectMapper.writeValueAsString(gameStateMap.get(gid));
            gameAction.setContent(content);

            broadcastMessage(gameAction, gid);

            gameAction.setAction("message");
            content = "<strong>" + playerState.getName() + "</strong> bought " + propertyState.getName() + ".";
            gameAction.setContent(content);
            broadcastMessage(gameAction, gid);
        }
        else if(propertyState.isOwned()){
            String content = property.getName() + " is sold";
            gameAction.setContent(content);
            gameAction.setAction("message");
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(gameAction));

            session.sendMessage(message);
        }
        else if(playerState.getMoney() < cost){
            gameAction.setAction("message");
            String content = playerState.getName() + "has insufficient money.";
            gameAction.setContent(content);
        }
        else {
            System.out.println("unhandle condition for buy property");
        }        
    }

    private void handleBuild(GameAction action) throws Exception {
        String gid = action.getGid();
        int index = Integer.parseInt(action.getContent());
        PropertyState propertyState = getProperStateById(gid, index);
        PlayerState playerState = getPlayerStateByName(gid, action.getPlayer());
        Property property = propertySvc.getPropertyBasicById(index);
        int build = property.getBuild();
        if(propertyState.getOwner().equals(playerState.getName()) && playerState.getMoney() >= build){
            int star = propertyState.getStar() + 1; 
            int rent = property.getRent().get(star);
            propertyState.build(star, rent);

            playerState.payMoney(build);

            GameAction gameAction = new GameAction();
            gameAction.setAction("build");
            gameAction.setPlayer("System");
            gameAction.setGid(gid);
            gameAction.setTimestamp(new Date());
            gameAction.setContent(objectMapper.writeValueAsString(gameStateMap.get(gid)));
            broadcastMessage(gameAction, gid);
        
            gameAction.setAction("message");
            if(star <= 3){
                gameAction.setContent("<strong>" + playerState.getName() + "</strong> build a new house at <strong>" + property.getName() + "</strong>.");
            }
            else{
                gameAction.setContent("<strong>" + playerState.getName() + "</strong> built a hotel at <strong>" + property.getName() + "</strong>.");
            }
            broadcastMessage(gameAction, gid);
        }
    }

    private void handleSell(GameAction action, String gid) throws Exception{
        PlayerState playerState = getPlayerStateByName(gid, action.getPlayer());
        int id = Integer.parseInt(action.getContent());
        Property property = propertySvc.getPropertyBasicById(id);
        PropertyState propertyState = getProperStateById(gid, id);
        if(propertyState.getOwner().equals(playerState.getName())){
            int index = playerState.getProperties().indexOf(id);
            
            //update player state
            playerState.getProperties().remove(index);
            playerState.receiveMoney(property.getCost());
    
            // update property state
            propertyState.setStar(0);
            propertyState.setRent(0);
            propertyState.setOwner("");
            propertyState.setCanBuy(true);
            propertyState.setOwned(false);
            
            GameAction gameAction = new GameAction();
                gameAction.setAction("sell");
                gameAction.setPlayer("System");
                gameAction.setGid(gid);
                gameAction.setTimestamp(new Date());
                gameAction.setContent(objectMapper.writeValueAsString(gameStateMap.get(gid)));
            broadcastMessage(gameAction, gid);
    
            gameAction.setAction("message");
            gameAction.setContent("<strong>" + playerState.getName() + "</strong> sell  <strong>" + property.getName() + "</strong> for $" + property.getCost() + ".");
            broadcastMessage(gameAction, gid);
        }
    }

    private void handleEndTurn(WebSocketSession session, GameAction action) throws Exception {
        String gid = action.getGid();
        PlayerState playerState = getPlayerStateByName(gid, action.getPlayer());
        playerState.setEndTurn(true);
        GameAction gameAction = new GameAction();
            gameAction.setAction("endTurn");
            gameAction.setPlayer("System");
            gameAction.setGid(gid);
            gameAction.setTimestamp(new Date());
            gameAction.setContent(objectMapper.writeValueAsString(gameStateMap.get(gid)));
        broadcastMessage(gameAction, gid);

        GameAction msg = new GameAction();
            msg.setAction("message");
            msg.setPlayer("System");
            msg.setGid(gid);
            msg.setTimestamp(new Date());
            String content = "<strong>" + playerState.getName() + "</strong> end the turn.";
            msg.setContent(content);
        broadcastMessage(msg, gid);

        // check if all no bankrupt player ended thier turn
        while(getPlayerStates(gid).stream().filter(p -> !p.isHasBankrupted()).allMatch(PlayerState::isEndTurn)){
            TurnState turnState = gameStateMap.get(gid);
            // check any player in jail
            List<PlayerState> jailedPlayers = turnState.getPlayerStates().stream().filter(p -> p.isInJail()).collect(Collectors.toList());

            if(jailedPlayers.size() >= 1){
                for(PlayerState ps : jailedPlayers){
                    handleInJail(ps, gid);
                }
            }

            //  filtered bankrupted player below and reset move and endturn
            turnState.nextTurn();
            
            gameStateSvc.updateTurnStateandPlayerScore(gid, turnState);
                gameAction.setAction("newTurn");
                gameAction.setTimestamp(new Date());
                gameAction.setContent(objectMapper.writeValueAsString(gameStateMap.get(gid)));
            broadcastMessage(gameAction, gid);
            
            msg.setContent("Turn No." + turnState.getCurrentTurn() + " started.");
            broadcastMessage(msg, gid);
        }
       
    }

    private void handleSurrender(WebSocketSession session, GameAction action) throws Exception {
        String gid = action.getGid();
        PlayerState bankruptedPlayerState = getPlayerStateByName(gid, action.getPlayer());
        handleBankrupt(session, bankruptedPlayerState, gid);
    }

    private void handlePayRent(WebSocketSession session, PlayerState currentPlayerState, PropertyState propertyState, String gid, int rollDice) throws Exception {
        
        int rent = propertyState.getRent();
        PlayerState ownerPlayerState = getPlayerStateByName(gid, propertyState.getOwner());
        int position = currentPlayerState.getPosition();
        // for railway rental
        if (position == 5 || position == 15 || position == 25 || position == 35){
            long count = getPropertyStates(gid).stream()
                .filter(p -> p.getId() == 5 || p.getId() == 15 || p.getId() == 25 || p.getId() == 35)
                .filter(p -> p.getOwner() != null) //remove railway without owner
                .filter(p -> p.getOwner().equals(propertyState.getOwner()))
                .count();

            // railway rental
            List<Integer> rental = Arrays.asList(100, 250, 500, 1000);

            if (count > 0 && count <= rental.size()) {
                rent = rental.get((int) count - 1);
            }
        }
        else if(position == 12 || position == 28){
            long count = getPropertyStates(gid).stream()
                .filter(p -> p.getId() == 12 || p.getId() == 28)
                .filter(p -> p.getOwner() != null) //remove railway without owner
                .filter(p -> p.getOwner().equals(propertyState.getOwner()))
                .count();

            int fee = 50;
            rent = rollDice * fee * (int) count;
        }

        GameAction gameAction = new GameAction();
        gameAction.setAction("payRent");
        gameAction.setPlayer("System");
        gameAction.setGid(gid);
        gameAction.setTimestamp(new Date());
        // check if landlord in jal
        if(!ownerPlayerState.isInJail()){
            // check if player bankrupt
            if(currentPlayerState.payMoney(rent)){
                ownerPlayerState.receiveMoney(rent);
    
                gameAction.setContent(objectMapper.writeValueAsString(getPlayerStates(gid)));
                broadcastMessage(gameAction, gid);
    
                gameAction.setAction("message");
                gameAction.setContent("<strong>" + currentPlayerState.getName() + "</strong> pay $" + rent + " to <strong>" + ownerPlayerState.getName() + "</strong>.");
                broadcastMessage(gameAction, gid);
            }
            else{
                ownerPlayerState.receiveMoney(currentPlayerState.getMoney());
                gameAction.setContent(objectMapper.writeValueAsString(getPlayerStates(gid)));
                broadcastMessage(gameAction, gid);
    
                gameAction.setAction("message");
                gameAction.setContent("<strong>" + currentPlayerState.getName() + "</strong>  has no enought money to pay $" + rent + " to <strong>" + ownerPlayerState.getName() + "</strong>.");     
                broadcastMessage(gameAction, gid);
                
                gameAction.setContent("<strong>" + ownerPlayerState.getName() + "</strong> receive only $" + currentPlayerState.getMoney() + " from <strong>" + currentPlayerState.getName() + "</strong>.");     
                broadcastMessage(gameAction, gid);
    
                handleBankrupt(session, currentPlayerState, gid);
            }
        }
        else{
            gameAction.setAction("message");
            gameAction.setContent(ownerPlayerState.getName() + " is in jail and unable to collect the rent.");
            broadcastMessage(gameAction, gid);
        }
    }

    private void handlePayTax(WebSocketSession session, PlayerState currentPlayerState, String gid) throws Exception {
        int rent = 200;
        GameAction gameAction = new GameAction();
        gameAction.setAction("payRent");
        gameAction.setPlayer("System");
        gameAction.setGid(gid);
        gameAction.setTimestamp(new Date());
        if(currentPlayerState.payMoney(rent)){
            gameAction.setContent(objectMapper.writeValueAsString(getPlayerStates(gid)));
            broadcastMessage(gameAction, gid);

            gameAction.setAction("message");
            gameAction.setContent("<strong>" + currentPlayerState.getName() + "</strong> pay $200 of tax.");
            broadcastMessage(gameAction, gid);
        }
        else{
            handleBankrupt(session, currentPlayerState, gid);
        }
    }

    private void handleFate(PlayerState currentPlayerState, String gid) throws Exception {
        GameAction gameAction = new GameAction();
            gameAction.setAction("message");
            gameAction.setPlayer("System");
            gameAction.setGid(gid);
            gameAction.setTimestamp(new Date());
            gameAction.setContent("<strong>" + currentPlayerState.getName() + "</strong> draw a fate/chance.\n" + gameStateMap.get(gid).getFates().removeFirst());
        
        broadcastMessage(gameAction, gid);

        if(gameStateMap.get(gid).getFates().size() == 0){
            gameStateMap.get(gid).setFates(gameStateSvc.initialiseFates());
        }

    }

    private void handleToJail(WebSocketSession session, PlayerState jailedPlayerState, String gid) throws Exception{
        GameAction gameAction = new GameAction();
        gameAction.setAction("toJail");
        gameAction.setPlayer(jailedPlayerState.getName());
        gameAction.setGid(gid);
        gameAction.setTimestamp(new Date());
        jailedPlayerState.sendtoJail();
        
        // update playerstate 
        gameAction.setContent(objectMapper.writeValueAsString(getPlayerStates(gid)));
        broadcastMessage(gameAction, gid);

        // invoke end turn
        gameAction.setAction("plsEndTurn");
        gameAction.setContent("To end turn");
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(gameAction));
        session.sendMessage(message);

        // message
        gameAction.setPlayer("System");
        gameAction.setAction("message");
        gameAction.setContent(jailedPlayerState.getName() + " sent to jail.");
        broadcastMessage(gameAction, gid);
    }

    private void handleInJail(PlayerState jailedPlayerState, String gid) throws Exception{
        boolean isReleased = jailedPlayerState.releasedFromJail();
        GameAction gameAction = new GameAction();
        gameAction.setPlayer("System");
        gameAction.setGid(gid);
        gameAction.setTimestamp(new Date());
        if(isReleased){
            gameAction.setAction("message");
            gameAction.setContent(jailedPlayerState.getName() + " is released from jail.");
            broadcastMessage(gameAction, gid);
        }
        else{
            // send msg
            gameAction.setAction("message");
            if(jailedPlayerState.getJailTurn() == 2){
                gameAction.setContent(jailedPlayerState.getName() + " still has 2 turn in jail.");
            }
            else if(jailedPlayerState.getJailTurn() == 1){
                gameAction.setContent(jailedPlayerState.getName() + " still has 1 turn in jail.");
            }
            broadcastMessage(gameAction, gid);
        }
    }

    private void handleBankrupt(WebSocketSession session, PlayerState bankruptedPlayerState, String gid) throws Exception {
        bankruptedPlayerState.setHasBankrupted(true);
        GameAction gameAction = new GameAction();
        gameAction.setPlayer("System");
        gameAction.setGid(gid);
        gameAction.setTimestamp(new Date());

        // send personal message to the specific player
        gameAction.setAction("lose");
        gameAction.setContent("You has bankrupted !");
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(gameAction));
        session.sendMessage(message);

        // update user gid to null
        userInfoRepo.updateUserGameStatusByName(bankruptedPlayerState.getName(), null, false);

        //if left only 1 player not bankrupt declare WIN
        List<PlayerState> playerStates = getPlayerStates(gid).stream().filter(p -> !p.isHasBankrupted()).toList();
        if(playerStates.size() == 1){
            gameStateSvc.updateTurnStateandPlayerScore(gid, gameStateMap.get(gid));
            GameState gameState = gameStateSvc.getGameStateBygid(gid);
            gameState.setEnd(true);

            gameAction.setAction("win");
            gameAction.setContent("CONGRATS !\n" + playerStates.get(0).getName() + " win the game !");
            
            broadcastMessage(gameAction, gid);
            
            // update user gid to null / "" ?
            userInfoRepo.updateUserGameStatusByName(playerStates.get(0).getName(), "", false);
            gameStateMap.get(gid).nextTurn();
            gameStateSvc.updateGameState(gameState);
            
            gameStateMap.remove(gid);
            return;
        };

        List<PropertyState> propertyStates = getPropertyStates(gid);
        List<Integer> properties = bankruptedPlayerState.getProperties();
        for(int i = 0; i < properties.size(); i++){
            propertyStates.get(properties.get(i)).clearState();
        }
        // update game state to remaining player
        gameAction.setAction("bankrupt");
        gameAction.setPlayer(bankruptedPlayerState.getName());
        gameAction.setContent(objectMapper.writeValueAsString(gameStateMap.get(gid)));
        broadcastMessage(gameAction, gid);

        // broadcast msg
        gameAction.setAction("message");
        gameAction.setPlayer("System");
        gameAction.setContent("<strong>" + bankruptedPlayerState.getName() + "</strong> bankrupted. \nAll the properties under " + bankruptedPlayerState.getName() + " are released back to market");
        broadcastMessage(gameAction, gid);

        bankruptedPlayerState.setProperties(new LinkedList<>());
        bankruptedPlayerState.setMoney(0);
    }

    private void broadcastMessage(GameAction action, String gid) throws Exception {
        List<WebSocketSession> sessionsInRoom = roomSessions.getOrDefault(gid, new CopyOnWriteArrayList<>());
        TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(action));
        
        for (WebSocketSession sess : sessionsInRoom) {
            if (sess.isOpen()) {
                sess.sendMessage(textMessage);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String gid = getRoomId(session);
        List<WebSocketSession> sessionsInRoom = roomSessions.get(gid);
        if (sessionsInRoom != null) {
            sessionsInRoom.remove(session);
            if (sessionsInRoom.isEmpty()) {
                roomSessions.remove(gid);

                GameState gameState = gameStateSvc.getGameStateBygid(gid);
                gameState.setTurnStates(gameStateMap.get(gid));
                gameStateSvc.updateGameState(gameState);
                gameStateMap.remove(gid);
            }
        }
    }

    private String getRoomId(WebSocketSession session) {
        // extract the roomId from the session URI
        return session.getUri().getPath().split("/")[3];
    }

    private List<PropertyState> getPropertyStates(String gid){
        return gameStateMap.get(gid).getPropertyStates();
    }

    private PropertyState getProperStateById(String gid, int index){
        return gameStateMap.get(gid).getPropertyStates().get(index);
    }

    private List<PlayerState> getPlayerStates(String gid){
        return gameStateMap.get(gid).getPlayerStates();
    }

    private PlayerState getPlayerStateByName(String gid, String name){
        return gameStateMap.get(gid).getPlayerState(name);
    }
}
