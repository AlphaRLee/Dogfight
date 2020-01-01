package io.github.alpharlee.dogfight.score;

import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.registry.ScoreRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DfScoreboard {
    private ScoreRegistry scoreRegistry;

    private Scoreboard scoreboard;
    private Objective objective;
    private Map<Team, org.bukkit.scoreboard.Score> teamScores;
    private ArrayList<org.bukkit.scoreboard.Score> bukkitScores; //List of all bukkit scores

    private static final String objectiveName = "df.mainScore"; //Minecraft complains if the name is longer than 16 characters

    public DfScoreboard(ScoreRegistry scoreRegistry) {
        setScoreRegistry(scoreRegistry);
        setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        setObjective(getScoreboard().registerNewObjective(objectiveName, "dummy")); //Register a dummy objective named "dogfight"

        getObjective().setDisplayName(ChatColor.GOLD + "" + ChatColor.ITALIC + "Dogfight");
        getObjective().setDisplaySlot(DisplaySlot.SIDEBAR);

        setTeamScores(new HashMap<Team, org.bukkit.scoreboard.Score>());
        setBukkitScores(new ArrayList<org.bukkit.scoreboard.Score>());
    }

    /**
     * @return the game
     */
    public ScoreRegistry getScoreRegistry() {
        return scoreRegistry;
    }

    /**
     * @param game the game to set
     */
    public void setScoreRegistry(ScoreRegistry scoreRegistry) {
        this.scoreRegistry = scoreRegistry;
    }

    /**
     * Get the bukkit-provided scoreboard
     *
     * @return
     * @author R Lee
     */
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    /**
     * Set the bukkit scoreboard
     *
     * @param scoreboard
     * @author R Lee
     */
    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    /**
     * @return the objective
     */
    public Objective getObjective() {
        return objective;
    }

    /**
     * @param objective the objective to set
     */
    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    /**
     * Get the bukkit scores (not the same as Dogfight scores) associating to the Dogfight teams (not the same as bukkit teams)
     *
     * @return
     * @author R Lee
     */
    public Map<Team, org.bukkit.scoreboard.Score> getTeamScores() {
        return teamScores;
    }

    /**
     * Set the bukkit scores (not the same as Dogfight scores) associating to the Dogfight teams (not the same as bukkit teams)
     *
     * @return
     * @author R Lee
     */
    public void setTeamScores(Map<Team, org.bukkit.scoreboard.Score> teamScores) {
        this.teamScores = teamScores;
    }

    public ArrayList<org.bukkit.scoreboard.Score> getBukkitScores() {
        return bukkitScores;
    }

    public void setBukkitScores(ArrayList<org.bukkit.scoreboard.Score> bukkitScores) {
        this.bukkitScores = bukkitScores;
    }

    public org.bukkit.scoreboard.Score getBukkitScore(Team team) {
        return getTeamScores().get(team);
    }

    /**
     * Set the bukkit score for the specified team.
     * <br/><br/>
     * If the team has a bukkit score entry already set, bukkitScore parameter's score is set to match that of the original entry.
     * If the team has no bukkit score entry set, {@link DfScoreboard#addBukkitScore(org.bukkit.scoreboard.Score)} is invoked
     * (Does not invoke {@link DfScoreboard#addBukkitScore(Team, org.bukkit.scoreboard.Score)})
     *
     * @param team        Team to set the score of
     * @param bukkitScore Bukkit score to set. Name is set but integer score value is attempted to match the original entry
     * @author R Lee
     */
    public void setBukkitScore(Team team, org.bukkit.scoreboard.Score bukkitScore) {
        org.bukkit.scoreboard.Score currentScore = getBukkitScore(team);
        int index = -1; //Set to the default value of "no value found"

        if (currentScore != null) {
            index = getBukkitScores().indexOf(currentScore); //Get the current index of the score. Must happen before map is changed
            bukkitScore.setScore(currentScore.getScore());
        }

        getTeamScores().put(team, bukkitScore);

        //If index is found
        if (index != -1) {
            getBukkitScores().set(index, bukkitScore);
        } else {
            addBukkitScore(bukkitScore); //Set to last entry and iterate all other entries
        }
    }

    /**
     * Add this bukkit score to the bottom of the list (score of 0)
     *
     * @param bukkitScore
     * @author R Lee
     */
    public void addBukkitScore(org.bukkit.scoreboard.Score bukkitScore) {
        //Increment all the scores by one
        for (org.bukkit.scoreboard.Score score : getBukkitScores()) {
            //Prevent self-iteration
            if (!score.equals(bukkitScore)) {
                score.setScore(score.getScore() + 1);
            }
        }

        //Add the score
        bukkitScore.setScore(0);
        getBukkitScores().add(bukkitScore);
    }

    /**
     * Add this bukkit score to the bottom of the list (score of 0), and register the score to the associated team
     *
     * @param bukkitScore
     * @author R Lee
     */
    public void addBukkitScore(Team team, org.bukkit.scoreboard.Score bukkitScore) {
        //addBukkitScore(bukkitScore);
        setBukkitScore(team, bukkitScore);
    }

    /**
     * Remove the bukkkit score for this team from the scoreboard, and decrement all scoreboard entries above it
     * If no score is associated to this team, do nothing
     *
     * @param team
     * @author R Lee
     */
    public void removeBukkitScore(Team team) {
        removeBukkitScore(getBukkitScore(team));
        getTeamScores().remove(team);
    }

    /**
     * Remove this bukkit score from the scoreboard, and decrement all scoreboard entries above it
     * If score is null, do nothing
     *
     * @param bukkitScore
     * @author R Lee
     */
    public void removeBukkitScore(org.bukkit.scoreboard.Score bukkitScore) {
        if (bukkitScore == null) {
            return;
        }

        org.bukkit.scoreboard.Score score = null;

        //Decrement the index score of all entries before this one
        for (int i = 0; i < getBukkitScores().indexOf(bukkitScore); i++) {
            score = getBukkitScores().get(i);
            score.setScore(score.getScore() - 1);
        }

        getScoreboard().resetScores(bukkitScore.getEntry()); //Remove entry from scoreboard
        getBukkitScores().remove(bukkitScore);
    }

    /**
     * Show this team's score on the scoreboard
     *
     * @param team Dogfight team. Must be registered under the same game as this DfScoreboard
     * @author R Lee
     */
    public void updateScore(Team team) {
        //Do not do anything if team is not part of this game
        if (!getScoreRegistry().getGame().getPlayerRegistry().getTeams().contains(team)) {
            return;
        }

        //Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "{DfScoreboard} updateScore started!");

        //Get the score (number) associated to the team
        int gameScoreNumber = 0;

        //Get dogfight score (not bukkit score)
        Score gameScore = getScoreRegistry().getScore(team);
        //Null check required, especiallly for instantiation of a dogfight score or a dogfight team
        if (gameScore != null) {
            gameScoreNumber = gameScore.getScore();
        }

        //Index number refers to indexed value where 0 is top score value
        //If no index number is found, just set it at the next spot (hence no -1)
        int indexNumber = getBukkitScores().size();
        int indexDisplayNumber = 0; //Index display number refers to indexed value where 0 is bottom score value
        String scoreText = team.getDisplayName() + ChatColor.RESET + ": " + ChatColor.GREEN + gameScoreNumber;

        org.bukkit.scoreboard.Score bukkitScore = getBukkitScore(team);

        //If score is already associated to this team, get its index number
        if (bukkitScore != null) {
            indexNumber = getBukkitScores().indexOf(bukkitScore);
            indexDisplayNumber = bukkitScore.getScore();

            getScoreboard().resetScores(bukkitScore.getEntry()); //Remove entry from scoreboard

            bukkitScore = getObjective().getScore(scoreText); //Re-assign bukkitScore to updated scoreText (including the game score)
            bukkitScore.setScore(indexDisplayNumber);
        } else {
            //Score does not exist for this team yet, add one in
            bukkitScore = getObjective().getScore(scoreText);
            bukkitScore.setScore(0);
        }

        setBukkitScore(team, bukkitScore); //Replace the mapped entry for the team
    }

    /**
     * Show this DfScoreboard to this player
     *
     * @param team
     * @author R Lee
     */
    public void showScoreboard(Player player) {
        player.setScoreboard(getScoreboard());
    }

    /**
     * Show this DfScoreboard to all members of the team
     *
     * @param team
     * @author R Lee
     */
    public void showScoreboard(Team team) {
        for (Player member : team.getMembers()) {
            showScoreboard(member);
        }
    }

    /**
     * Hide the DfScoreboard and restore the server's main scoreboard for this player
     *
     * @param player
     * @author R Lee
     */
    public void hideScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); //Returns scoreboard controlled by server (not by plugin)
    }

    /**
     * Hide the DfScoreboard and restore the server's main scoreboard for all members on the team
     *
     * @param team
     * @author R Lee
     */
    public void hideScoreboard(Team team) {
        for (Player member : team.getMembers()) {
            hideScoreboard(member);
        }
    }
}
