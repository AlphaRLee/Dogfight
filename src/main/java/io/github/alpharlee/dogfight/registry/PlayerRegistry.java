package io.github.alpharlee.dogfight.registry;

import io.github.alpharlee.dogfight.commandhandler.CommandHandler;
import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.score.Score;
import io.github.alpharlee.dogfight.score.Scorestreak;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerRegistry extends Registry {
    private Map<Player, Team> players; //Indexes players and the team that they are on
    private Set<Team> teams; //Index of teams

    private Set<Player> playersToUpdate;

    private boolean isTeamGame = false; //Denotes whether or not the game has teams or single players only

    public PlayerRegistry(Game game) {
        super(game);
        players = new HashMap<Player, Team>();
        teams = new HashSet<Team>();

        playersToUpdate = new HashSet<Player>();
    }

    /**
     * Get the players and their associated teams
     *
     * @return
     * @author R Lee
     */
    public Map<Player, Team> getPlayers() {
        return players;
    }

    public Set<Team> getTeams() {
        return this.teams;
    }

    public boolean addPlayer(Player player) {
        //TODO: Add joining to random team instead of creating a team
        return addPlayer(player, null);
    }

    /**
     * Returns whether or not the player is in the player registry
     * @param player
     * @return
     */
    public boolean hasPlayer(Player player) {
        return players.containsKey(player);
    }

    /**
     * Add a player to the registry and add them to the specified team, or creates a new team for the player if set to null
     * If the player is already added, the function does nothing and returns false
     *
     * @param player
     * @param team   Team to assign player to. If null, creates a new team and assigns player to it
     * @author R Lee
     */
    public boolean addPlayer(Player player, Team team) {
        //Check player is not already added
        if (!getPlayers().containsKey(player)) {
            //TODO: Enable adding player to a team directly instead of setting them to a new team
            if (team == null) {
                team = new Team(getGame()); //Instantiate a new team just for this player
            }

            setPlayerToTeam(player, team); //Assign player to a new team. Method will handle adding team

            //Default behavior for single player games
            if (!isTeamGame()) {
                team.setName(player.getName());
            }

            ScoreRegistry scoreRegistry = getGame().getScoreRegistry();
            scoreRegistry.setScorestreak(player, new Scorestreak(getGame(), player));

            return true;
        }

        return false;
    }

    /**
     * Remove a player from the registry, and remove them from their team
     *
     * @param player
     * @author R Lee
     */
    public void removePlayer(Player player) {
        if (getTeam(player) != null) {
            getTeam(player).removeMember(player);
        }

        getPlayers().remove(player);
        playersToUpdate.remove(player);
    }

    /**
     * Set a player's team to the specified team. If the player is not in this registry, then add the player
     * Does nothing if player is already on the same team
     *
     * @param player
     * @param team
     * @author R Lee
     */
    public void setPlayerToTeam(Player player, Team team) {
        Team currentTeam = getTeam(player);
        if (currentTeam != null && !team.equals(currentTeam)) {
            currentTeam.removeMember(player); //Remove player from previous team
        }

        addTeam(team); //Attempt to add team. If team already exists, fail gracefully and silently
        getPlayers().put(player, team);

        if (team != null) {
            team.addMember(player); //Attempt to add player to team. If player already on team, fail gracefully and silently
        }

        //TODO: Rename this method because of following line?
        // FIXME: This fails single responsibility. This isn't the job of the setPlayerToTeam
        //Ensure they are registered to game as well
        Dogfight.instance.registerGameToPlayer(player, getGame());

    }

    /**
     * Add a team to this registry
     *
     * @param team Team to add. If null, does nothing
     * @author R Lee
     */
    public boolean addTeam(Team team) {
        if (team == null) {
            return false;
        }

        if (getTeams().add(team)) {
            //Register the team and score
            getGame().getScoreRegistry().setScore(team, new Score(getGame(), team));

            return true;
        }

        return false;
    }

    //TODO: Fill me in
    public boolean removeTeam(Team team) {
        /*
         * TODO: Answer the following:
         * What happens to players on the team
         * What happens to team's registered score
         */

        if (getTeams().remove(team)) {
            //TODO: Formalize what happens to players on team

            ScoreRegistry scoreRegistry = getGame().getScoreRegistry();

            //Kick all players from game
            for (Player member : team.getMembers()) {
                scoreRegistry.removeScorestreak(member);
                removePlayer(member);

                CommandHandler.sendMessage(member, ChatColor.RED + "You have been kicked from the game because your team no longer exists");
            }

            scoreRegistry.removeScore(team);
            return true;
        }

        return false;
    }

    /**
     * Return the team that this player is assigned to
     *
     * @param player
     * @return
     * @author R Lee
     */
    public Team getTeam(Player player) {
        return getPlayers().get(player);
    }

    /**
     * Get a team based on its name or display name
     *
     * @param teamName
     * @return Team according to team name, or null if not found
     * @author R Lee
     */
    public Team getTeam(String teamName) {
        //Iterate through all teams and search for a match
        for (Team team : getGame().getPlayerRegistry().getTeams()) {
            if (team.getName().equals(teamName) || team.getRawDisplayName().equalsIgnoreCase(teamName)) {
                return team;
            }
        }

        return null;
    }

    /**
     * Mark player to update their inventory next tick
     *
     * @param player
     * @author R Lee
     */
    public void updatePlayer(Player player) {
        playersToUpdate.add(player);
    }

    /**
     * Update player's inventory
     *
     * @author R Lee
     */
    public void updatePlayers() {
        for (Player player : playersToUpdate) {
            player.updateInventory();
        }

        playersToUpdate.clear();
    }

    /**
     * Clear all players from this registry
     *
     * @param clearTeams If enabled, clear all teams tracked by this registry
     * @author R Lee
     */
    public void clearPlayers(boolean clearTeams) {
        getPlayers().clear();

        if (clearTeams) {
            getTeams().clear();
        }
    }

    /**
     * Denotes whether or not the game has multiple members on a team.
     * Defaulted to false
     *
     * @return
     * @author R Lee
     */
    public boolean isTeamGame() {
        return isTeamGame;
    }

    /**
     * Set if the game has multiple members on a team
     *
     * @param value
     * @author R Lee
     */
    public void setTeamGame(boolean value) {
        isTeamGame = value;
    }
}
