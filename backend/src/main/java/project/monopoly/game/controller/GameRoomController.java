package project.monopoly.game.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import project.monopoly.game.models.GameRoom;
import project.monopoly.game.models.GameRoomInfo;
import project.monopoly.game.models.character.CharacterSummary;
import project.monopoly.game.repository.CharacterRepository;
import project.monopoly.game.service.GameRoomService;
import project.monopoly.game.service.GameStateService;
import project.monopoly.game.service.GiphyService;
import project.monopoly.security.entity.UserInfo;
import project.monopoly.security.service.UserInfoService;

// Game room / game center function
@RestController
@RequestMapping("/api/gamerooms")
public class GameRoomController {
    @Autowired
    private GameRoomService gameSvc;

    @Autowired
    private CharacterRepository chacRepo;

    @Autowired
    private GameStateService gameStateSvc;

    @Autowired
    private GiphyService giphySvc;

    @Autowired
    private UserInfoService userInfoSvc;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<GameRoom>> getGameRoomList() {
        List<GameRoom> games = gameSvc.findAllGameRooms();
        return ResponseEntity.status(200).body(games);
    }
    
    @GetMapping("/get/{gid}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> findGameById(@PathVariable String gid){
        Optional<GameRoom> game = gameSvc.findGameRoomById(gid);
        boolean isInGamePlayer = gameStateSvc.isInGamePlayer(gid);

        if(game.isPresent() && isInGamePlayer){
            return ResponseEntity.status(200).body(game.get());
        }
        else if(game.isPresent() && !isInGamePlayer){
            return ResponseEntity.status(400).body("Please join the Game Room ID : " + gid + " by clicking 'JOIN' button in Game Center.");
        }
        else{            
            return ResponseEntity.status(404).body("Game Room ID : " + gid + " is not found.");
        }
    }

    @GetMapping("/chac")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<CharacterSummary>> getCharacterSummary() {
        return ResponseEntity.status(200).body(chacRepo.getAllCharacterSummary());
    }    

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> createGameRoom(@RequestParam String name) {
        String  email = userInfoSvc.getCurrentUserEmail();
        UserInfo user = userInfoSvc.findByEmail(email).get();
        System.out.println(user.getName());
        System.out.println(user.getInGame());
        if(!user.getInGame()){
            GameRoom game = gameSvc.createGameRoom(name);
            return ResponseEntity.status(201).body(
                Json.createObjectBuilder().add("gid", game.getGid()).build().toString()
            );
        }
        else{
            return ResponseEntity.status(400).body("You are already in game with GID: " + user.getGid());
        }
    }

    @PostMapping("/join/{gid}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> joinGameRoom(@PathVariable String gid, @RequestParam String name) {
        Optional<GameRoom> gameOpt = gameSvc.findGameRoomById(gid);
        String  email = userInfoSvc.getCurrentUserEmail();
        UserInfo user = userInfoSvc.findByEmail(email).get();

        if(gameOpt.isPresent()){
            GameRoom game = gameOpt.get();
            Boolean hasjoined = game.getPlayers().contains(name);
            Boolean notFull = game.getPlayers().size() < 4;
            Boolean isStart = game.isStart();

            Boolean inGame = user.getInGame();
            String userGid = user.getGid();

            if(!hasjoined && notFull && !isStart && !inGame){
                return ResponseEntity.status(200).body(gameSvc.joinGameRoom(gid, name));
            }
            else if(hasjoined)
                return ResponseEntity.status(200).body(
                    Json.createObjectBuilder().add("status", "info")
                        .add("message", "You are already inside a game.")
                        .build()
                );
            else if (!notFull)
                return ResponseEntity.status(400).body("The game is full.");
            else if (isStart)
                return ResponseEntity.status(400).body("The game has started.");
            else if(inGame)
                return ResponseEntity.status(400).body("You are in game with GID " + userGid);
            else
                return ResponseEntity.status(400).body("Other error to be handled.");
        }
        else{
            return ResponseEntity.status(404).body("Game Not Found");
        }
    }

    @PostMapping("/quit/{gid}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> quitGame(@PathVariable String gid, @RequestParam String name) {
        return gameSvc.quitGameRoom(gid, name);
    }

    @DeleteMapping("/delete/{gid}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> deleteGame(@PathVariable String gid) {
        return gameSvc.deleteGameRoom(gid);
    }

    @PostMapping("/giphy/search")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<String>> searchGiphy(@RequestParam String q) {
        List<String> gipyhys = giphySvc.search(q, 10);
        if(!gipyhys.isEmpty())
            return ResponseEntity.status(200).body(gipyhys);
        else
            return ResponseEntity.status(404).body(null);
    }

// start game function
    @PostMapping("/room/{gid}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> startGame(@PathVariable String gid, @RequestBody GameRoomInfo gameInfo) {

        GameRoom game = gameSvc.findGameRoomById(gid).get();
        if(null != game){
            if(game.isStart()){
                return ResponseEntity.status(400).body("Game has started");
            }     
            else {
                try{
                    gameStateSvc.initialiseGameState(gameInfo);
                    return ResponseEntity.status(201).body(
                        Json.createObjectBuilder().add("status", "info")
                            .add("message", "ok")
                            .build().toString()
                    );
                }
                catch(Exception e){
                    e.printStackTrace();
                    return ResponseEntity.status(500).body(e.getMessage());
                }
            }
        }
        else{
            return ResponseEntity.status(404).body("Game not found");
        }
    }   
    
}