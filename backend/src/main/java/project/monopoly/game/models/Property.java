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
public class Property {
    @Id
    @Field(name="id")
    private int id;
    private String name;
    private String cols;
    private String rows;
    private String color;
    private int cost;
    private String classNames; 
    private int build;   
    private List<Integer> rent;
}
