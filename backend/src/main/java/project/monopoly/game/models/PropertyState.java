package project.monopoly.game.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyState {
    
    private int id;
    private String name;    
    private boolean isOwned;
    private boolean canBuy;
    private String owner;
    private int star;
    private int rent;

    public void build(int star, int rent){
        this.star = star;
        this.rent = rent;
    }

    public void clearState(){
        this.setOwned(false);
        this.setCanBuy(true);
        this.setOwner("");
        this.setStar(0);
        this.setRent(0);
    }
}
