package io.github.alpharlee.dogfight.game;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.registry.PlayerRegistry;
import io.github.alpharlee.dogfight.score.DfScoreboard;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Team {
    private Game game;
    private Set<Player> members;

    private String name;
    private String displayName;

    private Color color;

    private boolean friendlyFire = false; //Denotes whether or not friendly fire is enabled

    public Team(Game game) {
        setGame(game);
        setMembers(new HashSet<Player>());
        setName("");
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Player> getMembers() {
        return this.members;
    }

    public void setMembers(HashSet<Player> members) {
        this.members = members;
    }

    /**
     * Attempt to add a member to this team
     * If the player is not a member of the team yet, show the scoreboard to them
     *
     * @param player
     * @return True if player was added, false if player already added
     * @author R Lee
     */
    public boolean addMember(Player player) {
        //Attempt to add the member
        if (getMembers().add(player)) {
            //Show member scoreboard
            getGame().getScoreRegistry().getScoreboard().showScoreboard(player);

            //If there's multiple players in the game, notify the player registry
            if (getMembers().size() > 1) {
                getGame().getPlayerRegistry().setTeamGame(true);
            }

            return true;
        }

        return false;
    }

    /**
     * Attempt to remove a member from this team
     * If the player is a member of the team, hide the scoreboard from them
     *
     * @param player
     * @return True if member was found and removed
     * @author R Lee
     */
    public boolean removeMember(Player player) {
        if (getMembers().remove(player)) {
            DfScoreboard scoreboard = getGame().getScoreRegistry().getScoreboard();

            //Hide scoreboard from member
            scoreboard.hideScoreboard(player);

            if (getMembers().isEmpty() || getMembers().size() <= 0) {
                scoreboard.removeBukkitScore(this); //Remove the scoreboard entry relating to this team

                //TODO: Remove testing device. Empty teams should be able to exist as standalone elements if the game is multiplayer
                getGame().getPlayerRegistry().removeTeam(this);
            }

            return true;
        }

        return false;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Set the name of this team. Does not support color codes
     * Name must be unique to the game
     * If display name is not set, set the display name to that of this team
     *
     * @param name Name for team. Must be unique
     * @return True if name changed successfully, false if not
     * @author R Lee
     */
    public boolean setName(String name) {
        return setName(name, getGame().getPlayerRegistry());
    }

    /**
     * Set the name of this team. Does not support color codes
     * Name must be unique to the inputted player registry
     * If display name is not set, set the display name to that of this team
     * Use for attempting to create a team without fully regisering it during the process
     *
     * @param name           Name for team. Must be unique
     * @param playerRegistry
     * @return True if name changed successfully, false if not
     * @author R Lee
     */
    public boolean setName(String name, PlayerRegistry playerRegistry) {
        for (Team team : playerRegistry.getTeams()) {
            if (!team.equals(this) && team.getName().equals(name)) {
                return false; //Non-unique name found, break
            }
        }

        this.name = name;

        if (getDisplayName() == null || getDisplayName().isEmpty()) {
            setDisplayName(name, playerRegistry);
        }

        return true;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Get the display name, without any color codes
     *
     * @return
     * @author R Lee
     */
    public String getRawDisplayName() {
        return ChatColor.stripColor(getDisplayName());
    }

    /**
     * Set the display name
     * Color codes are accepted, using '&' as the color character
     *
     * @param name Must be unique to the game (unique by color codes only is not allowed)
     * @return True if display name is successfully set
     * @author R Lee
     */
    public boolean setDisplayName(String name) {
        return setDisplayName(name, getGame().getPlayerRegistry());
    }

    /**
     * Set the display name
     * Color codes are accepted, using '&' as the color character
     * Use for attempting to create a team without fully regisering it during the process
     *
     * @param name           Must be unique to the inputted player registry (unique by color codes only is not allowed)
     * @param playerRegistry
     * @return True if display name is successfully set
     * @author R Lee
     */
    public boolean setDisplayName(String name, PlayerRegistry playerRegistry) {
        for (Team team : playerRegistry.getTeams()) {
            if (!team.equals(this) && team.getRawDisplayName().equals(ChatColor.stripColor(name))) {
                return false; //Non-unique name found, break
            }
        }

        this.displayName = ChatColor.translateAlternateColorCodes(Dogfight.ALT_COLOR_CHAR, name);

        //Update scoreboard with new name only if team is already registered
        if (getGame().getPlayerRegistry().getTeams().contains(this)) {
            getGame().getScoreRegistry().getScoreboard().updateScore(this);
        }

        return true;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Denotes whether or not friendly fire is enabled.
     * Defaults to false
     *
     * @return
     * @author R Lee
     */
    public boolean hasFriendlyFire() {
        return this.friendlyFire;
    }

    /**
     * Set friendly fire as enabled or not
     *
     * @param value
     * @author R Lee
     */
    public void setFriendlyFire(boolean value) {
        this.friendlyFire = value;
    }

    /**
     * Return whether or not the player in question is on this team
     *
     * @param player
     * @return
     * @author R Lee
     */
    public boolean containsMember(Player player) {
        return getMembers().contains(player)
                ;
    }
}
