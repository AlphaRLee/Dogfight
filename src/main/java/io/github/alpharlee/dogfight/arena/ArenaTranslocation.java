package io.github.alpharlee.dogfight.arena;

import org.bukkit.entity.Player;

public class ArenaTranslocation {
    private ArenaTranslocation() {
    }

    /**
     * Teleports players to the arena's spawnInPoint
     * <p>
     * This method will only work for small servers with arenas in different worlds but still on the same server
     *
     * @param a       arena to teleport players to
     * @param players players to teleport to arena
     */
    public static void teleportPlayersToArena(Arena a, Player... players) {
        for (Player p : players) {
            p.teleport(a.getSpawnInPoint());
        }
    }

}
