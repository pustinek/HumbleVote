package com.pustinek.humblevote.utils;

import javafx.scene.paint.Material;

import java.util.List;

public class PreItemStack {
    private String name;
    private List<String> lore;
    private Material material;

    public PreItemStack(String name, List<String> lore, Material material) {
        this.name = name;
        this.lore = lore;
        this.material = material;
    }




    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public Material getMaterial() {
        return material;
    }
}
