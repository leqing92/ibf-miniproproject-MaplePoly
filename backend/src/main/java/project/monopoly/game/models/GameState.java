package project.monopoly.game.models;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gamestates")
public class GameState {
    @Id
    @Field(name="_id")
    private String gid;
    private TurnState turnStates;
    private boolean isEnd;
    private int diceNo;
    private int salary;
    
    // for List<TurnState>
    // public TurnState getCurrentTurnState() {
    //     return turnStates.get(turnStates.size() - 1);
    // }

    // public void addTurnState(TurnState turnState) {
    //     turnState.nextTurn();
    //     turnStates.add(turnState);
    // }
}
