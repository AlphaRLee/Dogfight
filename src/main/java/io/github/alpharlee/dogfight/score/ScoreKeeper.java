package io.github.alpharlee.dogfight.score;

import io.github.alpharlee.dogfight.game.Game;

public abstract class ScoreKeeper {
    protected Game game;
    protected int score = 0;

    public ScoreKeeper(Game game) {
        setGame(game);
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void increment(int amount) {
        increment(amount, false);
    }

    /**
     * Increment the score by the specified amount
     *
     * @param amount  Amount to increment score by. Set to negative number to decrement score
     * @param minZero Minimum score of zero. If enabled, the score will be set to zero if it is less than zero
     * @author R Lee
     */
    public void increment(int amount, boolean minZero) {
        setScore(getScore() + amount);

        //If minZero is enabled, set the score to the current score or 0, whichever is greater
        if (minZero) {
            setScore(Math.max(getScore(), 0));
        }
    }

    /**
     * Reset the score to 0
     *
     * @author R Lee
     */
    public void resetScore() {
        setScore(0);
    }
}
