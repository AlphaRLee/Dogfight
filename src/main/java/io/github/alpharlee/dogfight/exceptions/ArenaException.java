package io.github.alpharlee.dogfight.exceptions;

/**
 * Created by kite7742 on 5/19/2017.
 */
public class ArenaException extends Exception {

    private String arenaName;
    private ArenaExceptionReason reason;

    public ArenaException(String arenaName, ArenaExceptionReason reason) {
        this.arenaName = arenaName;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return "Arena Exception. Reason: " + reason.reason;
    }

    public enum ArenaExceptionReason {
        NOT_FOUND("404 arena not found!"),
        IN_USE("Arena is in use!"),
        NOT_IN_USE("Arena is not being used!");

        String reason;

        ArenaExceptionReason(String reason) {
            this.reason = reason;
        }
    }
}
