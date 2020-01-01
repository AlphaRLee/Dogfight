package io.github.alpharlee.dogfight.score;

import io.github.alpharlee.dogfight.Dogfight;
import io.github.alpharlee.dogfight.game.Game;
import io.github.alpharlee.dogfight.game.Team;
import org.bukkit.configuration.file.YamlConfiguration;

public class Score extends ScoreKeeper {
    private Team team; //Team that this score is keeping index for

    public int killIncrement;
    public int deathIncrement; //Death increment is a negative number in config file
    public int maxScore;

    public static int defaultKillIncrement = 10;
    public static int defaultDeathIncrement = -5;
    public static int defaultMaxScore = 50; //Max score before game ends

    public Score(Game game, Team team) {
        super(game);
        setup(team);
    }

    private void setup(Team team) {
        setTeam(team);

        setKillIncrement(defaultKillIncrement);
        setDeathIncrement(defaultDeathIncrement);
        setMaxScore(defaultMaxScore);

        setScore(0); //Set the score and add the scoreboard initially
    }

    /**
     * Get values from scorestreak.yml and store them.
     * Must be called after scorestreak.yml is loaded
     *
     * @author R Lee
     */
    public static void setupDefaultValues() {
        YamlConfiguration scoreData = Dogfight.instance.getScoreData();

        defaultKillIncrement = scoreData.getInt("increment.kill", 10);
        defaultDeathIncrement = scoreData.getInt("increment.death", -5);
        defaultMaxScore = scoreData.getInt("max", 50);
    }

    /**
     * @return the team this score keeps record of
     */
    public Team getTeam() {
        return team;
    }

    /**
     * @param team the team to set
     */
    public void setTeam(Team team) {
        this.team = team;
    }

    public int getMaxScore() {
        return this.maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getKillIncrement() {
        return this.killIncrement;
    }

    /**
     * Set the amount players scores increment by when they make a kill
     *
     * @param value
     * @author R Lee
     */
    public void setKillIncrement(int value) {
        this.killIncrement = value;
    }

    /**
     * Amount player's score increments by when they die from an environmental factor.
     * Default to a negative number
     *
     * @return Death increment
     * @author R Lee
     */
    public int getDeathIncrement() {
        return this.deathIncrement;
    }

    /**
     * Set the amount players scores increment by when they die from an environmental factor
     *
     * @param value Value to set death increment to. Recommended to be a negative number
     * @author R Lee
     */
    public void setDeathIncrement(int value) {
        this.deathIncrement = value;
    }

    /**
     * Set the score and update it on the scoreboard
     * If the score is greater than the max score, trigger the game win function
     */
    @Override
    public void setScore(int score) {
        super.setScore(score);
        getGame().getScoreRegistry().getScoreboard().updateScore(getTeam()); //Update scoreboard with new score

        if (getScore() >= getMaxScore()) {
            getGame().win(getTeam());
        }
    }
}
