package project.monopoly.game.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWealthAtEachTurn {
    
    private int turn;
    private List<PlayerWealth> playersWealth;
}
