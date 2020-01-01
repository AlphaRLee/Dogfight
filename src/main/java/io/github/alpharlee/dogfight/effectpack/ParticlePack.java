package io.github.alpharlee.dogfight.effectpack;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;


public class ParticlePack {

    private Particle particle;
    private Location location;
    private int timesPlayed;

    public ParticlePack(Particle particle) {
        this.particle = particle;
    }

    public ParticlePack(Particle particle, Location location) {
        this.particle = particle;
        this.location = location;
    }

    public ParticlePack(Particle particle, int timesPlayed) {
        this.particle = particle;
        this.timesPlayed = timesPlayed;
    }

    public ParticlePack(Particle particle, Location location, int timesPlayed) {
        this.particle = particle;
        this.location = location;
        this.timesPlayed = timesPlayed;
    }

    public Location getLocation() {
        return location;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    /**
     * Plays Particle at location (gotten from this#getLocation)
     */
    public void play() {
        location.getWorld().spawnParticle(getParticle(), getLocation(), getTimesPlayed());
    }

    /**
     * Plays Particle at location (gotten from this#getLocation) so p can see it
     *
     * @param p player who can see the Particle
     */
    public void playForPlayer(Player p) {
        p.spawnParticle(getParticle(), getLocation(), getTimesPlayed());
    }

    /**
     * Plays Particle at location of p
     *
     * @param p player to spawn Particle at
     */
    public void playAtPlayer(Player p) {
        location.getWorld().spawnParticle(getParticle(), getLocation(), getTimesPlayed());
    }

    /**
     * Plays Perticle at location of playedAt so playedFor can see it
     *
     * @param playedFor player who can see the Particle
     * @param playedAt  player to spawn Particle at
     */
    public void playForPlayerAtPlayer(Player playedFor, Player playedAt) {
        playedFor.spawnParticle(getParticle(), playedAt.getLocation(), getTimesPlayed());
    }
}
