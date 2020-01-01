package io.github.alpharlee.dogfight.exceptions;

/**
 * Created by kite7742 on 5/18/2017.
 */
public class WorldException extends Exception {

    private String worldName;
    private WorldExceptionReason reason;

    public WorldException(String worldName, WorldExceptionReason reason) {
        this.worldName = worldName;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return "World Exception in world \'" + worldName + "\". Reason: " + reason.reasonMsg;
    }

    public enum WorldExceptionReason {
        NAME_IN_USE("Name in use");

        String reasonMsg;

        WorldExceptionReason(String reasonMsg) {
            this.reasonMsg = reasonMsg;
        }
    }
}

