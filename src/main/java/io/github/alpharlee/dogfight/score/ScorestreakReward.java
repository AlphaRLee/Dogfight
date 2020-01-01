package io.github.alpharlee.dogfight.score;

import org.bukkit.entity.Player;

public abstract class ScorestreakReward {
    private Player player;

    private int requiredScore; //Required score in scorestreak to access reward
    private boolean grantedReward = false; //Status on if reward was granted or not already

    private ScorestreakReward nextReward = null;
    private ScorestreakReward lastReward = null;

    public ScorestreakReward(Player player, int requiredScore) {
        setPlayer(player);
        setRequiredScore(requiredScore);
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getRequiredScore() {
        return this.requiredScore;
    }

    /**
     * Set the required score in order to obtain this reward.
     * Sets to 0 if inputted score is lesser than 0
     *
     * @param score
     * @author R Lee
     */
    public void setRequiredScore(int score) {
        this.requiredScore = Math.max(score, 0);
    }

    /**
     * @return the next scorestreak reward the player will achieve after this one
     */
    public ScorestreakReward getNextReward() {
        return nextReward;
    }

    /**
     * @param nextReward the next scorestreak reward to set that the player will achieve after this one
     */
    public void setNextReward(ScorestreakReward nextReward) {
        this.nextReward = nextReward;
    }

    /**
     * @return the last score streak reward the player has achieved prior to this one
     */
    public ScorestreakReward getLastReward() {
        return lastReward;
    }

    /**
     * @param lastReward the last scorestreak reward to set that the player has achieved prior to this one
     */
    public void setLastReward(ScorestreakReward lastReward) {
        this.lastReward = lastReward;
    }

    /**
     * Check whether or not this reward has been granted yet
     *
     * @return
     * @author R Lee
     */
    public boolean isGranted() {
        return grantedReward;
    }

    /**
     * Set whether or not this reward has been granted to the specified state
     *
     * @param state
     * @author R Lee
     */
    public void setGranted(boolean state) {
        this.grantedReward = state;
    }

    /**
     * Check to see if the scorestreak reward can be granted, and if so, executes {@link ScorestreakReward#grant()}
     * Requires current score to be greater than the required score and for this scorestreak to not be already granted
     *
     * @param currentScore Current score that this player is at
     * @return True if reward is granted, false if not
     * @author R Lee
     */
    public boolean checkGrant(int currentScore) {
        //Check current score is greater or equal to required score and this scorestreak is not already granted
        if (currentScore >= getRequiredScore() && !isGranted()) {
            grant();
            return true;
        }

        return false;
    }

    /**
     * Grant the player the reward given by {@link ScorestreakReward#onGrant()}
     *
     * @author R Lee
     */
    public void grant() {
        onGrant();
        setGranted(true);
    }

    /**
     * Action to perform once reward has been granted
     *
     * @author R Lee
     */
    protected abstract void onGrant();
}
