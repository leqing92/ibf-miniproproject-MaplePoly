package project.monopoly.game.models;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScore {

    @Id
    @Field(name="_id")
    private String gid;
    private List<PlayerWealthAtEachTurn> playerWealthAtEachTurn;
}
