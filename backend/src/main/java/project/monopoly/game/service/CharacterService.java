package project.monopoly.game.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.monopoly.game.models.character.Character;
import project.monopoly.game.models.character.CharacterSummary;
import project.monopoly.game.repository.CharacterRepository;

@Service
public class CharacterService {
    
    @Autowired
    CharacterRepository repo;

    public List<Character> getAllCharacters(){
        return repo.getAllCharacters();
    }

    public List<CharacterSummary> getAllCharacterSummary(){  
        return repo.getAllCharacterSummary();
    }

    public Character getCharacterByName(String name){
        return repo.getCharacterByName(name);
    }
}
