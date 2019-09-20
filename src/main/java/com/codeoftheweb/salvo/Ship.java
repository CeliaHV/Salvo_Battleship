/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeoftheweb.salvo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author Usuario
 */
@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private ShipType shipType;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;
    
    @ElementCollection
    @Column(name="cellGrid")
    private List<String> cellGrid = new ArrayList<>();
    
    public Ship() { }
    
    public Ship (ShipType shipType,List<String> cellGrid){
        this.shipType = shipType;
        this.cellGrid = cellGrid;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public ShipType getShipType() {
        return shipType;
    }

    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }
    
    public List<String> getCellGrid() {
        return cellGrid;
    }

    public void setCellGrid(List<String> cellGrid) {
        this.cellGrid = cellGrid;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
    
    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
    
    public Map<String, Object> shipDto() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", this.getShipType());
        dto.put("locations", this.getCellGrid());
        return dto;
    }
}
