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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class GamePlayer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private LocalDateTime joinDate;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;
    
    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private
    Set<Ship> ships = new HashSet<>();
    
    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private
    Set<Salvo> salvos = new HashSet<>();
    
    public GamePlayer () {}
    
    public GamePlayer(LocalDateTime now) {
        this.joinDate = now;
    }
    
    public GamePlayer(Game game, Player player,LocalDateTime date) {
        this.game = game;
        this.player = player;
        this.joinDate = date;
        this.addSalvos(salvos);
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void addShips(Ship ship) {
        ship.setGamePlayer(this);
        ships.add(ship);
    }
    
    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void addSalvo(Salvo salvo) {
        salvo.setGamePlayer(this);
        salvos.add(salvo);
    }
    
    public void addSalvos(Set<Salvo> salvos){
        salvos.stream().forEach(salvo -> {
            salvo.setGamePlayer(this);
            this.salvos.add(salvo);
        });
    }
   
    public LocalDateTime getJoinDate() {
        return joinDate;
    }
    
    public void setJoinDate(LocalDateTime date) {
        this.joinDate = date;
    }
    
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public Score getScore(){
        return this.getPlayer().getGameScore(this.getGame());
    }
    
    public Map<String, Object> gamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.id);
        dto.put("player", this.player.playerDto());
        if(this.getPlayer().getGameScore(this.getGame()) != null)
           dto.put("score", this.getScore().getScore());
        else
           dto.put("score", this.getScore());
        return dto;
        
        }

}

