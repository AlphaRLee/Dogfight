package io.github.alpharlee.dogfight.commandhandler;

import org.bukkit.entity.Player;

public interface SubCommandHandler {
    public boolean onSubCommand(Player player, String[] args);
}
