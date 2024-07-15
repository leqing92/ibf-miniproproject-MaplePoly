package project.monopoly.game.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import project.monopoly.game.models.Property;
import project.monopoly.game.models.PropertyInMongo;

@Repository
public class PropertyRepository {

    @Autowired
    private MongoTemplate mongoTemp;

    public List<Property> getPropertyBasic(String name){        

        Query query = new Query(Criteria.where("name").is(name));
        PropertyInMongo property = mongoTemp.findOne(query, PropertyInMongo.class, "properties");
        // System.out.println("\n\nproperty" + property.getProperty().toString());
        return property.getProperty();
    }

    public Property getPropertyBasicById(int id){
        Query query = new Query(Criteria.where("name").is("Default"));
        PropertyInMongo properties = mongoTemp.findOne(query, PropertyInMongo.class, "properties");
        
        return properties.getProperty().get(id);
    }
}