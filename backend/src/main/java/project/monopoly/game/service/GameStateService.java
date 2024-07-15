package project.monopoly.game.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import project.monopoly.game.models.GameRoom;
import project.monopoly.game.models.GameRoomInfo;
import project.monopoly.game.models.GameSetupForm;
import project.monopoly.game.models.GameState;
import project.monopoly.game.models.PlayerScore;
import project.monopoly.game.models.PlayerState;
import project.monopoly.game.models.PlayerWealth;
import project.monopoly.game.models.PlayerWealthAtEachTurn;
import project.monopoly.game.models.Property;
import project.monopoly.game.models.PropertyState;
import project.monopoly.game.models.TurnState;
import project.monopoly.game.models.character.Character;
import project.monopoly.game.repository.CharacterRepository;
import project.monopoly.game.repository.GameRoomRepository;
import project.monopoly.game.repository.GameStateRepository;
import project.monopoly.game.repository.PlayerScoreRepository;
import project.monopoly.game.repository.PropertyRepository;
import project.monopoly.security.service.UserInfoService;

@Service
public class GameStateService {

    @Autowired
    PropertyRepository propertyRepo;

    @Autowired
    GameStateRepository gamestateRepo;

    @Autowired
    UserInfoService userInfoSvc;

    @Autowired
    GameRoomRepository gameRoomRepo;

    @Autowired
    CharacterRepository chacRepo;       

    @Autowired
    PlayerScoreService playerScoreSvc;
    
    public GameState getGameStateBygid(String gid){
        Optional<GameState> gameState = gamestateRepo.getGameStateBygid(gid);
        if(gameState.isPresent()){
            return gameState.get();
        }
        else{
            return null;
        }
    }

    public ResponseEntity<Object> getGameBasicInfoById(String gid){        

        Optional<GameState> gameStateOpt = gamestateRepo.getGameStateBygid(gid);
        if(gameStateOpt.isPresent() && isInGamePlayer(gid)){
            GameState gameState = gameStateOpt.get();
            List<Property> properties = propertyRepo.getPropertyBasic("Default");
            
            // for List<TurnState>
            // List<String> characterNameList = gameState.getTurnStates().get(0).getPlayerStates().stream()
            List<String> characterNameList = gameState.getTurnStates().getPlayerStates().stream()
                .map(playstate -> playstate.getCharacter())
                .collect(Collectors.toList());
            List<Character> characters = new LinkedList<>();

            for(String name : characterNameList){
                Character character = chacRepo.getCharacterByName(name);
    
                characters.add(character);
            }
           
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("properties", properties);
            responseMap.put("characters", characters);

            return ResponseEntity.status(200).body(responseMap);
           
        }
        else if(!isInGamePlayer(gid)){
            return ResponseEntity.status(400).body("You are not a player of game with ID : " + gid);
        }
        
        return ResponseEntity.status(404).body("Game with " + gid + " is not found.");
    }

    public GameState getGameStateBasicInfoByGid(String gid){
        return gamestateRepo.getGameStateBasicInfoByGid(gid).get();
    }

    // use to update the turnstate when all players finish their turn
    public void updateTurnStateandPlayerScore(String gid, TurnState newTurnState){
        Optional<GameState> gameStateOpt = gamestateRepo.getGameStateBygid(gid);
        Optional<PlayerScore> playScoreOpt = playerScoreSvc.getPlayerScoreByGid(gid);

        if (gameStateOpt.isPresent() && playScoreOpt.isPresent()) {
            // game state update
            GameState gameState = gameStateOpt.get();
            // for all turnstate (model to chang to List<TurnState>)
            // List<TurnState> turnStates = gameState.getTurnStates();
            // turnStates.add(newTurnState); 
            gameState.setTurnStates(newTurnState);

            gamestateRepo.saveGameState(gameState);

            // player score
            PlayerScore playerScore = playScoreOpt.get();
            List<PlayerWealthAtEachTurn> playerWealthAtEachTurns = playerScore.getPlayerWealthAtEachTurn();
            PlayerWealthAtEachTurn playerWealthAtEachTurn = new PlayerWealthAtEachTurn(
                playerWealthAtEachTurns.size(),
                playerScoreSvc.updatedPlayerWealth(newTurnState.getPlayerStates(), 
                newTurnState.getPropertyStates()));
            playerWealthAtEachTurns.add(playerWealthAtEachTurn);

            playerScoreSvc.savePlayerScore(playerScore);

        } 
        else {
            throw new IllegalArgumentException("Game with gid " + gid + " not found.");
        }
    }

    // use to update game state when game ended | all men leave the room
    public void updateGameState(GameState gamestate){
        gamestateRepo.saveGameState(gamestate);
    }

    public void initialiseGameState(GameRoomInfo gameRoomInfo){
        //initialise Player State
        List<PlayerState> playerStates = new LinkedList<>();
        List<PlayerWealth> playersWealth = new LinkedList<>();

        List<PropertyState> propertyStates = initialisePropertyState();
        List<String> fates = initialiseFates();

        for(GameSetupForm players : gameRoomInfo.getPlayers()){
            PlayerState playerState = initialisePlayerState(players.getName(), players.getCharacter(), gameRoomInfo.getFund());
            playerStates.add(playerState);
            playersWealth.add(playerScoreSvc.initialisePlayerWealth(playerState));
            // updare user to inGame
            userInfoSvc.updateUserGameStatusByName(players.getName(), gameRoomInfo.getGid(), true);
        }
        
        //initalise Turn info
        TurnState initialTurnState = new TurnState(0, playerStates, propertyStates, fates);
        // List<TurnState> turnStates = new LinkedList<>();
        // turnStates.add(initialTurnState);

        // for gamemap in GameHandler to directly modify from
        // TurnState turnState = new TurnState(1, playerStates, propertyStates, fates);
        // turnStates.add(turnState);
        
        PlayerWealthAtEachTurn playerWealthAtEachTurn = new PlayerWealthAtEachTurn(0, playersWealth);
        List<PlayerWealthAtEachTurn> playerWealthAtEachTurns = new LinkedList<>();
        playerWealthAtEachTurns.add(playerWealthAtEachTurn);
        PlayerScore playerScore = new PlayerScore(gameRoomInfo.getGid(), playerWealthAtEachTurns);

        // initilise game state 
        GameState gameState = new GameState(gameRoomInfo.getGid(), initialTurnState, false, gameRoomInfo.getDiceNo(), gameRoomInfo.getSalary());
        
        // save gamestate to mongo
        gamestateRepo.saveGameState(gameState);
        gameRoomRepo.updateGameRoomToStarted(gameRoomInfo.getGid());

        playerScoreSvc.savePlayerScore(playerScore);
    }

    // initialise Player State
    private PlayerState initialisePlayerState(String name, String character, int fund){ 
        return new PlayerState(name, character, new LinkedList<>(), fund, 0, false, false, false, 0, false);
    }  

    // initialise Property State
    private List<PropertyState> initialisePropertyState(){
        List<PropertyState> propertyStates = new LinkedList<>();
        List<Integer> specialTileList = Arrays.asList(0, 2, 4, 7, 10, 17, 20, 22, 30, 33, 36, 38, 40);
        
        List<Property> propertyBasics = propertyRepo.getPropertyBasic("Default");
        for(int i = 0; i < propertyBasics.size(); i++){
            int propertyId = propertyBasics.get(i).getId();
            boolean canBuy = true;
            if(specialTileList.contains(propertyId)){
                canBuy = false;
            }
            propertyStates.add(new PropertyState(propertyId, propertyBasics.get(i).getName(), false, canBuy, null, 0, 0));
        }

        return propertyStates;
    }

    public List<String> initialiseFates(){
        List<String> fates = Arrays.asList("You found a 'Free parking' card, but it is only valid at you land!",
            "You pulled a 'Get out of jail free' card, but they don't charge for getting out either.",
            "You drew a 'Go to jail' card, please report to jail yourself ASAP!",
            "You received a 'Go directly to Go' card, please continue to roll dice to reach there later",
            "You got a 'Advance to MBS' card, please continue to roll dice to reach there later",
            "You landed on 'Income Tax Refund' but your income doesn't require you to pay tax, so no refund from IRAS.",
            "You drew a 'Go Back 3 Spaces' card, but the game board function can only go forward now!",
            "You received a 'Collect $200' card. That's it, just the card, not the money! HAHAHAHA!",
            "You got a 'You Have Won First Prize in a Beauty Contest' card.",
            "Today is your birthday. Happy birthday!",
            "You drew a 'Collect $200' card, but too bad, I had no time to write the function to bank it in for you.",
            "This card is just to make up the collection.",
            "Good luck to you. May you win the game!",
            "You drew a 'Pay Land Tax' card. Please calculate the related tax and find your way to pay it.",
            "You received an 'Insurance Matures' card, but the payout is $0 due to inflation!",
            "This is just to test the fate/chance.",
            "Thank you for playing the game.",
            "Sorry for wasting your time with this card.",
            "You got a 'Steal a Property' card, but stealing is illegal, so it is confiscated.",
            "You got a scam and lost $100. Just kidding...");
            
        Collections.shuffle(fates);

        return new LinkedList<>(fates);
    }
    
    public boolean isInGamePlayer(String gid){
        String currentUsername = userInfoSvc.getCurrentUserName();
        Optional<GameRoom> gameRoom = gameRoomRepo.findGameRoomById(gid);
        if(gameRoom.isPresent())
            return gameRoomRepo.findGameRoomById(gid).get().getPlayers().contains(currentUsername);
        else
            return false;
    }
}
