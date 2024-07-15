package project.monopoly.game.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomInfo {
    
    String gid;
    List<GameSetupForm> players;
    int diceNo;
    int salary;
    int fund;
}