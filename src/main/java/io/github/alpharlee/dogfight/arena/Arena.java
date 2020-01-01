package io.github.alpharlee.dogfight.arena;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Random;


public class Arena {

    private World arena;
    private String name;
    /**
     * Location where everyone is teleported at the beginning of a match
     */
    private Location spawnInPoint;
    /**
     * These are spawnpoints that players will be teleported to on death (if they have multiple lives)
     */
    private ArrayList<Location> randomSpawnPoints = new ArrayList<>();

    public Arena(World arena) {
        this.arena = arena;
        this.name = arena.getName();
        this.spawnInPoint = arena.getSpawnLocation();
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return arena;
    }

    public void setSpawnInPoint(Location spawnInPoint) {
        this.spawnInPoint = spawnInPoint;
    }

    public Location getSpawnInPoint() {
        return this.spawnInPoint;
    }

    public void addRandomSpawnpoint(Location spawnPoint) {
        this.randomSpawnPoints.add(spawnPoint);
    }

    public ArrayList<Location> getRandomSpawnpoints() {
        return this.randomSpawnPoints;
    }

    public Location getRandomSpawnpoint() {
        return randomSpawnPoints.get(new Random().nextInt(randomSpawnPoints.size()));
    }
}
