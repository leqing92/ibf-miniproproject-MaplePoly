package project.monopoly.game.models;

import java.util.List;

import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {

    @Id
    private String gid;
    private String owner;
    private boolean isStart;
    private List<String> players;   
}
