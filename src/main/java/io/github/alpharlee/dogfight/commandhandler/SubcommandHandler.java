package io.github.alpharlee.dogfight.commandhandler;

import org.bukkit.entity.Player;

public interface SubcommandHandler {
    public boolean onSubcommand(Player player, String[] args);
}
