package project.monopoly.game.models;

import java.util.List;

import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnState {
    
    @Id    
    private int currentTurn;
    private List<PlayerState> playerStates;
    private List<PropertyState> propertyStates;
    private List<String> fates;

    public void nextTurn(){
        this.currentTurn ++;

        // // release player from jail
        // this.getPlayerStates().stream()
        //     .filter(p -> p.isInJail())
        //     .forEach(p -> p.releasedFromJail());

        // start new turn
        this.getPlayerStates().stream()
            .filter(p-> !p.isHasBankrupted() && !p.isInJail())                
            .forEach(p -> {
                p.setEndTurn(false);
                p.setHasMoved(false);
            });
    }
    
    public PlayerState getPlayerState(String player) {
        return playerStates.stream()
                .filter(playerState -> playerState.getName().equals(player))
                .findFirst()
                .orElse(null);
    }

    public void updatePlayerPosition(String playerName, int newPosition) {
        PlayerState playerState = getPlayerState(playerName);
        if (playerState != null) {
            playerState.setPosition(newPosition);
        }
    }
}
