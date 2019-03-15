package com.mcabla.microbit.game.scripts.Room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Casper Haems on 25/02/2019.
 * Copyright (c) 2019 Casper Haems. All rights reserved.
 */

@Entity
public class GameModel {
    @PrimaryKey
    public int id;
    private String name;
    private String filename;
    private String description;
    private String icon;
    private String color;
    private int maxSpelers;


    public GameModel(Integer id, String name, String filename, String description, String icon, String color, Integer maxSpelers) {
        this.id = ((id == null) ? 0 : id);
        this.name = ((name == null) ? "-" : name);
        this.filename = ((filename == null) ? "-" : filename);
        this.description = ((description == null) ? "-" : description);
        this.icon = ((icon == null) ? "-" : icon);
        this.color = ((color == null) ? "-" : color);
        this.maxSpelers = ((maxSpelers == null) ? 0 : maxSpelers);
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public int getMaxSpelers() {
        return maxSpelers;
    }
}