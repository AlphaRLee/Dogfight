package io.github.alpharlee.dogfight.registry;

import io.github.alpharlee.dogfight.game.Game;

public abstract class Registry {
    private Game game;

    public Registry(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    /*
     * TODO: Add
     * updateMembers()
     * removeMembers()
     * removeMarkedMembers()
     */
}
