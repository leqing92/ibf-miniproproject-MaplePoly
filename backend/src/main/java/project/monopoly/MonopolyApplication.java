package project.monopoly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import project.monopoly.game.models.character.Character;
import project.monopoly.game.models.character.CharacterSummary;
import project.monopoly.game.repository.CharacterRepository;
import project.monopoly.game.repository.GameStateRepository;
import project.monopoly.game.repository.PropertyRepository;
import project.monopoly.game.service.PlayerScoreService;
import project.monopoly.security.repository.UserInfoRepository;

@SpringBootApplication
public class MonopolyApplication implements CommandLineRunner {

	// @Autowired
	// GameStateRepository playerRepo;
	public static void main(String[] args) {
		SpringApplication.run(MonopolyApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		// System.out.println(playerRepo.getGameStateBasicInfoByGid("a3dd2be3").toString());
	}

	

}
