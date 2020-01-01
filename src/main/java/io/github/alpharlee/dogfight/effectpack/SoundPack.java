package io.github.alpharlee.dogfight.effectpack;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundPack {
    private Location location;
    private Sound sound;
    private SoundCategory soundCategory;
    private float volume;
    private float pitch;

    /**
     * Create a sound pack where the location is set to null (plays at player's location),
     * the sound category is {@link SoundCategory#MASTER},
     * the volume is 1,
     * and the pitch is 1
     *
     * @param sound
     * @param pitch
     */
    public SoundPack(Sound sound) {
        this(sound, 1);
    }

    /**
     * Create a sound pack where the location is set to null (plays at player's location),
     * the sound category is {@link SoundCategory#MASTER},
     * and the volume is 1
     *
     * @param sound
     * @param pitch
     */
    public SoundPack(Sound sound, float pitch) {
        this(sound, SoundCategory.MASTER, pitch);
    }

    /**
     * Create a sound pack where the location is set to null (plays at player's location), and the sound category is {@link SoundCategory#MASTER}
     *
     * @param sound
     * @param volume
     * @param pitch
     */
    public SoundPack(Sound sound, float volume, float pitch) {
        this(null, sound, SoundCategory.MASTER, volume, pitch);
    }

    /**
     * Create a sound pack where the location is set to null (plays at player's location), and the volume is 1
     *
     * @param sound
     * @param soundCategory
     * @param pitch
     */
    public SoundPack(Sound sound, SoundCategory soundCategory, float pitch) {
        this(null, sound, soundCategory, 1, pitch);
    }

    /**
     * Create a sound pack where the location is set to null (plays at player's location)
     *
     * @param sound
     * @param soundCategory
     * @param volume
     * @param pitch
     */
    public SoundPack(Sound sound, SoundCategory soundCategory, float volume, float pitch) {
        this(null, sound, soundCategory, volume, pitch);
    }

    public SoundPack(Location location, Sound sound, SoundCategory soundCategory, float volume, float pitch) {
        setLocation(location);
        setSound(sound);
        setSoundCategory(soundCategory);
        setVolume(volume);
        setPitch(pitch);
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the sound
     */
    public Sound getSound() {
        return sound;
    }

    /**
     * @param sound the sound to set
     */
    public void setSound(Sound sound) {
        this.sound = sound;
    }

    /**
     * @return the soundCategory
     */
    public SoundCategory getSoundCategory() {
        return soundCategory;
    }

    /**
     * @param soundCategory the soundCategory to set
     */
    public void setSoundCategory(SoundCategory soundCategory) {
        this.soundCategory = soundCategory;
    }

    /**
     * @return the volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * @param pitch the pitch to set
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Play the sound to the player.
     * If {@link SoundPack#getLocation()} returns null, then play the sound at the player's location
     *
     * @param player
     * @author R Lee
     */
    public void play(Player player) {
        player.playSound((getLocation() != null ? getLocation() : player.getLocation()), getSound(), getSoundCategory(), getVolume(), getPitch());
    }
}
