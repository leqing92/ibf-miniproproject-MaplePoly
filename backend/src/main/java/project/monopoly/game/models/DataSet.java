package project.monopoly.game.models;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSet {
    private String label;
    private List<Integer> money;
    private List<Integer> propertyValue;
    private List<Integer> total;

    public DataSet(String label) {
        this.label = label;
        this.money = new LinkedList<>();
        this.propertyValue = new LinkedList<>();
        this.total = new LinkedList<>();
    }

    
}
