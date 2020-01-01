package io.github.alpharlee.dogfight.registry;

import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import io.github.alpharlee.dogfight.score.DfScoreboard;
import io.github.alpharlee.dogfight.score.Score;
import io.github.alpharlee.dogfight.score.Scorestreak;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ScoreRegistry extends Registry {
    private HashMap<Team, Score> scores;
    private HashMap<Player, Scorestreak> scorestreaks;

    private DfScoreboard scoreboard;

    public ScoreRegistry(Game game) {
        super(game);

        scores = new HashMap<Team, Score>();
        scorestreaks = new HashMap<Player, Scorestreak>();
        setScoreboard(new DfScoreboard(this));
    }

    /**
     * Get a map of all teams and their current score
     *
     * @return
     * @author R Lee
     */
    public HashMap<Team, Score> getScores() {
        return this.scores;
    }

    /**
     * Get the score associated to this team
     *
     * @param team
     * @return score or null if not found
     * @author R Lee
     */
    public Score getScore(Team team) {
        return getScores().get(team);
    }

    /**
     * Set the score of the team
     *
     * @param player
     * @param score
     * @author R Lee
     */
    public void setScore(Team team, Score score) {
        getScores().put(team, score);
    }

    /**
     * Remove the score entry given for this team
     *
     * @param team
     * @author R Lee
     */
    public void removeScore(Team team) {
        getScoreboard().removeBukkitScore(team);
        getScores().remove(team);
    }

    /**
     * Get a map of all players and their current scorestreak
     *
     * @return
     * @author R Lee
     */
    public HashMap<Player, Scorestreak> getScorestreaks() {
        return this.scorestreaks;
    }

    /**
     * Get the scorestreak associated to this player
     *
     * @param player
     * @return scorestreak or null if not found
     * @author R Lee
     */
    public Scorestreak getScorestreak(Player player) {
        return getScorestreaks().get(player);
    }

    /**
     * Set the scorestreak of the player
     *
     * @param player
     * @param scorestreak
     * @author R Lee
     */
    public void setScorestreak(Player player, Scorestreak scorestreak) {
        getScorestreaks().put(player, scorestreak);
    }

    /**
     * Remove the scorestreak entry for the given player
     *
     * @param player
     * @author R Lee
     */
    public void removeScorestreak(Player player) {
        getScorestreaks().remove(player);
    }

    public DfScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void setScoreboard(DfScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    /**
     * Reset all the scores being tracked
     * Does NOT reset scorestreaks
     *
     * @author R Lee
     */
    public void resetScores() {
        for (Score score : getScores().values()) {
            score.resetScore();
        }
    }

    /**
     * Reset all the scorestreaks being tracked
     *
     * @author R Lee
     */
    public void resetScorestreaks() {
        for (Scorestreak scorestreak : getScorestreaks().values()) {
            scorestreak.resetScore();
        }
    }

    /**
     * Clear all scores from this registry
     *
     * @param clearScorestreaks If enabled, clear all scorestreaks tracked by this registry
     * @author R Lee
     */
    public void clearScores(boolean clearScorestreaks) {
        getScores().clear();

        if (clearScorestreaks) {
            getScorestreaks().clear();
        }
    }
}
