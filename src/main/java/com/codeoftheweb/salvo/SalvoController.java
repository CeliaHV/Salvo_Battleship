/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeoftheweb.salvo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.validator.internal.util.logging.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SalvoController {
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    
    @Autowired
    private SalvoRepository salvoRepository;
    
    @Autowired
    private ScoreRepository scoreRepository;
    
    @Autowired
    private ShipRepository shipRepository;
    
    
    @RequestMapping("/games")
        public Map<String, Object> getGameIdDate (Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            dto.put("player", "guest");
        } else {
        dto.put("player", playerRepository.findByUserName(authentication.getName()).playerDto());
        }
        dto.put("games", gameRepository.findAll().stream().map(game -> game.gameDTO()).collect(Collectors.toList()));    
        return dto;
    }
        
    @RequestMapping("/game_view/{gamePlayerId}")
        public Map<String, Object> getGameIdView (@PathVariable Long gamePlayerId) {
            Optional<GamePlayer> gamePlayerOptional = gamePlayerRepository.findById(gamePlayerId);
            if(gamePlayerOptional.isPresent()){
                GamePlayer gamePlayer = gamePlayerOptional.get();
                return gameViewDTO(gamePlayer);
            }
            Map<String, Object> map = new LinkedHashMap<>();
             map.put("Error", "Unknown ID");
            return map;
        }
        
    @RequestMapping(path = "/players", method = RequestMethod.POST)
        public ResponseEntity<Object> register(
          @RequestParam String userName, @RequestParam String password) {

        if (userName.isEmpty() || password.isEmpty()) {
          return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(userName) !=  null) {
          return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
        
    @PostMapping("/game/{gameId}/players")
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("Error","You need to login"), HttpStatus.FORBIDDEN);
        }
        Game game = gameRepository.findById(gameId).orElse(null);
        if(game == null){
            return new ResponseEntity<>(makeMap("Error","Game does not exist"), HttpStatus.FORBIDDEN);
        }
        if(game.getGamePlayers().size() > 1){
            return new ResponseEntity<>(makeMap("Error","Game Full"), HttpStatus.FORBIDDEN);
        }

        Player player = playerRepository.findByUserName(authentication.getName());
        GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(game, player, LocalDateTime.now()));

        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    };
    
    @PostMapping("/games")
	public ResponseEntity<Map<String, Object>> createGames (Authentication authentication) {
		if (isGuest(authentication)) {
			return new ResponseEntity<>(makeMap("Error", "Not authorized"), HttpStatus.FORBIDDEN);
		}
		Player player = playerRepository.findByUserName(authentication.getName());
		Game newGame = gameRepository.save(new Game(LocalDateTime.now()));
		GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(newGame, player, LocalDateTime.now()));

		return  new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
	}
    
    @PostMapping("games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> addShips(@PathVariable Long gamePlayerId, Authentication authentication, @RequestBody Set<Ship> SetShips){
        if(isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("Error","Forbidden"), HttpStatus.FORBIDDEN);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if(!gamePlayer.isPresent()){
            return new ResponseEntity<>(makeMap("Error","Game does not exist"), HttpStatus.BAD_REQUEST);
        }
        if(!gamePlayer.get().getPlayer().getUserName().equals(authentication.getName())){
            return new ResponseEntity<>(makeMap("Error","Forbidden"), HttpStatus.FORBIDDEN);
        }
        Optional<GamePlayer> opponentGamePlayer = gamePlayer.get().getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gamePlayerId).findFirst();
        if(!opponentGamePlayer.isPresent()){
            return new ResponseEntity<>(makeMap("Error","Forbidden"), HttpStatus.FORBIDDEN);
        }
        if(gamePlayer.get().getShips().size() > 0 || SetShips.size() != 5){
            return new ResponseEntity<>(makeMap("Error","Error placed ship"), HttpStatus.FORBIDDEN);
        }
        
        SetShips.forEach(ship -> gamePlayer.get().addShips(ship));
        
        gamePlayerRepository.save(gamePlayer.get());
        return  new ResponseEntity<>(makeMap("Created","Created"), HttpStatus.CREATED);
    }
    
    @PostMapping("/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object> > addSalvoes(Authentication authentication, @PathVariable long gamePlayerId, @RequestBody List<String>shots) {
    ResponseEntity<Map<String, Object>> response;
    Player player = playerRepository.findByUserName(authentication.getName());
    GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
    if (isGuest(authentication)) {
        response = new ResponseEntity<>(makeMap("error", "not logged in"), HttpStatus.UNAUTHORIZED);
    } else if (gamePlayer == null) {
        response = new ResponseEntity<>(makeMap("error", "that game does not exist"), HttpStatus.NOT_FOUND);
    } else if (gamePlayer.getPlayer().getId() != player.getId()) {
        response = new ResponseEntity<>(makeMap("error", "that player is not participating in that game"), HttpStatus.NOT_FOUND);
    } else if (shots.size() > 5) {
        response = new ResponseEntity<>(makeMap("error", "you're firing more salvoes than you should"), HttpStatus.FORBIDDEN);
    } else {
        
        int turn = gamePlayer.getSalvos().size() + 1;
        Salvo newSalvo = new Salvo(turn, shots);
        gamePlayer.addSalvo(newSalvo);
        gamePlayerRepository.save(gamePlayer);
        response = new ResponseEntity<>(makeMap("success", "the salvo have been placed"), HttpStatus.CREATED);
    }
   return response;
}
            
    public Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created",gamePlayer.getGame().getDate());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gp -> gp.gamePlayerDTO()));
        dto.put("ships", gamePlayer.getShips().stream().map(Ship::shipDto).collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().flatMap(gameplayer -> gameplayer.getSalvos().stream().map(Salvo::salvosDto)).collect(Collectors.toList()));
        return dto;
        }    
    
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    
    private Map<String, Object> makeMap(String key, Object value) {
      Map<String, Object> map = new HashMap<>();
      map.put(key, value);
          return map;
      }
}
