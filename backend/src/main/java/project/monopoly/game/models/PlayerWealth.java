package project.monopoly.game.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWealth {
    
    private String name;
    private int money;
    private List<Integer> properties;
    private int propertyValue;
}
