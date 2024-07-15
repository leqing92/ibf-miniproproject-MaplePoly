package project.monopoly.game.models.character;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Animation {
    private List<Coordinate> coordinates;
   
}
