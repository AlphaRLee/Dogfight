package io.github.alpharlee.dogfight;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import io.github.alpharlee.dogfight.alert.AlertLevel;
import io.github.alpharlee.dogfight.alert.ShowAlertType;
//import io.github.alpharlee.dogfight.floatingentity.ItemTarget; FIXME evaluate import
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.projectile.ProjectileType;
import io.github.alpharlee.dogfight.registry.PlayerRegistry;
import io.github.alpharlee.dogfight.score.Score;

public class CommandHandler
{
	public CommandHandler()
	{
		
	}
	
	/**
	 * Manage commands sent from a player
	 * @param player
	 * @param args
	 * @return
	 *
	 * @author R Lee
	 */
	public boolean managePlayerCommand(Player player, String[] args)
	{
		boolean result = false;
		
		//TODO: Move to more formal location
		//Target player for operations not centered around sender
		Player targetPlayer = null;
		Team targetTeam = null;
		
		if (args.length > 0)
		{
			//Get the first argument
			switch (args[0].toLowerCase())
			{	
			case "give":
				
				giveItemCommand(player, cmdArgs(args));
				break;
			
			case "jointest": case "jt":
				//Add player to test game
				
				if (args.length > 1)
				{
					//TODO: Add game filter
					targetTeam = Dogfight.instance.testGame.getPlayerRegistry().getTeam(args[1]);
					
					//ATTEMPT to create team if getting team failed
					if (targetTeam == null)
					{
						nameTeamCommand(player, cmdArgs(cmdArgs(args)), true);
						targetTeam = Dogfight.instance.testGame.getPlayerRegistry().getTeam(args[1]);
					}
				}
		
				//Attempt to join game
				if (Dogfight.instance.testGame.getPlayerRegistry().addPlayer(player, targetTeam))
				{
					sendMessage(player, "You have been added to the dogfight test game"); //TODO: Edit this line
				
					//Make attempt to grab name, assuming joining game did not fail
					if (targetTeam == null)
					{
						targetTeam = Dogfight.instance.testGame.getPlayerRegistry().getTeam(player);
					}
					
					sendMessage(player, ChatColor.YELLOW + "You are on team " + targetTeam.getDisplayName());
				}
				
				//Clean targetTeam for following commands
				targetTeam = null;
				break;
			
			case "leave":
				
				if (args.length > 1)
				{
					if (player.hasPermission("dogfight.leave.others"))
					{
						targetPlayer = Dogfight.instance.getServer().getPlayer(args[1]);
					}
					else
					{
						sendError(player, "Sorry, you can't force people to do what you want. Maybe try asking them?");
					}
				}
				else
				{
					targetPlayer = player;
				}
				
				if (targetPlayer != null)
				{
					Dogfight.instance.testGame.getPlayerRegistry().removePlayer(targetPlayer);
					sendMessage(targetPlayer, "You have been removed from the dogfight test game");
					
					//Send message if player selected a different player
					if (!targetPlayer.equals(player))
					{
						sendMessage(player, "You have removed " + targetPlayer.getDisplayName() + " from the test game");
					}
				}
				else
				{
					sendError(player, "I'm sorry, who are you looking for?");
				}
				
				//Clean aimAtPlayer for following commands
				targetPlayer = null;
				
				break;
			
			case "forceglide": case "fg":
				
				//If another player has been specified
				if (args.length > 2)
				{
					if (player.hasPermission("dogfight.forceglide.others"))
					{
						targetPlayer = Dogfight.instance.getServer().getPlayer(args[2]);
					}
					else
					{
						sendError(player, "Hey, that's dangerous to mess with other people's landing gear");
					}
				}
				else
				{
					targetPlayer = player;
				}
				
				if (player.hasPermission("dogfight.forceglide"))
				{
					if (targetPlayer != null)
					{
						boolean forcedGlide = false;
						
						if (args.length > 1)
						{				
							switch (args[1])
							{
							case "true": case "yes": case "on": case "1":
								forcedGlide = true;
								break;
								
							case "false": case "no": case "off": case "0":
								forcedGlide = false;
								break;
								
							default:
								//Toggle state
								forcedGlide = !Dogfight.instance.getBoostHandler().forcedGliders.contains(targetPlayer);
								break;
							}
						}
						else
						{
							//Toggle state
							forcedGlide = !Dogfight.instance.getBoostHandler().forcedGliders.contains(targetPlayer);
						}
						
						Dogfight.instance.getBoostHandler().setForcedGlide(targetPlayer, forcedGlide);
						
						//Send message if player selected a different player
						if (!targetPlayer.equals(player))
						{
							sendMessage(player, ChatColor.AQUA + "You have changed " + targetPlayer.getDisplayName() + ChatColor.AQUA + "'s force glide settings");
						}
					}
					else
					{
						sendError(player, "I'm sorry, who are you looking for?");
					}
				}
				else
				{
					sendError(player, "You're not a certified technician to control the landing gear");
				}
				
				//Clean aimAtPlayer for following commands
				targetPlayer = null;
				
				break;
				
			case "showalert": case "sa":
				//TODO: Clean up command syntax
				
				if (!player.hasPermission("dogfight.alert.see"))
				{
					sendError(player, "Sorry, you're not allowed to peek under the hood for Dogfight");
					break;
				}
				
				List<Player> alertViewers = Dogfight.instance.alertViewers;
				
				if (!alertViewers.contains(player))
				{
					alertViewers.add(player);
					sendMessage(player, "Dogfight alerts are now visible!");
				}
				else
				{
					alertViewers.remove(player);
					sendMessage(player, "No more dogfight alerts. Ignorance is bliss");
				}
			
				break;
			
			//TODO: Remove test commands or modify them to admin level
			case "team":
				teamCommand(player, cmdArgs(args));
				break;
				
			case "score":
				scoreCommand(player, cmdArgs(args));
				break;
/* FIXME Evaluate ItemTarget import code
			case "spawntarget": case "spawn-target": case "st":
				
				if (!player.hasPermission("dogfight.spawn.target"))
				{
					sendError(player, "Sorry, you can't drop your own target gizmo here");
					break;
				}
				
				Game game = Dogfight.instance.getGame(player);
				
				if (game == null)
				{
					sendError(player, "Sorry, try running this command when you're in a Dogfight game");
					break;
				}
				
				ItemTarget itemTarget = new ItemTarget(game.getFloatingEntityRegistry(), (EnderCrystal) player.getWorld().spawnEntity(player.getLocation(), EntityType.ENDER_CRYSTAL), 10.0);
				
				//------Test item 1---------------
				ItemStack itemStack = new ItemStack(Material.POTION);
				PotionMeta pMeta = (PotionMeta) itemStack.getItemMeta();
				
				pMeta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
				itemStack.setItemMeta(pMeta);
				
				itemTarget.setItemDrop(itemStack, 16);
				
				//------Test item 2---------------
				itemStack = new ItemStack(ProjectileType.FIRE_BULLET.getItemStack());
				itemStack.setAmount(16);
				itemTarget.setItemDrop(itemStack, 8);
				
				//------Test item 3---------------
				itemStack = new ItemStack(ProjectileType.SHOTGUN.getItemStack());
				itemStack.setAmount(8);
				itemTarget.setItemDrop(itemStack, 4);
				
				//------Test item 4---------------
				itemStack = new ItemStack(ProjectileType.SNIPER.getItemStack());
				itemStack.setAmount(4);
				itemTarget.setItemDrop(itemStack, 2);
				
				//------Test item 5---------------
				itemStack = new ItemStack(ProjectileType.SQUID_SHOT.getItemStack());
				itemStack.setAmount(4);
				itemTarget.setItemDrop(itemStack, 2);
				
				//------Test item 6---------------
				itemStack = new ItemStack(ProjectileType.MINE.getItemStack());
				itemStack.setAmount(1);
				itemTarget.setItemDrop(itemStack, 2);
				
				//------Test item 7---------------
				itemStack = new ItemStack(ProjectileType.MISSILE.getItemStack());
				itemStack.setAmount(1);
				itemTarget.setItemDrop(itemStack, 1);
				
				//------Test item 8---------------
				itemStack = new ItemStack(ProjectileType.PLAYER_MISSILE.getItemStack());
				itemStack.setAmount(1);
				itemTarget.setItemDrop(itemStack, 0.5);
				
				game.getFloatingEntityRegistry().addFloatingEntity(itemTarget);
				
				sendMessage(player, "Dogfight DEBUG: Target has been spawned at your feet");
				
				//Clean off
				game = null;
				itemTarget = null;
				itemStack = null;
				
				break;
*/ // End of spawntarget command
			default:	
				//Attempt to see if command is under general manageCommand
				manageCommand(player, args);
				break;
			}	
		}
		else
		{
			sendError(player, "Please type /dogfight help for a list of commands");
		}
		
		return result;
	}	

	/**
	 * Manage commands from any command sender
	 * @param sender
	 * @param args
	 * @return
	 *
	 * @author R Lee
	 */
	public boolean manageCommand(CommandSender sender, String[] args)
	{
		boolean result = true;
		
		if (args.length > 0)
		{			
			//Get the first argument
			switch (args[0].toLowerCase())
			{	
			case "version":
				sendMessage(sender, ChatColor.GOLD + "You are running Dogfight version: " + ChatColor.AQUA + Dogfight.instance.getDescription().getVersion());
				sendMessage(sender, ChatColor.YELLOW + "Make sure you check frequently for more funtatic updates for your flying adventure");
				break;
			
			case "logalert": case "la":
				logAlertCommand(sender, cmdArgs(args));
				break;
			
			case "autojoin":
				
				Player targetPlayer = null;
				String targetName = null;
				
				//Permision check
				if (sender instanceof Player)
				{
					if (!((Player) sender).hasPermission("dogfight.autojoin.set"))
					{
						sendError(sender, "Sorry, you don't have permision to use the autojoin command");
						return true;
					}
					
					targetPlayer = (Player) sender; //Set sender to this player
					targetName = targetPlayer.getName();
				}
				
				//Selecting target player
				if (args.length > 1)
				{
					//Permission check
					if (sender instanceof Player)
					{
						if (!((Player) sender).hasPermission("dogfight.autojoin.set.others"))
						{
							sendError(sender, "Sorry, you don't have permision to configure other's autojoin settings");
							return true;
						}
					}
					
					targetName = args[1];
					targetPlayer = Dogfight.instance.getServer().getPlayer(targetName); //Deal with offline players
				}
				
				FileConfiguration configFile = Dogfight.instance.getConfig();
				
				if (configFile == null)
				{
					sendError(sender, "Sorry, but somehow, you don't have a config.yml file for Dogfight here");
					return true;
				}
				
				List<String> autoJoinNames = configFile.getStringList("autojoin");
				
				//Test if entry already exists
				if (autoJoinNames.contains(targetName))
				{
					autoJoinNames.remove(targetName); //Remove the name
					
					if (targetPlayer != null)
					{
						sendMessage(targetPlayer, "You will no longer auto-join Dogfight test games on log-in");
					}
					
					if (!sender.equals(targetPlayer))
					{
						sendMessage(sender, "Player '" + targetName + "' will no longer auto-join the Dogfight test game on login");
					}
				}
				else
				{
					autoJoinNames.add(targetName); //Add the name
					
					if (targetPlayer != null)
					{
						sendMessage(targetPlayer, "You will now auto-join Dogfight test games on log-in. Have fun blowing up stuff!");
					}
		
					if (!sender.equals(targetPlayer))
					{
						sendMessage(sender, "Player '" + targetName + "' will now auto-join the Dogfight test game on login");
					}
				}
					
				configFile.set("autojoin", autoJoinNames);
				Dogfight.instance.saveConfig();
				
				targetPlayer = null;
				targetName = null;
				autoJoinNames = null;
				
				break;
				
			case "help": case "?":
				
				//TODO: Complete
				//TODO: Hide commands for users without permissions
				//Order: Colour, command name, command description
				//"/df" is prefixed at the front when displayed
				String[] helpArray = {
						ChatColor.GOLD + "help" + ChatColor.WHITE + ": Displays this lovely page",
						ChatColor.GOLD + "version" + ChatColor.WHITE + ": Get the version number of Dogfight",
						ChatColor.GOLD + "give" + ChatColor.WHITE + ": Gives Dogfight items",
	                    ChatColor.GRAY + "jointest [team]" + ChatColor.WHITE + ": Join the test game, optionally to a specific team",
	                    ChatColor.GRAY + "leave" + ChatColor.WHITE + ": Leave the test game",
	                    ChatColor.GRAY + "forceglide" + ChatColor.WHITE + ": Change if you (or others) are forced to land or not",
	                    ChatColor.GRAY + "team" + ChatColor.WHITE + ": Ways to have fun editing, making and breaking teams!",
	                    ChatColor.GRAY + "score" + ChatColor.WHITE + ": All things you ever wanted to do with the score (well, some things)",
	                    ChatColor.GRAY + "autojoin" + ChatColor.WHITE + ": Toggle if you (or others) automatically join the Dogfight test game on log in",
	                    ChatColor.GRAY + "showalert" + ChatColor.WHITE + ": Admin debugging tool to view alerts (as a player)",
	                    ChatColor.GRAY + "logalert" + ChatColor.WHITE + ": Admin debugging tool to log alerts to console"
				};
				
				sendMessage(sender, ChatColor.RED + "Under construction! Here's a brief summary. Grey commands may disappear without warning. Btw, Find me a Jelly Dragon ;)");
				
				String[] outputArray = new String[helpArray.length + 1];
				
				outputArray[0] = ChatColor.YELLOW + "==========" + ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Dogfight Help Page" + ChatColor.DARK_GRAY + "]" + ChatColor.YELLOW + "==========";
				//Copy over help array and add in the /df prefix
				for (int i = 0; i < helpArray.length; i++)
				{
					outputArray[i + 1] = ChatColor.GOLD + "/df " + helpArray[i] + ChatColor.RESET;
				}
				
				sendMessage(sender, false, outputArray);
				
				break;
				
			default:
				sendError(sender, "Please type /dogfight help for a list of commands");
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Send a message or a set of messages to a recipient (player or console), with the "[Dogfight]" prefix
	 * @param recipient Message receiver. Must either be a player or an instance of ConsoleCommandSender
	 * @param messages Messages to be given to receiver. If the recipient is a player then the first message will be prefixed with "[Dogfight]"
	 *
	 * @author R Lee
	 */
	public static void sendMessage(CommandSender recipient, String... messages)
	{
		sendMessage(recipient, true, messages);
	}
	
	/**
	 * Send a message or a set of messages to a recipient (player or console)
	 * @param recipient Message receiver. Must either be a player or an instance of ConsoleCommandSender. Null also works for a ConsoleCommandSender
	 * @param showPrefix Set to true to include the "[Dogfight]" message prefix
	 * @param messages Messages to be given to receiver. If the recipient is a player then the first message will be prefixed with "[Dogfight]"
	 *
	 * @author R Lee
	 */
	public static void sendMessage(CommandSender recipient, boolean showPrefix, String... messages)
	{
		String prefix = "";
		
		if (showPrefix)
		{
			prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.ITALIC + "Dogfight" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
		}
		
		if (recipient instanceof Player)
		{
			Player player = (Player) recipient;
			
			//Send first message wih prefix in front
			player.sendMessage(prefix + messages[0]);
			
			if (messages.length > 1)
			{
				//NOTE: Iterator starts at 1 (second message), NOT 0
				for (int i = 1; i < messages.length; i++)
				{
					player.sendMessage(messages[i]);
				}
			}
		}
		else if (recipient instanceof ConsoleCommandSender || recipient == null)
		{
			Bukkit.getServer().getLogger().info(prefix + messages[0]);
			
			if (messages.length > 1)
			{
				//NOTE: Iterator starts at 1 (second message), NOT 0
				for (int i = 1; i < messages.length; i++)
				{
					Bukkit.getServer().getLogger().info(messages[i]);
				}
			}
		}
	}
	
	/**
	 * Performs same function as {@link CommandHandler#sendMessage(CommandSender, String...)}, except colours all messages red
	 * @param recipient
	 * @param messages
	 *
	 * @author R Lee
	 */
	public static void sendError(CommandSender recipient, String... messages)
	{
		String[] errorMessages = new String[messages.length];
		
		for (int i = 0; i < errorMessages.length; i++)
		{
			errorMessages[i] = ChatColor.RED + messages[i];
		}
		
		sendMessage(recipient, errorMessages);
	}
	
	/**
	 * Performs method {@link CommandHandler#sendMessage(CommandSender, String...)}, except directed to everyone on this game's player registry
	 * @param game Game where all tracked players to will receive the message
	 * @param messages
	 *
	 * @author R Lee
	 */
	public static void broadcast(Game game, String... messages)
	{
		broadcast(game, true, messages);
	}
	
	public static void broadcast(Game game, boolean showPrefix, String... messages)
	{
		broadcast(game.getPlayerRegistry(), showPrefix, messages);
	}
	
	/**
	 * Performs method {@link CommandHandler#sendMessage(CommandSender, String...)}, except directed to everyone on the player registry
	 * @param playerRegistry Registry of all tracked players to send message to
	 * @param messages
	 *
	 * @author R Lee
	 */
	public static void broadcast(PlayerRegistry playerRegistry, String... messages)
	{
		broadcast(playerRegistry, true, messages);
	}
	
	public static void broadcast(PlayerRegistry playerRegistry, boolean showPrefix, String... messages)
	{
		broadcast(playerRegistry.getPlayers().keySet(), showPrefix, messages);
	}
	
	/**
	 * Performs method {@link CommandHandler#sendMessage(CommandSender, String...)}, except directed to everyone on this team
	 * @param team Team where all members to will receive the message
	 * @param messages
	 *
	 * @author R Lee
	 */
	public static void broadcast(Team team, String... messages)
	{
		broadcast(team, true, messages);
	}
	
	public static void broadcast(Team team, boolean showPrefix, String... messages)
	{
		broadcast(team.getMembers(), showPrefix, messages);
	}
	
	public static void broadcast(Set<Player> players, String... messages)
	{
		broadcast(players, true, messages);
	}
	
	public static void broadcast(Set<Player> players, boolean showPrefix, String... messages)
	{
		for (Player player : players)
		{
			sendMessage(player, showPrefix, messages);
		}
	}
	
	/**
	 * Using a command, give a player items. If player cannot carry items, items are dropped at feet.
	 * @param player Command executor. Must have dogfight.canGive permission
	 * @param args Command arguments. 
	 * arg 1: Target player name. Use "@p" to set to command executor
	 * arg 2: Item name
	 * arg 3: Item quantity. If not specified, defaulted to 1
	 *
	 * @author R Lee
	 */
	private void giveItemCommand(Player player, String[] args)
	{
		//Give player item
		//Syntax: <player> <item> [amount]
		
		//TODO: Properly configure permission
		//Requires give permission in order to use
		if (!player.hasPermission("dogfight.give"))
		{
			sendError(player, "Sorry, you're not eligible for free stuff with everything on top");
			return;
		}
		
		final int targetPlayerIndex = 0;
		final int itemIndex = 1;
		final int amountIndex = 2;
		
		//If the args contains the itemIndex
		if (args.length > itemIndex)
		{
			//Set the target player to the selected player or @p for the player executing the command
			Player targetPlayer = (args[targetPlayerIndex].equals("@p") ? player : Dogfight.instance.getServer().getPlayer(args[targetPlayerIndex]));
			ItemStack givenItem = null;
			
			//Check to see if player was found
			if (targetPlayer != null)
			{
				switch (args[itemIndex])
				{
				case "elytra":
					
					givenItem = new ItemStack(Material.ELYTRA);
					
					//Add unbreakable to the elytra
					ItemMeta elytraMeta = givenItem.getItemMeta();
					elytraMeta.setUnbreakable(true);
					givenItem.setItemMeta(elytraMeta);
					
					break;
					
				case "bullet": case "snowball":
					
					givenItem = new ItemStack(ProjectileType.BULLET.getItemStack());
					break;
				
				case "sniperbullet": case "sniper":
					
					givenItem = new ItemStack(ProjectileType.SNIPER.getItemStack());
					break;
			
				case "shotgun":
					
					givenItem = new ItemStack(ProjectileType.SHOTGUN.getItemStack());
					break;
				
				case "squidshot": case "squid":
					
					givenItem = new ItemStack(ProjectileType.SQUID_SHOT.getItemStack());
					break;
					
				case "mine":
					
					givenItem = new ItemStack(ProjectileType.MINE.getItemStack());
					break;
					
				case "firebullet": case "fire":
					
					givenItem = new ItemStack(ProjectileType.FIRE_BULLET.getItemStack());
					break;
					
				case "missile":
					
					givenItem = new ItemStack(ProjectileType.MISSILE.getItemStack());
					break;
					
				case "playermissile": case "pmissile":
					
					givenItem = new ItemStack(ProjectileType.PLAYER_MISSILE.getItemStack());
					break;
					
				default:
					break;
				}
				
				//Give the player the item if successfully set
				if (givenItem != null)
				{
					int itemAmount;
					
					//Set the item quantity
					//Default to 1 if illegal amount specified or no value specified
					if (args.length > amountIndex)
					{
						//TODO: Catch non-integers (without try-catch)
						itemAmount = Integer.parseInt(args[amountIndex]);

						//Give 1 if 0 or less are attempted to be given
						itemAmount = (itemAmount > 0 ? itemAmount : 1);
						
					}
					else
					{
						itemAmount = 1;
					}
					
					givenItem.setAmount(itemAmount);
					
					//Give the players the items and store the items that do not fit (see javadocs for Inventory#addItem()
					//Key represents item order
					//Value represents ItemStack that could not be stored
					HashMap<Integer, ItemStack> extraItems = targetPlayer.getInventory().addItem(givenItem);
					
					if (extraItems.isEmpty())
					{
						//All items were successfully given
						sendMessage(player, ChatColor.GOLD + "Item " + args[itemIndex] + " given");
					}
					else
					{
						//Spawn items that could not be stored on the the ground at the player's feet
						for (ItemStack itemStack : extraItems.values())
						{
							player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
						}
						
						sendError(player, "Inventory full, items dropped on ground");
					}
				}
				else
				{
					sendError(player, "Is '" + args[itemIndex] + "' a Dogfight item? I don't think that's a Dogfight item. Try again?");
				}
			}
			else
			{
				//Player not found
				sendError(player, "Sorry, who are you looking for?");
			}
		}
		else
		{
			//TODO: Update to proper list
			player.sendMessage(ChatColor.RED + "Syntax: /df give <player> <item> [amount]");
		}
	}

	private boolean teamCommand(CommandSender sender, String[] args)
	{
		final int subCommandArg = 0;
		final int teamNameArg = 1;
		
		Player player;
		
		if (sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			//TODO: Handle console (eg. get game)
			sendError(sender, "Sorry! We're still working on this. For now, play the game as a player before running this command");
			return true;
		}
		
		if (player != null && !player.hasPermission("dogfight.team"))
		{
			sendError(sender, "Sorry, teamwork is important, but that doesn't mean you have permission to look here");
			return true;
		}
		
		//TODO: Refine this assumption with error checking
		Game game = Dogfight.instance.getGame(player);
		
		//TODO: Demand game input as parameter
		if (game == null)
		{
			game = Dogfight.instance.testGame;
		}
		
		if (args.length > subCommandArg)
		{
			if (game != null)
			{
				switch (args[subCommandArg].toLowerCase())
				{
				case "join":
					
					if (args.length > teamNameArg)
					{
						if (player != null && !player.hasPermission("dogfight.team.join"))
						{
							sendError(sender, "Sorry, we can't allow you to join this team. It's becau-CLASSIFIED");
							return true;
						}
						
						joinTeamCommand(player, args[teamNameArg]);
					}
					else
					{
						sendError(sender, "Usage: /df team join <team name>");
						return true;
					}
					
					break;
				
				case "leave":
					/*
					 * TODO: fill me in
					 * Possibilities:
					 * -Set player to spectator
					 * -Kick from game
					 */
					sendError(sender, "Surprise! This command...actually does nothing. Try /df leave to leave the game");
					break;
				
				case "create":
					nameTeamCommand(player, cmdArgs(args), true);
					break;
				
				case "remove":
					
					if (args.length > teamNameArg)
					{
						if (player != null && !player.hasPermission("dogfight.team.remove"))
						{
							sendError(sender, "Sorry, but you shouldn't try to end teamwork like this. It would make us sad");
							return true;
						}
						
						Team targetTeam = game.getPlayerRegistry().getTeam(args[teamNameArg]);
						
						if (targetTeam != null)
						{
							//Attempt to remove the team
							if (game.getPlayerRegistry().removeTeam(targetTeam))
							{
								sendMessage(sender, ChatColor.RED + "Team " + targetTeam.getDisplayName() + ChatColor.RED + " has ceased to exist");
							}
							else
							{
								sendMessage(sender, ChatColor.RED + "That makes life easier for everyone...the team '" + targetTeam.getDisplayName() + ChatColor.RED + "' isn't even in this game");
							}
						}
						else
						{
							sendError(sender, "Sorry! We couldn't find any team named '" + args[teamNameArg] + "'. Maybe check your spelling and try again");
							return true;
						}
					}
					else
					{
						sendError(sender, "Usage: /df team remove <team name>");
						return true;
					}
					
					break;
				
				case "list":
					
					if (player != null && !player.hasPermission("dogfight.team.list"))
					{
						sendError(sender, "Sorry, how many teams " + ChatColor.ITALIC + " really " + ChatColor.RED + " exists shall remain a mystery");
						return true;
					}
					
					sendMessage(sender, ChatColor.YELLOW + "This Dogfight game has " + game.getPlayerRegistry().getTeams().size() + " teams: ");
					
					for (Team listedTeam : game.getPlayerRegistry().getTeams())
					{
						sendMessage(sender, false, ChatColor.YELLOW + "-" + ChatColor.RESET + listedTeam.getName() 
						+ ChatColor.YELLOW + ", Display name: " + ChatColor.RESET + listedTeam.getDisplayName());
					}
					
					break;
					
				case "name":
					nameTeamCommand(player, cmdArgs(args), false);
					break;
					
				default:	
					sendError(sender, "Subcommand '" + args[subCommandArg] + "' not recognized. This teamwork works with one of these options: join, leave, name, create, remove");
					return true;
				}
			}
			else
			{
				sendError(sender, "Sorry, try joining a game first before you run this command");
				return true;
			}
		}
		else
		{
			sendError(sender, "Usage: /df team <join|leave|name|create|remove> [name]");
			return true;
		}
		
		return true;
	}
	
	/**
	 * Score command handler
	 * @param sender Command sender
	 * @param args Command arguments, where first arg (index of 0) is subcommand, second arg is integer value (if applicable)
	 * @return
	 *
	 * @author R Lee
	 */
	private boolean scoreCommand(CommandSender sender, String[] args)
	{
		final int subCommandArg = 0;
		final int valueArg = 1;
		
		Player player;
		
		if (sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			//TODO: Handle console (eg. get game)
			sendError(sender, "Sorry! We're still working on this. For now, play the game as a player before running this command");
			return true;
		}
		
		if (player != null && !player.hasPermission("dogfight.score"))
		{
			sendError(sender, "If you're gonna shoot for gold, you're gonna have to do it the honest way. No peeking here!");
			return true;
		}
		
		//TODO: Refine this assumption with error checking
		Game game = Dogfight.instance.getGame(player);
		Team team = null;
		Score score = null;
		
		if (game != null)
		{
			team = game.getPlayerRegistry().getTeam(player);
			
			if (team != null)
			{
				score = game.getScoreRegistry().getScore(team);
			}
		}
		
		if (args.length > subCommandArg)
		{
			if (team != null && score != null)
			{
				switch (args[subCommandArg].toLowerCase())
				{
				case "set":
				
					//Validate permission
					if (player != null && !player.hasPermission("dogfight.score.set"))
					{
						sendError(sender, "Sorry, setting the game score is directly off limits. Go and fight for your victory instead");
						break;
					}
					
					if (args.length > valueArg)
					{
						try
						{
							score.setScore(Integer.parseInt(args[valueArg]));
							sendMessage(player, ChatColor.AQUA + "Your team's score has been updated to: " + ChatColor.LIGHT_PURPLE + args[valueArg]);
						}
						catch (NumberFormatException exception)
						{
							sendError(sender, "*Ahem* Usage: /df score set <integer>");
						}
					}
					else
					{
						sendError(sender, "Usage: /df score set <value>");
					}
					break;
					
				case "query": case "get":
					
					sendMessage(sender, ChatColor.AQUA + "Your team's score is: " + ChatColor.LIGHT_PURPLE + score.getScore());
					break;
					
				case "reset":
					
					//Validate permission
					if (player != null && !player.hasPermission("dogfight.score.set"))
					{
						sendError(sender, "Sorry, setting the game score is directly off limits. Go and fight for your victory instead");
						break;
					}
					
					score.resetScore();
					sendMessage(sender, ChatColor.AQUA + "Your team's score has been reset to: " + ChatColor.LIGHT_PURPLE + "0");
					break;
				
				case "show":
					
					//Validate permission
					if (player != null && !player.hasPermission("dogfight.score.show"))
					{
						sendError(sender, "Sorry, the mysteries on what the game score is shall stay a mystery");
						break;
					}
					
					Dogfight.instance.getGame(player).getScoreRegistry().getScoreboard().showScoreboard(player);
					sendMessage(sender, ChatColor.AQUA + "Scoreboard is now on display!");
					break;
				
				case "hide":
					
					//Validate permission
					if (player != null && !player.hasPermission("dogfight.score.hide"))
					{
						sendError(sender, "Sorry, you must face the reality and look at the game score");
						break;
					}
					
					Dogfight.instance.getGame(player).getScoreRegistry().getScoreboard().hideScoreboard(player);
					sendMessage(sender, ChatColor.AQUA + "Scoreboard is now hidden from the eye");
					break;
					
				default:
					sendError(sender, "Subcommand '" + args[subCommandArg] + "' not recognized. Try scoring with one of these options: set, query, reset, show, hide");
					break;
				}
			}
			else
			{
				//TODO: Handle null score registry or null team
				sendError(sender, "Somehow either your team or your score doesn't exist. We're working on why...");
				return true;
			}
		}
		else
		{
			sendError(sender, "Usage: /df score <set|query|reset|show|hide> [value]");
		}
		
		return true;
	}
	
	private boolean logAlertCommand(CommandSender sender, String[] args)
	{
		final int showArg = 0;
		final int levelArg = 1;
		
		if (sender instanceof Player && !((Player) sender).hasPermission("dogfight.alert.log"))
		{
			((Player) sender).sendMessage(ChatColor.RED + "Sorry, you don't get to see the inside scoops of how this game works");
			return false;
		}
		
		if (args.length > showArg)
		{
			ShowAlertType showAlert = ShowAlertType.find(args[showArg]);
			
			if (showAlert != null)
			{
				boolean needAlertLevel = true;
				//Set the alert type
				Dogfight.instance.showAlertType = showAlert;
				
				//Check if more details required
				switch (showAlert)
				{
				case ALL: 
					needAlertLevel = false;
					sendMessage(sender, "All alert messages will be logged! Knowledge is power");
					break;
				case OFF:
					needAlertLevel = false;
					sendMessage(sender, "Alert logging has been disabled. Ignorance is bliss");
					break;
					
				default:
					//Intentional blank
					break;
				}
				
				if (needAlertLevel)
				{
					if (args.length > levelArg)
					{

						//Find alert level
						AlertLevel level = AlertLevel.find(args[levelArg]);
						
						if (level != null)
						{
							//Set the alert level
							Dogfight.instance.messageAlertLevel = level;
							
							//Message syntax
							switch (showAlert)
							{
							case GREATER: case LESSER:
								sendMessage(sender, "Alert logging set to " + showAlert.getDisplayName() + " than or equal to " + level.getDisplayName());
								break;
								
							case ONLY:
								sendMessage(sender, "Alert logging set to show " + showAlert.getDisplayName() + " " + level.getDisplayName());
								break;
								
							default:
								//Should never be hit
								break;
							}
						}
						else
						{
							//Incorrect syntax
							sendError(sender, "Alert! '" + args[levelArg] + "' is not a recognized alert level", "Try 'info', 'warning', 'severe', or 'fatal'");
							return false;
						}
					}
					else
					{
						//Incorrect syntax
						sendError(sender, "Usage: /df logalert <show type> [alert level]");
						return false;
					}
				} //End of needLevel. No else case required
			}
			else
			{
				//Incorrect syntax
				sendError(sender, "Alert! '" + args[showArg] + "' is not a recognized show alert type.", "Try 'off', 'greater', 'lesser', 'only' or 'all'");
				return false;
			}
		}
		else
		{
			//Incorrect syntax
			
			sendError(sender, "Usage: /df logalert <show type> [alert level]");
			return false;
		}
		
		return true; //TODO Change out return value
	}
	
	private void joinTeamCommand(Player player, String teamName)
	{
		if (player != null && !player.hasPermission("dogfight.team.join"))
		{
			sendError(player, "Sorry, we're excited you want to be part of a team, but you can't use this");
			return;
		}
		
		//TODO: Refine this assumption with error checking
		Game game = Dogfight.instance.getGame(player);
		PlayerRegistry playerRegistry = null;
		
		//TODO: Demand game input as parameter
		if (game == null)
		{
			game = Dogfight.instance.testGame;
		}
		
		playerRegistry = game.getPlayerRegistry();
		Team team = playerRegistry.getTeam(teamName);
		
		if (team != null)
		{
			//Use playerRegistry's method of adding player to team, more comprehensive than newTeam.addMember(player);
			playerRegistry.setPlayerToTeam(player, team);
			sendMessage(player, ChatColor.AQUA + "Welcome to the " + ChatColor.RESET + team.getDisplayName() + ChatColor.AQUA + " team");
		}
		else
		{
			sendError(player, "Sorry! We couldn't find any team named '" + teamName + "'. Maybe check your spelling and try again");
		}
	}
	
	private void nameTeamCommand(CommandSender sender, String[] args, boolean createTeam)
	{	
		//TODO: Clean spaghetti code concerning createTeam. Centralize all "createTeam" references to one point)
		
		final int teamNameArg = 0;
		final int dispNameArg = 1;
		
		Player player;
		
		if (sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			sendError(sender, "Sorry, we're working on making this compatible for non-players");
			return;
		}
		
		Game game = Dogfight.instance.getGame(player);
		PlayerRegistry playerRegistry = null;
		Team team = null;
		
		//TODO: Edit this segment. Require game as an input instead of test game
		if (game == null)
		{
			game = Dogfight.instance.testGame;
		}
		playerRegistry = game.getPlayerRegistry();
		team = playerRegistry.getTeam(player);
		
		if (args.length > teamNameArg)
		{
			//Attempting to create team
			if (createTeam)
			{
				if (player.hasPermission("dogfight.team.create"))
				{
					team = new Team(game);
				}
				else
				{
					sendError(sender, "Sorry, you can't kickstart your own team right now");
					return;
				}
			}
			//Not creating team, check if team is already given
			else if (team != null)
			{
				if (!player.hasPermission("dogfight.team.name"))
				{
					sendError(sender, "Sorry, you can't rename your team. Allowing you to do so might be rather...chaotic");
				}
			}
			else
			{
				sendError(sender, "Sorry, it's hard to get the name of something that doesn't exist");
				return;
			}
			
			//Attempt to set the name
			if (team.setName(args[teamNameArg], playerRegistry))
			{
				if (createTeam)
				{
					sendMessage(sender, ChatColor.AQUA + "You have created team: " + ChatColor.WHITE + team.getName());
				}
				else
				{
					sendMessage(sender, ChatColor.AQUA + "Your team name is now: " + ChatColor.WHITE + team.getName());
				}
				
				if (args.length > dispNameArg)
				{
					//Attempt to set the display name
					if (team.setDisplayName(args[dispNameArg], playerRegistry))
					{
						sendMessage(sender, false, ChatColor.DARK_AQUA + "The team's display name is: " + ChatColor.RESET + team.getDisplayName());	
					}
					else
					{
						sendError(sender, "Sorry, setting your display name failed because it is already taken by another team. Meanies");
						return;
					}
				}
				
				//Register team only after successfully setting the name
				//Wait for display name to be attempted to be set to accurately portray the display name
				if (createTeam)
				{
					playerRegistry.addTeam(team);
				}
			}
			else
			{
				sendError(sender, "Sorry, setting your name failed because it is already taken by another team. Try being original!");
				
				//Destroy the team
				if (createTeam)
				{
					sendError(sender, "Your team creation process has been aborted");
					team = null;
				}
				
				return;
			}
		}
		else
		{
			if (team != null && !createTeam)
			{
				sendMessage(sender, ChatColor.AQUA + "Your team's name is: " + team.getName(),
						ChatColor.AQUA + "The display name is: " + team.getDisplayName(),
						ChatColor.RED + "To rename, use: /df team name <new name> [display name]");
			}
			else
			{
				sendError(sender, "If you're trying to create a team: /df team create <team name> [display name]",
						"Otherwise, you need to be on a team to run: /df team name <team name> [display name]");
			}
		}
	}
	
	/**
	 * Get the 2nd (index of 1) args and all args following that one
	 * @param args
	 * @return
	 *
	 * @author R Lee
	 */
	public static String[] cmdArgs(String[] args)
	{
		//For Arrays.copyOfRange:
		//1st parameter: Original array list
		//2nd parameter: From index (inclusive)
		//3rd parameter: To index (exclusive)
		return Arrays.copyOfRange(args, 1, args.length);
	}
}
