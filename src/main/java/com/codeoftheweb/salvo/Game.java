/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeoftheweb.salvo;

/**
 *
 * @author Usuario
 */
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime date;
    
    
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;
    
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private
    Set<Score> scores = new HashSet<>();
    
    public Game () {}
    
    public Game(LocalDateTime now) {
        this.date = now;
    }
    
    public Map<String, Object> gameDTO() {
            Map<String, Object> dto = new LinkedHashMap<String, Object>();
            dto.put("id", this.getId());
            dto.put("created", this.getDate());
            dto.put("gamePlayers", this.getGamePlayers().stream().map(gamePlayer -> gamePlayer.gamePlayerDTO()).collect(Collectors.toList()));
            return dto;
    }
    
      public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public long getId(){
        return this.id;
    }
    
    public void setId(Long id){
        this.id = id;
    }
    
    @JsonIgnore
    public Set<GamePlayer> getGamePlayers(){
        return this.gamePlayers;
    }
    
    public void setGamePlayers(Set<GamePlayer> gamePlayers) { 
        this.gamePlayers = gamePlayers; 
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    public void addScore (Score score) {
        score.setGame(this);
        scores.add(score);
    }
}

