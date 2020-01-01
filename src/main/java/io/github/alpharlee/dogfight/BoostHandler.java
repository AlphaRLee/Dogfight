package io.github.alpharlee.dogfight;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class BoostHandler {
    //TODO: Temp function, remove when done
    public ArrayList<Player> forcedGliders;
    private ArrayList<Player> fallingForcedGliders;
    public static final short BROKEN_ELYTRA_DURABILITY = 431;

    //Constructor
    public BoostHandler() {
        forcedGliders = new ArrayList<Player>();
        fallingForcedGliders = new ArrayList<Player>();
    }

    public void checkBoost(Player player) {
        if (player == null) {
            return;
        }

        //Accelerate player
        if (player.isSprinting()) {
            //TODO: Make these values configurable
            double magnitude = 0.05;
            double maxMagnitude = 2.5;

            useBoost(player, magnitude, maxMagnitude);
        }
        //Decelerate player
        else if (player.isSneaking()) {
            //TODO: Make these values configurable
            double magnitude = 0.05;
            double minMagnitude = 0.3;

            slow(player, magnitude, minMagnitude);
        }
    }

    /**
     * Force a player to glide if they have landed and are assigned to forcefully glide
     *
     * @param player
     * @author R Lee
     */
    public void checkForceGlide(Player player) {
        if (player == null) {
            return;
        }

        //Do not force flying (not gliding) users to force glide
        if (forcedGliders.contains(player) && !player.isGliding()) {
            //Test for players who recently landed
            if (player.isOnGround()) {
                //Check player actually has elytra and elytra is not broken
                if (player.getInventory().getChestplate().getType() == Material.ELYTRA
                        && player.getInventory().getChestplate().getDurability() < BROKEN_ELYTRA_DURABILITY) {
                    //Gently push player off ground and mark them to force them to fly again
                    player.setVelocity(player.getVelocity().clone().add(new Vector(0, 0.5, 0)));

                    if (!fallingForcedGliders.contains(player)) {
                        fallingForcedGliders.add(player);
                    }
                }
            }

            //Toggle gliding on for players who are descending (while not flying in creative)
            if (fallingForcedGliders.contains(player) && player.getVelocity().getY() < 0 && !player.isFlying()) {
                player.setGliding(true);
                fallingForcedGliders.remove(player);
            }
        }
    }

    /**
     * Set force glide for a player
     *
     * @param player
     * @author R Lee
     */
    public void setForcedGlide(Player player, boolean forcedGlide) {
        if (player == null) {
            return;
        }

        if (forcedGlide) {
            if (!forcedGliders.contains(player)) {
                forcedGliders.add(player);
                player.sendMessage(ChatColor.AQUA + "You're now " + ChatColor.LIGHT_PURPLE + "forced" + ChatColor.AQUA + " to keep gliding");
            } else {
                player.sendMessage(ChatColor.RED + "You're already forced to keep gliding!");
            }
        } else {
            if (forcedGliders.contains(player)) {
                forcedGliders.remove(player);
                player.sendMessage(ChatColor.AQUA + "You can now walk on the ground again");
            } else {
                player.sendMessage(ChatColor.RED + "Lucky you, you're already able to touch down on terra firma (you already aren't force gliding)");
            }
        }
    }

    /**
     * Apply boost to player with a specific magnitude, up to a maximum magnitude
     * Boost is applied in the direction player is facing and is added to current velocity
     *
     * @param player       Player to boost
     * @param magnitude    Strength of boost. Value of 1 is approximately equivalent to velocity of a elytra glide at 35 degree tilt below XZ plane
     * @param maxMagnitude Maximum magnitude for velocity player can achieve using boost
     */
    public void useBoost(Player player, double magnitude, double maxMagnitude) {
        if (player == null) {
            return;
        }

        //If player is gliding
        if (player.isGliding()) {
            Vector playerVelocity = player.getVelocity();
            Vector boostVelocity = player.getEyeLocation().getDirection().multiply(magnitude);

            boostEntity(player, playerVelocity, boostVelocity, maxMagnitude);

            //Summon particles at player's location
            //TODO: Customize/show/hide effect here
            /*
             * Particle effect parameters:
             * 1st: Particle effect
             * 2nd: Location
             * 3rd: Count
             * 4th-6th: X, Y, Z offsets
             * 7th: Extra (usually speed)
             */
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 1, 0, 0, 0, 0);
        }
    }

    /**
     * Set an entity's velocity to the initVelocity + addedVelocity and cap it off so that the total speed is equal to or lessere than the maxMagnitude
     *
     * @param entity        Entity to set velocity of
     * @param initVelocity  Initial velocity of entity
     * @param addedVelocity Added velocity to entity
     * @param maxMagnitude  Maximum speed to set velocity at
     * @author R Lee
     */
    public static void boostEntity(Entity entity, Vector initVelocity, Vector addedVelocity, double maxMagnitude) {
        //Add the boost velocity to the player velocity
        Vector finalVelocity = initVelocity.clone().add(addedVelocity);
        Double lengthSquared = finalVelocity.lengthSquared();

        //Set the magnitude of the velocity to be equal to or lesser than the max magnitude
        if (lengthSquared > maxMagnitude * maxMagnitude) {
            //Set to max magnitude (Convert to unit vector then multiply to max magnitude)
            finalVelocity.multiply(maxMagnitude / Math.sqrt(lengthSquared));
        }

        //Fireballs do not seem to cooperate well with setVelocity()
        if (entity instanceof Fireball) {
            ((Fireball) entity).setDirection(finalVelocity);
        }

        entity.setVelocity(finalVelocity);
    }

    public void slow(Player player, double magnitude, double minMagnitude) {
        if (player == null) {
            return;
        }

        if (player.isGliding()) {
            Vector playerVelocity = player.getVelocity();
            Vector slowDownVelocity = player.getVelocity().normalize().multiply(magnitude);

            //Do nothing if minimum slow down magnitude is already greater than player's current velocity
            if (playerVelocity.lengthSquared() < minMagnitude * minMagnitude) {
                return;
            }

            //Add the boost velocity to the player velocity
            Vector finalVelocity = playerVelocity.clone().subtract(slowDownVelocity);
            Double lengthSquared = finalVelocity.lengthSquared();

            //Set the magnitude of the velocity to be equal to or lesser than the max magnitude
            if (lengthSquared < minMagnitude * minMagnitude) {
                //Set to max magnitude (Convert to unit vector then multiply to max magnitude)
                finalVelocity.multiply(minMagnitude / Math.sqrt(lengthSquared));
            }

            player.setVelocity(finalVelocity);
        }
    }
}
