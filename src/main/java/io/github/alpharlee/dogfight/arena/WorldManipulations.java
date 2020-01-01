package io.github.alpharlee.dogfight.arena;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.exceptions.WorldException;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class WorldManipulations {

    /**
     * Do not instantiate
     */
    private WorldManipulations() {
    }

    /**
     * Creates world with specified name, the generated world file will need to be replaced with the area map
     *
     * @param worldName Name of the world you want to generate
     * @throws WorldException Thrown if world name is already being used for another world
     */
    public static void createNewWorld(String worldName) throws WorldException {

        assert worldName != null;

        for (World w : Dogfight.instance.getServer().getWorlds()) {
            if (worldName.equals(w.getName())) {
                throw new WorldException(worldName, WorldException.WorldExceptionReason.NAME_IN_USE);
            }
        }

        Arena a = new Arena(Dogfight.instance.getServer().createWorld(new WorldCreator(worldName)));
        ArenaInformation.addArena(a);
    }
}
