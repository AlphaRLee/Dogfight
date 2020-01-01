package io.github.alpharlee.dogfight.arena;

import io.github.alpharlee.dogfight.exceptions.ArenaException;
import io.github.alpharlee.dogfight.game.Game;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used to store information about arenas.
 * <p>
 * For small servers it will save needed information to a separate yml file
 * <p>
 * For large servers it will save needed information into a database
 */
public class ArenaInformation {

    private static ArrayList<Arena> arenaList = new ArrayList<>();
    private static HashMap<Arena, Boolean> arenaInUse = new HashMap<>();
    private static HashMap<String, Arena> arenaByName = new HashMap<>();
    private static HashMap<World, Arena> arenaByWorld = new HashMap<>();
    /**
     * Entry will always be null if {@link ArenaInformation#arenaInUse(Arena)} == false;
     */
    private static HashMap<Arena, Game> gameByArena = new HashMap<>();

    public static void addArena(Arena arena) {
        arenaList.add(arena);
        arenaInUse.put(arena, false);
        arenaByName.put(arena.getName(), arena);
        arenaByWorld.put(arena.getWorld(), arena);
        gameByArena.put(arena, null);
    }

    /**
     * Checks if this arena is in use
     *
     * @param arena
     * @return
     * @author R Lee
     */
    public static boolean arenaInUse(Arena arena) {
        return arenaInUse.get(arena);
    }

    /**
     * Checks if this arena is in the specified world
     *
     * @param world
     * @return
     * @author R Lee
     */
    public static boolean isArenaWorld(World world) {
        return arenaByWorld.containsKey(world);
    }

    public static Arena getArena(String arenaName) throws ArenaException {
        if (arenaByName.containsKey(arenaName)) {
            return arenaByName.get(arenaName);
        }
        throw new ArenaException(arenaName, ArenaException.ArenaExceptionReason.NOT_FOUND);
    }

    public static Arena getArena(World arenaWorld) throws ArenaException {
        if (arenaByWorld.containsKey(arenaWorld)) {
            return arenaByWorld.get(arenaWorld);
        }
        throw new ArenaException(arenaWorld.getName(), ArenaException.ArenaExceptionReason.NOT_FOUND);
    }

    public static void setGameForArena(Game game, Arena arena) throws ArenaException {
        if (!arenaInUse(arena)) {
            arenaInUse.replace(arena, true);
            gameByArena.replace(arena, game);
        } else {
            throw new ArenaException(arena.getName(), ArenaException.ArenaExceptionReason.IN_USE);
        }
    }

    public static void removeGameForArena(Arena arena) throws ArenaException {
        if (arenaInUse(arena)) {
            gameByArena.replace(arena, null);
            arenaInUse.replace(arena, false);
        } else {
            throw new ArenaException(arena.getName(), ArenaException.ArenaExceptionReason.NOT_IN_USE);
        }
    }

}
