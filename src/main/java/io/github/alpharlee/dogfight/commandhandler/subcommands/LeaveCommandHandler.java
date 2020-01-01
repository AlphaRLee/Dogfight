package io.github.alpharlee.dogfight.commandhandler.subcommands;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.commandhandler.SubcommandHandler;
import org.bukkit.entity.Player;

import static io.github.alpharlee.dogfight.commandhandler.CommandHandler.sendError;
import static io.github.alpharlee.dogfight.commandhandler.CommandHandler.sendMessage;

public class LeaveCommandHandler implements SubcommandHandler {
    public boolean onSubcommand(Player player, String[] args) {
        Player targetPlayer = getTargetPlayer(player, args);

        if (targetPlayer == null) {
            sendError(player, "I'm sorry, who are you looking for?");
            return false;
        }

        // TODO Dynamically select game
        Dogfight.instance.testGame.getPlayerRegistry().removePlayer(targetPlayer);
        sendMessage(targetPlayer, "You have been removed from the dogfight test game"); // TODO Update this message

        //Send message if player selected a different player
        if (!targetPlayer.equals(player)) {
            sendMessage(player, "You have removed " + targetPlayer.getDisplayName() + " from the test game");
        }

        return true;
    }

    private Player getTargetPlayer(Player player, String[] args) {
        if (args.length > 1) {
            if (player.hasPermission("dogfight.leave.others")) {
                return Dogfight.instance.getServer().getPlayer(args[1]);
            } else {
                sendError(player, "Sorry, you can't force people to do what you want. Maybe try asking them?");
                return null;
            }
        } else {
            return player;
        }
    }
}
