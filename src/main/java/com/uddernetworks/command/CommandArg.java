package com.uddernetworks.command;

import org.bukkit.Material;
import org.bukkit.Sound;

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

}
