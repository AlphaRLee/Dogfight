package io.github.alpharlee.dogfight.commandhandler.subcommands;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.commandhandler.SubcommandHandler;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.registry.PlayerRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static io.github.alpharlee.dogfight.commandhandler.CommandHandler.cmdArgs;
import static io.github.alpharlee.dogfight.commandhandler.CommandHandler.sendError;
import static io.github.alpharlee.dogfight.commandhandler.CommandHandler.sendMessage;

public class TeamCommandHandler implements SubcommandHandler {
    private Map<String, SubcommandHandler> subcommands;
    // FIXME Double check: multiple players in different games will be using this same member variable. So far it's fine because this is stateless. Right?
    private Game game;

    public TeamCommandHandler() {
        subcommands = new HashMap<>();
        subcommands.put("join", new JoinTeamCommandHandler());
        subcommands.put("list", new ListTeamsCommandHandler());
    }

    public boolean onSubcommand(Player player, String[] args) {
        final int subCommandArg = 0;
        final int teamNameArg = 1;

        // TODO Resolve non-player senders.
//        if (sender instanceof Player) {
//            player = (Player) sender;
//        } else {
//            //TODO: Handle console (eg. get game)
//            sendError(sender, "Sorry! We're still working on this. For now, play the game as a player before running this command");
//            return true;
//        }

        if (player != null && !player.hasPermission("dogfight.team")) {
            sendError(player, "Sorry, teamwork is important, but that doesn't mean you have permission to look here");
            return true;
        }

        if (args.length <= subCommandArg) {
            sendError(player, "Usage: /df team <join|leave|name|create|remove>");
            return true;
        }

        game = Dogfight.instance.getGame(player);
        if (game == null) {
            sendError(player, "Sorry, try joining a game first before you run this command");
            return true;
        }

        SubcommandHandler subcommand = subcommands.get(args[0].toLowerCase());

// TODO Restore this concise code block. For now, need to be able to subcommands that aren't refactored yet
//        if (subcommand == null) {
//            sendError(player, "Usage: /df team <join|leave|name|create|remove>");
//            return false;
//        }
//
//        subcommand.onSubcommand(player, cmdArgs(args));

        // TODO Remove this if statement. See TODO above.
        if (subcommand != null) {
            return subcommand.onSubcommand(player, cmdArgs(args));
        }

        // TODO Refactor everything beneath this to be part of the code above ------------------------------


        switch (args[subCommandArg].toLowerCase()) {
// TODO delete
//            case "join":
//
//                if (args.length > teamNameArg) {
//                    if (player != null && !player.hasPermission("dogfight.team.join")) {
//                        sendError(player, "Sorry, we can't allow you to join this team. Seems like you don't have permission. It's becau-CLASSIFIED");
//                        return true;
//                    }
//
//                    joinTeamCommand(player, args[teamNameArg]);
//                } else {
//                    sendError(player, "Usage: /df team join <team name>");
//                    return true;
//                }
//
//                break;

            case "leave":
                /*
                 * TODO: fill me in
                 * Possibilities:
                 * -Set player to spectator
                 * -Kick from game
                 */
                sendError(player, "Surprise! This command...actually does nothing. Try /df leave to leave the game");
                break;

            case "create":
                nameTeamCommand(player, cmdArgs(args), true);
                break;

            case "remove":

                if (args.length > teamNameArg) {
                    if (player != null && !player.hasPermission("dogfight.team.remove")) {
                        sendError(player, "Sorry, but you shouldn't try to end teamwork like this. It would make us sad");
                        return true;
                    }

                    Team targetTeam = game.getPlayerRegistry().getTeam(args[teamNameArg]);

                    if (targetTeam != null) {
                        //Attempt to remove the team
                        if (game.getPlayerRegistry().removeTeam(targetTeam)) {
                            sendMessage(player, ChatColor.RED + "Team " + targetTeam.getDisplayName() + ChatColor.RED + " has ceased to exist");
                        } else {
                            sendMessage(player, ChatColor.RED + "That makes life easier for everyone...the team '" + targetTeam.getDisplayName() + ChatColor.RED + "' isn't even in this game");
                        }
                    } else {
                        sendError(player, "Sorry! We couldn't find any team named '" + args[teamNameArg] + "'. Maybe check your spelling and try again");
                        return true;
                    }
                } else {
                    sendError(player, "Usage: /df team remove <team name>");
                    return true;
                }

                break;

//            case "list":
//
//                if (player != null && !player.hasPermission("dogfight.team.list")) {
//                    sendError(player, "Sorry, how many teams " + ChatColor.ITALIC + " really " + ChatColor.RED + " exists shall remain a mystery");
//                    return true;
//                }
//
//                int teamCount = game.getPlayerRegistry().getTeams().size();
//                String teamsWord = teamCount == 1 ? " team:" : " teams:";
//                sendMessage(player, ChatColor.YELLOW + "This Dogfight game has " + teamCount + teamsWord);
//
//                for (Team listedTeam : game.getPlayerRegistry().getTeams()) {
//                    sendMessage(player, false, ChatColor.YELLOW + "-" + ChatColor.RESET + listedTeam.getName()
//                            + ChatColor.YELLOW + ", Display name: " + ChatColor.RESET + listedTeam.getDisplayName());
//                }

//                break;

            case "name":
                nameTeamCommand(player, cmdArgs(args), false);
                break;

            default:
                sendError(player, "Subcommand '" + args[subCommandArg] + "' not recognized. This teamwork works with one of these options: join, leave, name, create, remove");
                return true;
        }

        return false;
    }

    /**
     * Handles this command:
     *  /df team join <team> [player]
     */
    private static class JoinTeamCommandHandler implements SubcommandHandler {
        @Override
        public boolean onSubcommand(Player player, String[] args) {
            final int teamNameArg = 0;

            if (args.length <= teamNameArg) {
                sendError(player, "Usage: /df team join <team name>");
                return false;
            }

            Player targetPlayer = getTargetPlayer(player, args);
            if (targetPlayer == null) {
                sendError(player, "Sorry, who are you looking for?");
                return false;
            }

            if (!player.hasPermission("dogfight.team.join")) {
                sendError(player, "Sorry, we can't allow you to join this team. Seems like you don't have permission. It's becau-CLASSIFIED");
                return true;
            }

            Game game = Dogfight.instance.getGame(targetPlayer);
            if (game == null) {
                // TODO What if the player and targetPlayer are in different games? Currently considering throwing an error message
                // TODO Generalize error message for target player
                sendError(player, "Sorry, you need to be part of a game before you can be part of a team in that game. Try /df join <game>");
                return true;
            }

            PlayerRegistry playerRegistry = game.getPlayerRegistry();
            String teamName = args[teamNameArg];
            Team team = playerRegistry.getTeam(teamName);

            if (team == null) {
                sendError(player, "Sorry! We couldn't find any team named '" + teamName + "'. Maybe check your spelling and try again");
                return true;
            }

            playerRegistry.setPlayerToTeam(targetPlayer, team);
            sendMessage(targetPlayer, ChatColor.AQUA + "Welcome to the " + ChatColor.RESET + team.getDisplayName() + ChatColor.AQUA + " team");

            // Notify player if player isn't target player
            if (!player.equals(targetPlayer)) {
                sendMessage(player, ChatColor.AQUA + "You have moved " + targetPlayer.getName() + ChatColor.AQUA + " to the " + ChatColor.RESET + team.getDisplayName() + ChatColor.AQUA + " team");
            }
            return true;
        }

        // TODO Merge this code into single utility function (new)
        private Player getTargetPlayer(Player player, String[] args) {
            if (args.length <= 2) {
                return player;
            }

            String targetPlayerName = args[2];
            if (player.getName().equalsIgnoreCase(targetPlayerName)) {
                return player;
            }

            if (!player.hasPermission("dogfight.team.join.others")) {
                sendError(player, "Sorry, you can't force people to do what you want. Maybe try asking them?");
                return null;
            }

            return Dogfight.instance.getServer().getPlayer(targetPlayerName);
        }
    }

    private class ListTeamsCommandHandler implements SubcommandHandler {
        public boolean onSubcommand(Player player, String[] args) {
            if (player != null && !player.hasPermission("dogfight.team.list")) {
                sendError(player, "Sorry, how many teams " + ChatColor.ITALIC + " really " + ChatColor.RED + " exist shall remain a mystery to you.");
                return true;
            }

            int teamCount = game.getPlayerRegistry().getTeams().size();
            String teamsWord = teamCount == 1 ? " team:" : " teams:";
            sendMessage(player, ChatColor.YELLOW + "This Dogfight game has " + teamCount + teamsWord);

            for (Team listedTeam : game.getPlayerRegistry().getTeams()) {
                sendMessage(player, false, ChatColor.YELLOW + "-" + ChatColor.RESET + listedTeam.getName()
                        + ChatColor.YELLOW + ", Display name: " + ChatColor.RESET + listedTeam.getDisplayName());
            }

            return true;
        }
    }


    private void nameTeamCommand(CommandSender sender, String[] args, boolean createTeam) {
        //TODO: Clean spaghetti code concerning createTeam. Centralize all "createTeam" references to one point)

        final int teamNameArg = 0;
        final int dispNameArg = 1;

        Player player;

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sendError(sender, "Sorry, we're working on making this compatible for non-players");
            return;
        }

        Game game = Dogfight.instance.getGame(player);
        PlayerRegistry playerRegistry = null;
        Team team = null;

        //TODO: Edit this segment. Require game as an input instead of test game
        if (game == null) {
            game = Dogfight.instance.testGame;
        }
        playerRegistry = game.getPlayerRegistry();
        team = playerRegistry.getTeam(player);

        if (args.length > teamNameArg) {
            //Attempting to create team
            if (createTeam) {
                if (player.hasPermission("dogfight.team.create")) {
                    team = new Team(game);
                } else {
                    sendError(sender, "Sorry, you can't kickstart your own team right now");
                    return;
                }
            }
            //Not creating team, check if team is already given
            else if (team != null) {
                if (!player.hasPermission("dogfight.team.name")) {
                    sendError(sender, "Sorry, you can't rename your team. Allowing you to do so might be rather...chaotic");
                }
            } else {
                sendError(sender, "Sorry, it's hard to get the name of something that doesn't exist");
                return;
            }

            //Attempt to set the name
            if (team.setName(args[teamNameArg], playerRegistry)) {
                if (createTeam) {
                    sendMessage(sender, ChatColor.AQUA + "You have created team: " + ChatColor.WHITE + team.getName());
                } else {
                    sendMessage(sender, ChatColor.AQUA + "Your team name is now: " + ChatColor.WHITE + team.getName());
                }

                if (args.length > dispNameArg) {
                    //Attempt to set the display name
                    if (team.setDisplayName(args[dispNameArg], playerRegistry)) {
                        sendMessage(sender, false, ChatColor.DARK_AQUA + "The team's display name is: " + ChatColor.RESET + team.getDisplayName());
                    } else {
                        sendError(sender, "Sorry, setting your display name failed because it is already taken by another team. Meanies");
                        return;
                    }
                }

                //Register team only after successfully setting the name
                //Wait for display name to be attempted to be set to accurately portray the display name
                if (createTeam) {
                    playerRegistry.addTeam(team);
                }
            } else {
                sendError(sender, "Sorry, setting your name failed because it is already taken by another team. Try being original!");

                //Destroy the team
                if (createTeam) {
                    sendError(sender, "Your team creation process has been aborted");
                    team = null;
                }

                return;
            }
        } else {
            if (team != null && !createTeam) {
                sendMessage(sender, ChatColor.AQUA + "Your team's name is: " + team.getName(),
                        ChatColor.AQUA + "The display name is: " + team.getDisplayName(),
                        ChatColor.RED + "To rename, use: /df team name <new name> [display name]");
            } else {
                sendError(sender, "If you're trying to create a team: /df team create <team name> [display name]",
                        "Otherwise, you need to be on a team to run: /df team name <team name> [display name]");
            }
        }
    }
}
