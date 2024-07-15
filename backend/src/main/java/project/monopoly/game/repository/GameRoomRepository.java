package project.monopoly.game.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.monopoly.game.models.GameRoom;

import com.fasterxml.jackson.core.type.TypeReference;

@Repository
public class GameRoomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper; 
    
    private final RowMapper<GameRoom> gameRowMapper = (rs, rowNum) -> {
        GameRoom game = new GameRoom();
        game.setGid(rs.getString("gid"));
        game.setStart(rs.getBoolean("is_start"));
        game.setOwner(rs.getString("owner"));
        try {
            String playersJson = rs.getString("players");
            List<String> players = objectMapper.readValue(playersJson, new TypeReference<List<String>>() {});
            game.setPlayers(players);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse players JSON", e);
        }

        return game;        
    };

    public GameRoom createGameRoom(GameRoom game){
        String INSERT_GAME = "INSERT INTO gamerooms (gid, is_start, owner, players) VALUES (?, ?, ?, ?)";
        try {
            String playersJson = objectMapper.writeValueAsString(game.getPlayers());
            jdbcTemplate.update(INSERT_GAME, game.getGid(), game.isStart(), game.getOwner(), playersJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize players to JSON", e);
        }
        return game;
    }

    public Optional<GameRoom> findGameRoomById(String gameId){
        String FIND_GAMEROOM_BY_ID = "SELECT * FROM gamerooms WHERE gid = ?";
        try {
            GameRoom game = jdbcTemplate.queryForObject(FIND_GAMEROOM_BY_ID, gameRowMapper, gameId);
            return Optional.of(game);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<GameRoom> findAllGameRooms(){
        String GET_GAMEROOM_IS_NOT_START = "SELECT * FROM gamerooms WHERE is_start = false";
        return jdbcTemplate.query(GET_GAMEROOM_IS_NOT_START, gameRowMapper);
    }

    public GameRoom updateGameRoom(GameRoom game){
        String UPDATE_GAMEROOM_DETAIL = "UPDATE gamerooms SET is_start = ?, owner = ?, players = ? WHERE gid = ?";
        try {
            String playersJson = objectMapper.writeValueAsString(game.getPlayers());
            jdbcTemplate.update(UPDATE_GAMEROOM_DETAIL, game.isStart(), game.getOwner(), playersJson, game.getGid());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize players to JSON", e);
        }
        return game;
    }

    public void updateGameRoomToStarted(String gid){
        String UPDATE_GAMEROOM_TO_STARTED = "UPDATE gamerooms SET is_start = ? WHERE gid = ?";
        jdbcTemplate.update(UPDATE_GAMEROOM_TO_STARTED, true, gid);
    }

    public void deleteGameRoomById(String gameId){
        String DELETE_GAMEROOM_BY_ID = "DELETE FROM gamerooms WHERE gid = ?";
        jdbcTemplate.update(DELETE_GAMEROOM_BY_ID, gameId);
    }
}
