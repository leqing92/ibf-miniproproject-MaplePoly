package project.monopoly.game.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import project.monopoly.game.models.ChartData;
import project.monopoly.game.models.DataSet;
import project.monopoly.game.models.PlayerScore;
import project.monopoly.game.models.PlayerState;
import project.monopoly.game.models.PlayerWealth;
import project.monopoly.game.models.PlayerWealthAtEachTurn;
import project.monopoly.game.models.Property;
import project.monopoly.game.models.PropertyState;
import project.monopoly.game.repository.PlayerScoreRepository;
import project.monopoly.game.repository.PropertyRepository;

@Repository
public class PlayerScoreService {
    @Autowired
    PropertyRepository propertyRepo;

    @Autowired
    PlayerScoreRepository playerScoreRepo;

    public void savePlayerScore(PlayerScore playerScore){
        playerScoreRepo.savePlayerScore(playerScore);
    }

    public Optional<PlayerScore> getPlayerScoreByGid(String gid){
        return playerScoreRepo.getPlayerScoreByGid(gid);
    }

    public ResponseEntity<Object> getChartData(String gid){
        Optional<PlayerScore> playScoreOpt = playerScoreRepo.getPlayerScoreByGid(gid);
        ChartData chartData = new ChartData();
        List<Integer> labels = new LinkedList<>();
        Map<String, DataSet> playerDataMap = new HashMap<>(); //temporary keep y-axis based on different name

        if(playScoreOpt.isPresent()){
            PlayerScore playerScore = playScoreOpt.get();

            for(PlayerWealthAtEachTurn pwaet : playerScore.getPlayerWealthAtEachTurn()){
                labels.add(pwaet.getTurn()); //x-axis

                for(PlayerWealth pw : pwaet.getPlayersWealth()){
                    DataSet dataSet = playerDataMap.getOrDefault(pw.getName(), new DataSet(pw.getName()));
                    dataSet.getMoney().add(pw.getMoney());
                    dataSet.getPropertyValue().add(pw.getPropertyValue());
                    dataSet.getTotal().add(pw.getMoney() + pw.getPropertyValue());

                    playerDataMap.put(pw.getName(), dataSet);
                }
            }
            
            chartData.setLabels(labels);
            chartData.setDatasets(new LinkedList<>(playerDataMap.values()));
    
            return ResponseEntity.status(200).body(chartData);
        }
        return ResponseEntity.status(404).body("Not found");
    }

    public PlayerWealth initialisePlayerWealth(PlayerState playerState){
        return new PlayerWealth(playerState.getName(), playerState.getMoney(), new LinkedList<>(), 0);
    }

    public List<PlayerWealth> updatedPlayerWealth(List<PlayerState> playerStates, List<PropertyState> propertyStates){

        List<Property> propertyInfos = propertyRepo.getPropertyBasic("Default");
        List<PlayerWealth> playerWealths = new LinkedList<>(); 

        for(PlayerState playerState : playerStates){
            List<Integer> properties = playerState.getProperties();
            int propertyValue = 0;
    
            if(!properties.isEmpty()){
                for(int i = 0; i < properties.size(); i++){
                    propertyValue += propertyInfos.get(properties.get(i)).getCost();
                    propertyValue += propertyStates.get(properties.get(i)).getStar() * propertyInfos.get(properties.get(i)).getBuild();
                }
            }
    
            PlayerWealth playerWealth = new PlayerWealth(playerState.getName(), playerState.getMoney(), properties, propertyValue);
            playerWealths.add(playerWealth);
        }
        
        return playerWealths;
    }
}
