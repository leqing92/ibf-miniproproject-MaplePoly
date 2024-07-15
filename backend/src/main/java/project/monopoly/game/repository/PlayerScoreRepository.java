package project.monopoly.game.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import project.monopoly.game.models.PlayerScore;

@Repository
public class PlayerScoreRepository {
    @Autowired
    private MongoTemplate mongoTemp;

    private final String COLLECTION_PLAYERSCORE= "playerscores";

    public void savePlayerScore(PlayerScore playerScore){
        mongoTemp.save(playerScore, COLLECTION_PLAYERSCORE);
    }

    public Optional<PlayerScore> getPlayerScoreByGid(String gid){
        Query query = new Query(Criteria.where("gid").is(gid));
        query.fields().exclude("turnStates");
        
        return Optional.ofNullable(mongoTemp.findOne(query,PlayerScore.class, COLLECTION_PLAYERSCORE));
    }
}
