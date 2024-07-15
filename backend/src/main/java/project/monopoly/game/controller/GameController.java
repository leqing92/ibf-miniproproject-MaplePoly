package project.monopoly.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project.monopoly.game.service.GameStateService;
import project.monopoly.game.service.PlayerScoreService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//  for game 
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    GameStateService gameStateSvc;

    @Autowired
    PlayerScoreService playerScoreSvc;
    
    @GetMapping("/{id}/data")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> getGameBasicInfo(@PathVariable String id) {
        
        return gameStateSvc.getGameBasicInfoById(id);
    }

    @GetMapping("/{id}/chart")
    public ResponseEntity<Object> getMethodName(@PathVariable String id) {        
        
        return playerScoreSvc.getChartData(id);
    }
    
    
}
