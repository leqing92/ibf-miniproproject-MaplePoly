package project.monopoly.game.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInMongo {
    private String id; 
    private String name;
    private List<Property> property;
}
