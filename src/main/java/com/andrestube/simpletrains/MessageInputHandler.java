package com.andrestube.simpletrains;

import java.util.HashMap;
import java.util.UUID;

public class MessageInputHandler {
    
    // Key: Player UUID | Value: Target Data (Station Name or "LINK_" + Station Name)
    private final HashMap<UUID, String> waitingForInput;

    public MessageInputHandler() {
        this.waitingForInput = new HashMap<>();
    }

    /** Starts listening for the player's next chat message. */
    public void startListening(UUID playerId, String targetData) {
        waitingForInput.put(playerId, targetData);
    }

    /** Stops listening for the player's chat message. */
    public void stopListening(UUID playerId) {
        waitingForInput.remove(playerId);
    }

    /** Checks if the player is currently waiting for chat input. */
    public boolean isWaitingForInput(UUID playerId) {
        return waitingForInput.containsKey(playerId);
    }

    /** Gets the target data (station name or link identifier). */
    public String getTargetStation(UUID playerId) {
        return waitingForInput.get(playerId);
    }
}