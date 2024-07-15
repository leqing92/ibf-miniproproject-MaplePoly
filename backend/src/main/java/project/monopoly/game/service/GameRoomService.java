package project.monopoly.game.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import project.monopoly.game.models.GameRoom;
import project.monopoly.game.repository.GameRoomRepository;
import project.monopoly.security.entity.UserInfo;
import project.monopoly.security.service.UserInfoService;

@Service
public class GameRoomService {
    
    @Autowired
    private GameRoomRepository gameRepo;

    @Autowired
    private UserInfoService userInfoSvc;

    public GameRoom createGameRoom(String name){
        GameRoom game = new GameRoom();
        String gid = UUID.randomUUID().toString().substring(0, 8);
        // String name = userInfoSvc.getCurrentUserName();

        // use to make sure gid is unique
        while (gameRepo.findGameRoomById(gid).isPresent()) {
            gid = UUID.randomUUID().toString().substring(0, 8);
        }

        game.setGid(gid);
        game.setStart(false);
        game.setOwner(name);

        List<String> players = new LinkedList<>();
        players.add(name);
        game.setPlayers(players);

        // create game room and update the owner player state
        game = gameRepo.createGameRoom(game);
        userInfoSvc.updateUserGameStatusByName(name, gid, false);

        return game;
    }
    
    public GameRoom joinGameRoom(String gid, String name){
        GameRoom game = findGameRoomById(gid).get();
        // String name = userInfoSvc.getCurrentUserName();

        game.getPlayers().add(name);

        gameRepo.updateGameRoom(game);
        userInfoSvc.updateUserGameStatusByName(name, gid, false);
        // System.out.println(game.toString());
        return game;
    }

    public ResponseEntity<String> quitGameRoom(String gid, String name){

        String email = userInfoSvc.getCurrentUserEmail();
        UserInfo user = userInfoSvc.findByEmail(email).get();

        Optional<GameRoom> existingGame = gameRepo.findGameRoomById(gid);
        if (existingGame.isPresent()) {
            GameRoom game = existingGame.get();
            if(!game.getPlayers().contains(name) || game.isStart() || user.getInGame()){
                return ResponseEntity.status(400).body("Bad request");
            }
            int index = game.getPlayers().indexOf(name);
            game.getPlayers().remove(index);

            gameRepo.updateGameRoom(game);
            userInfoSvc.updateUserGameStatusByName(name, "", false);
            return ResponseEntity.status(200).body("Quit game sucessfully");
        }
        else {
            return ResponseEntity.status(404).body("Game with GID:" + gid + " is not found.");
        }
    }

    public ResponseEntity<String> deleteGameRoom(String gid){
        Optional<GameRoom> existingGame = gameRepo.findGameRoomById(gid);
        if (existingGame.isPresent()) {
            // not allow to delete if game is started
            if(!existingGame.get().isStart()){
                for(String name : existingGame.get().getPlayers()){
                    userInfoSvc.updateUserGameStatusByName(name, null, false);
                }
                gameRepo.deleteGameRoomById(gid);
                return ResponseEntity.status(200).body("Game deleted");
            }
            return ResponseEntity.status(400).body("Game has started and not allowed to delete");
        }
        return ResponseEntity.status(404).body("Game with GID:" + gid + " is not found.");
    }

    public Optional<GameRoom> findGameRoomById(String gid){
        return gameRepo.findGameRoomById(gid);
    }

    public List<GameRoom> findAllGameRooms(){        
        return gameRepo.findAllGameRooms();
    } 
}
