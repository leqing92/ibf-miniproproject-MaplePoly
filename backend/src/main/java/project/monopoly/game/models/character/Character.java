package project.monopoly.game.models.character;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document (collection = "characters")
public class Character {
    @Id
    private String id;
    private String url;
    private String dieUrl;
    private String spriteurl;
    private String name;
    private int spriteWidth;
    private int spriteHeight;
    private Spritesheet spritesheet;    
}
