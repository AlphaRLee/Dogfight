package io.github.alpharlee.dogfight.game;

import io.github.alpharlee.dogfight.commandhandler.CommandHandler;
import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.arena.Arena;
import io.github.alpharlee.dogfight.arena.ArenaInformation;
import io.github.alpharlee.dogfight.exceptions.ArenaException;
import io.github.alpharlee.dogfight.projectile.Missile;
import io.github.alpharlee.dogfight.registry.*;
import io.github.alpharlee.dogfight.runnable.IncomingMissileWarning;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;

// FIXME Add this dependency
//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.events.PacketAdapter;
//import com.comphenix.protocol.events.PacketEvent;

/**
 * Stores information relevant to the minigame
 *
 * @author R Lee
 */
public class Game {
    private PlayerRegistry playerRegistry;
    private ProjectileRegistry projectileRegistry;
    private FloatingEntityRegistry floatingEntityRegistry;
    private DamageRegistry damageRegistry;
    private ScoreRegistry scoreRegistry;

    public HashMap<Player, IncomingMissileWarning> incomingWarnings;

    private Arena arena = null;

    public Game() {
        setup();
    }

    private void setup() {
        //Establish new registries
        setPlayerRegistry(new PlayerRegistry(this));
        setProjectileRegistry(new ProjectileRegistry(this));
        setFloatingEntityRegistry(new FloatingEntityRegistry(this));
        setDamageRegistry(new DamageRegistry(this));
        setScoreRegistry(new ScoreRegistry(this));
        setIncomingWarnings(new HashMap<Player, IncomingMissileWarning>()); //TODO: Give independent registry for this
    }

    /**
     * Disable the game and all contents within
     *
     * @author R Lee
     */
    public void disable() {
        //Clear all values from each registry
        getProjectileRegistry().clearProjectiles();
        getFloatingEntityRegistry().clearFloatingEntities();
        getDamageRegistry().clearDamageSources();
        getScoreRegistry().clearScores(true); //Clear scores and teams
        getPlayerRegistry().clearPlayers(true); //Clear players and teams
    }

    public PlayerRegistry getPlayerRegistry() {
        return this.playerRegistry;
    }

    public void setPlayerRegistry(PlayerRegistry registry) {
        this.playerRegistry = registry;
    }

    public ProjectileRegistry getProjectileRegistry() {
        return this.projectileRegistry;
    }

    public void setProjectileRegistry(ProjectileRegistry registry) {
        this.projectileRegistry = registry;
    }

    public FloatingEntityRegistry getFloatingEntityRegistry() {
        return this.floatingEntityRegistry;
    }

    private void setFloatingEntityRegistry(FloatingEntityRegistry registry) {
        this.floatingEntityRegistry = registry;
    }

    public DamageRegistry getDamageRegistry() {
        return this.damageRegistry;
    }

    private void setDamageRegistry(DamageRegistry registry) {
        this.damageRegistry = registry;
    }

    public ScoreRegistry getScoreRegistry() {
        return this.scoreRegistry;
    }

    private void setScoreRegistry(ScoreRegistry registry) {
        this.scoreRegistry = registry;
    }

    /**
     * General update on all things being tracked and needs to be updated
     *
     * @author R Lee
     */
    public void update() {
        //Update player inventories
        getPlayerRegistry().updatePlayers();

        getProjectileRegistry().updateProjectiles();
        getProjectileRegistry().removeMarkedProjectiles(); //Remove marked projectiles

        getFloatingEntityRegistry().updateFloatingEntities();
        getFloatingEntityRegistry().removeMarkedFloatingEntities();

        getDamageRegistry().updateDamageSources();
        getDamageRegistry().removeMarkedDamageSources();
    }

    public HashMap<Player, IncomingMissileWarning> getIncomingWarnings() {
        return this.incomingWarnings;
    }

    public void setIncomingWarnings(HashMap<Player, IncomingMissileWarning> incomingWarnings) {
        this.incomingWarnings = incomingWarnings;
    }

    public IncomingMissileWarning getIncomingMissileWarning(Player player) {
        return getIncomingWarnings().get(player);
    }

    public void setIncomingMissileWarning(Player player, IncomingMissileWarning warning) {
        getIncomingWarnings().put(player, warning);
    }

    /**
     * Add a missile to the existing incomingMissileWarning for the player, if it exists.
     * If it does not exist, create a new incomingMissileWarning
     *
     * @param player
     * @param missile
     * @author R Lee
     */
    public void addMissileToWarning(Player player, Missile missile) {
        IncomingMissileWarning warning = getIncomingMissileWarning(player);

        if (warning == null) {
            //Instantiate new warning
            warning = new IncomingMissileWarning(player, this);

            //Create new warning
            setIncomingMissileWarning(player, warning);

            /*
             * Start the timer (via the main plugin, first param is delay in Longs, second is repeat delay in longs)
             * param 1: Main plugin
             * param 2: Delay (in longs) (measured in ticks)
             * param 3: Repeat delay (in longs) (measured in ticks)
             */
            warning.runTaskTimer(Dogfight.instance, 1L, 1L);
        }

        warning.addMissile(missile);
    }

    public Arena getArena() {
        return this.arena;
    }

    public void setArena(Arena arena) throws ArenaException {
        if (this.arena == null) {
            this.arena = arena;
            ArenaInformation.setGameForArena(this, arena);
        } else {
            ArenaInformation.removeGameForArena(this.arena);
            this.arena = null;
            setArena(arena);
        }
    }

    /**
     * End the game
     *
     * @author R Lee
     */
    public void end() {
        getScoreRegistry().resetScores();
        getScoreRegistry().resetScorestreaks();

        //TODO: Remove the following:
        getPlayerRegistry().setTeamGame(false);

        /*
         * TODO: Call disable()
         */
    }

    /**
     * End the game with one team winning
     *
     * @param winningTeam
     * @author R Lee
     */
    public void win(Team winningTeam) {
        CommandHandler.broadcast(this, false,
                ChatColor.YELLOW + "----------------------------------------",
                "",
                ChatColor.YELLOW + "Congratulations, " + winningTeam.getDisplayName() + ChatColor.YELLOW + (getPlayerRegistry().isTeamGame() ? " team" : "") + " has" + ChatColor.GREEN + ChatColor.BOLD + " won" + ChatColor.YELLOW + " the dogfight!",
                "",
                ChatColor.YELLOW + "----------------------------------------");

        end(); //End the game after the victors are announced
    }
}
