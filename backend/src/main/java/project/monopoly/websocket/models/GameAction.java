package project.monopoly.websocket.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameAction {
    private String action;
    private String player;
    private String gid;
    private Date timestamp;
    private String content;
   
}

