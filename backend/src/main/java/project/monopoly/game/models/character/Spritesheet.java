package project.monopoly.game.models.character;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Spritesheet {

    private Animation idle;
    private Animation run;
    private Animation ko;  

}
