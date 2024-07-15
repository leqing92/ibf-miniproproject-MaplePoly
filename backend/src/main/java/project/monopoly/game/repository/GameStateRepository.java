package project.monopoly.game.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import project.monopoly.game.models.GameState;

@Repository
public class GameStateRepository {

    @Autowired
    private MongoTemplate mongoTemp;

    private final String COLLECTION_GAMESTATE = "gamestates";

    public void saveGameState(GameState gameState){
        mongoTemp.save(gameState, COLLECTION_GAMESTATE);
    }

    public Optional<GameState> getGameStateBygid(String gid){
        Query query = new Query(Criteria.where("gid").is(gid));
        
        return Optional.ofNullable(mongoTemp.findOne(query, GameState.class, COLLECTION_GAMESTATE));
    }

    /*
    db.getCollection('gamestates').find(
        { "_id": "your-gid-value" },
        { "turnStates": 0 }
    )
    */
    public Optional<GameState> getGameStateBasicInfoByGid(String gid){
        Query query = new Query(Criteria.where("_id").is(gid));
        query.fields().exclude("turnStates");

        return Optional.ofNullable(mongoTemp.findOne(query, GameState.class, COLLECTION_GAMESTATE));
    }

    public void deleteGameState(String gid){
        Query query = new Query(Criteria.where("_id").is(gid));
        mongoTemp.remove(query, COLLECTION_GAMESTATE);
    }
    
}
