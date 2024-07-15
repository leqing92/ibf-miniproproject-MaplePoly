package project.monopoly.game.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.monopoly.game.models.Property;
import project.monopoly.game.repository.PropertyRepository;

@Service
public class PropertyService {
    
    @Autowired
    PropertyRepository propertyRepo;

    public List<Property> getPropertyBasic(String name){
        return propertyRepo.getPropertyBasic(name);
    }

    public Property getPropertyBasicById(int id){
        return propertyRepo.getPropertyBasicById(id);
    }
}
