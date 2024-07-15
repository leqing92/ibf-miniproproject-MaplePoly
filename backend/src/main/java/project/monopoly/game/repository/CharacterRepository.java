package project.monopoly.game.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import project.monopoly.game.models.character.Character;
import project.monopoly.game.models.character.CharacterSummary;

@Repository
public class CharacterRepository {
    @Autowired
	MongoTemplate mongoTemp;

    public List<Character> getAllCharacters(){
        Query query = new Query();

        return mongoTemp.find(query, Character.class);
    }

    public Character getCharacterByName(String name){
        Query query = new Query(Criteria.where("name").is(name));

        return mongoTemp.findOne(query, Character.class);
    }

    public List<CharacterSummary> getAllCharacterSummary(){
        Query query = new Query();
        
        return mongoTemp.find(query, CharacterSummary.class);
    }
}
