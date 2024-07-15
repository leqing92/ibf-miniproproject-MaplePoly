package project.monopoly.game.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerState {
    
    private String name;
    private String character;
    private List<Integer> properties;
    private int money;
    private int position;
    private boolean hasMoved;
    private boolean endTurn;
    private boolean inJail;
    private int jailTurn;
    private boolean hasBankrupted;

    public boolean move(int steps) {
        int oldPosition = this.position;
        int newPosition = (this.position + steps) % 40; // 0 to 39
        setHasMoved(true);

        // check if passed go
        if(newPosition < oldPosition){
            this.position = newPosition < 0 ? newPosition + 40 : newPosition;
            return true;
        }
        this.position = newPosition < 0 ? newPosition + 40 : newPosition;
        return false;
    }

    public void passGo(int salary){
        this.money += salary;
    }

    public void addProperty (int id, int cost){
        this.properties.add(id);
        this.money -= cost;
    }

    // return true if pay successfully else handle bankrupt
    public boolean payMoney(int rent){
        if(this.money >= rent){
            this.money -= rent;
            return true;
        }
        else{
            return false;
        }
    }

    public void receiveMoney(int rent){
        this.money += rent;
    }

    public void addJailTurn(){        
        this.jailTurn += 1;
    }

    public void sendtoJail(){
        this.setInJail(true);
        this.setJailTurn(3);
        this.setPosition(10);
        this.setEndTurn(true);
    }

    public boolean releasedFromJail(){
        this.jailTurn -= 1;
        if(this.jailTurn == 0){
            this.setInJail(false);
            setHasMoved(false);
            setEndTurn(false);
            return true;
        }
        return false;
    }
}
