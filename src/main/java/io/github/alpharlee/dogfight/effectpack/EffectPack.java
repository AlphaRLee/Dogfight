package io.github.alpharlee.dogfight.effectpack;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EffectPack {

    private Effect effect;
    private Location location;
    private int data = 0;
    private int radius = 1;

    public EffectPack(Effect effect) {
        this.effect = effect;
    }

    public EffectPack(Effect effect, Location location) {
        this.effect = effect;
        this.location = location;
    }

    public EffectPack(Effect effect, int data) {
        this.effect = effect;
        this.data = data;
    }

    public EffectPack(Effect effect, Location location, int data) {
        this.effect = effect;
        this.location = location;
        this.data = data;
    }

    public EffectPack(Effect effect, int radius, int data) {
        this.effect = effect;
        this.radius = radius;
        this.data = data;
    }

    public EffectPack(Effect effect, int radius, Location location) {
        this.effect = effect;
        this.radius = radius;
        this.location = location;
    }

    public EffectPack(Effect effect, Location location, int data, int radius) {
        this.effect = effect;
        this.location = location;
        this.data = data;
        this.radius = radius;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Effect getEffect() {
        return effect;
    }

    public int getData() {
        return data;
    }

    public int getRadius() {
        return radius;
    }

    public void setData(int data) {
        this.data = data;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Plays Effect at Location with data and radius (if supplied)
     */
    public void play() {
        if (getLocation() != null) {
            if (getRadius() != 0) {
                getLocation().getWorld().playEffect(getLocation(), getEffect(), getData(), getRadius());
                return;
            }
            getLocation().getWorld().playEffect(getLocation(), getEffect(), getData());
        }
    }

    /**
     * Plays Effect at Location so p can see it
     *
     * @param p player who can see the effect
     */
    public void playForPlayer(Player p) {
        p.playEffect(getLocation(), getEffect(), getData());
    }

    /**
     * Plays Effect at Location of p
     *
     * @param p player to play the Effect at
     */
    public void playAtPlayer(Player p) {
        if (getRadius() != 0) {
            getLocation().getWorld().playEffect(p.getLocation(), getEffect(), getData(), getRadius());
            return;
        }
        getLocation().getWorld().playEffect(p.getLocation(), getEffect(), getData());
    }

    /**
     * Plays Effect at Location of playedAt so playedFor can see it
     *
     * @param playedFor player who can see the effect
     * @param playedAt  player to play the effect at
     */
    public void playAtPlayerForPlayer(Player playedFor, Player playedAt) {
        playedFor.playEffect(playedAt.getLocation(), getEffect(), getData());
    }
}
