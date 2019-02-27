package com.uddernetworks.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CommandArg {

    private String input;

    public CommandArg(String input) {
        this.input = input;
    }

    public String getString() {
        return input;
    }

    public int getInt() {
        return Integer.valueOf(input);
    }

    public boolean getBoolean() {
        return Boolean.valueOf(input);
    }

    public long getLong() {
        return Long.valueOf(input);
    }

    public short getShort() {
        return Short.valueOf(input);
    }

    public byte getByte() {
        return Byte.valueOf(input);
    }

    public Material getMaterial() {
        return Material.valueOf(input);
    }

    public Sound getSound() {
        return Sound.valueOf(input);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(input);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(input);
    }

}
