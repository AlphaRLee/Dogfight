package io.github.alpharlee.dogfight;

import io.github.alpharlee.dogfight.commandhandler.CommandHandler;
import io.github.alpharlee.dogfight.alert.AlertLevel;
import io.github.alpharlee.dogfight.alert.ShowAlertType;
import io.github.alpharlee.dogfight.eventlistener.ListenerManager;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.runnable.MasterRunnable;
import io.github.alpharlee.dogfight.score.Score;
import io.github.alpharlee.dogfight.score.Scorestreak;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main starting class for Dogfight Plugin
 *
 * @author R Lee
 */
public class Dogfight extends JavaPlugin {
    public static Dogfight instance;

    //public ProtocolManager protocolManager; //TODO: Re-enable once ProtocolLib for Spigot 1.12 is released

    //Create an event listener and instantiate it in the onEnable
    protected ListenerManager listenerManager;
    //Create a command handler and instantiate in the onEnable
    protected CommandHandler commandHandler;
    //Create a general item handler
    protected ItemHandler itemHandler;

    //Master runnable handling all timer tasks
    protected MasterRunnable masterRunnable;

    //Create a boostHandler. Handles everything related to the boosting mechanic
    public BoostHandler boostHandler = new BoostHandler();

    private ArrayList<Game> games;
    private HashMap<Player, Game> gameByPlayer; //Reverse lookup tool: Get the game the player is in

    /**
     * Config file for the plugin
     */
    private YamlConfiguration config;

    /**
     * Saved data for arenas
     * <p>
     * For large servers this will be changed into a database
     */
    private YamlConfiguration arenasData;

    /**
     * Score increment and maximum data for games
     */
    private YamlConfiguration scoreData;

    /**
     * Config file for default scorestreaks
     */
    private YamlConfiguration scorestreakData;

    public AlertLevel messageAlertLevel = AlertLevel.WARNING;
    public ShowAlertType showAlertType = ShowAlertType.GREATER;
    public ArrayList<Player> alertViewers = new ArrayList<Player>(); //TODO: Move to more formal spot

    public static final double NOT_FOUND = -999; //Placeholder for unset numeric variables
    public static final char ALT_COLOR_CHAR = '&';

    //TODO: REMOVE WHEN FINISHED
    public Game testGame;

    @Override
    public void onEnable() {
        setup();
    }

    @Override
    public void onDisable() {
        /*
         * TODO:
         * -Save player profiles
         * -Restore player inventories
         * -Return players to lobbies (not flying)
         */

        //Remove all registered instances
        for (Game game : getGames()) {
            game.disable();
        }
    }

    public ListenerManager getListenerManager() {
        return this.listenerManager;
    }

    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    public ItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public BoostHandler getBoostHandler() {
        return this.boostHandler;
    }

    public ArrayList<Game> getGames() {
        return this.games;
    }

    public YamlConfiguration getArenasData() {
        return this.arenasData;
    }

    public YamlConfiguration getScoreData() {
        return this.scoreData;
    }

    public YamlConfiguration getScorestreakData() {
        return this.scorestreakData;
    }

    /**
     * General setup to keep onEnable() decluttered
     *
     * @author R Lee
     */
    private void setup() {
        instance = this; //Instance reference to the only instance of the main plugin class

        //this.protocolManager = ProtocolLibrary.getProtocolManager(); //TODO: Re-enable once ProtocolLib for Spigot 1.12 is released

        this.listenerManager = new ListenerManager();
        this.commandHandler = new CommandHandler();
        this.itemHandler = new ItemHandler();
        this.boostHandler = new BoostHandler();

        //Initialize the master task timer
        this.masterRunnable = new MasterRunnable();

        this.games = new ArrayList<Game>();
        this.gameByPlayer = new HashMap<Player, Game>();

        //TODO: KLUDGE, DEBUG PURPOSES ONLY
        testGame = new Game();
        this.games.add(testGame);

        //Start the timer (via this plugin, first param is delay in Longs, second is repeat delay in longs)
        //Started after all other aspects instantiated
        this.masterRunnable.runTaskTimer(this, 1L, 1L);

        createConfigs();
        loadDefaultValues(); //Load all default values from config files into their respective classes
    }

    private void createConfigs() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            File config = new File(getDataFolder(), "config.yml");
            if (!config.exists()) {
                getLogger().info("config.yml not found, creating!");
                saveDefaultConfig();
            }
            this.config = YamlConfiguration.loadConfiguration(config);

            File arenasFile = new File(getDataFolder(), "arenas.yml");
            if (!arenasFile.exists()) {
                getLogger().info("arenas.yml not found, creating!");
                saveResource("arenas.yml", false);
            }
            this.arenasData = YamlConfiguration.loadConfiguration(arenasFile);

            File scoreFile = new File(getDataFolder(), "score.yml");
            if (!scoreFile.exists()) {
                getLogger().info("score.yml not found, creating!");
                saveResource("score.yml", false);
            }
            this.scoreData = YamlConfiguration.loadConfiguration(scoreFile);

            File scorestreakFile = new File(getDataFolder(), "scorestreak.yml");
            if (!scorestreakFile.exists()) {
                getLogger().info("scorestreak.yml not found, creating!");
                saveResource("scorestreak.yml", false);
            }
            this.scorestreakData = YamlConfiguration.loadConfiguration(scorestreakFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultValues() {
        //TODO: Complete this list for all config-related values
        Score.setupDefaultValues();
        Scorestreak.setupDefaultValues();
    }

    /**
     * Generalized command handler
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean result = false;

        //Send off the command to the commandHandler class to operate on
        if (cmd.getName().equalsIgnoreCase("dogfight")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                result = commandHandler.managePlayerCommand(player, args);
            } else {
                result = commandHandler.manageCommand(sender, args);
            }
        }

        return result;
    }

    /**
     * Get the game the player is currently in
     *
     * @param player
     * @return Game player is in or null if player is not in any game
     * @author R Lee
     */
    public Game getGame(Player player) {
        return this.gameByPlayer.get(player);
    }

    /**
     * Register that the player is part of the present game. If null is set as the game parameter, player is unregistered
     *
     * @param player
     * @param game
     * @author R Lee
     */
    public void registerGameToPlayer(Player player, Game game) {
        if (game != null) {
            this.gameByPlayer.put(player, game);
        } else {
            this.gameByPlayer.remove(player);
        }
    }

    public void logAlert(AlertLevel level, String message) {
        boolean showAlert = false;

        switch (showAlertType) {
            case ALL:
                showAlert = true;
                break;

            case GREATER:
                showAlert = (level.compareTo(messageAlertLevel) >= 0); //Show messages greater (more severe) than the messageAlertLevel
                break;

            case LESSER:
                showAlert = (level.compareTo(messageAlertLevel) <= 0); //Show messages lesser (more severe) than the messageAlertLevel
                break;

            case OFF:
                showAlert = false;
                break;

            case ONLY:
                showAlert = (level == messageAlertLevel);
                break;

            default:
                showAlert = false;
                break;

        }

        if (showAlert) {
            getLogger().info("*" + level.getDisplayName() + "* " + message);

            for (Player viewer : alertViewers) {
                CommandHandler.sendMessage(viewer, "[" + ChatColor.RED + "Df Alert-" + level.getDisplayName() + ChatColor.RESET + "] " + message);
            }
        }
    }
}
