package dev.anoopkrishnarao.bolted.lock;

import java.util.UUID;

public class LockEntry {

    public LockState state;
    public String ownerUuid; // stored as String for JSON serialization

    public LockEntry(LockState state, UUID ownerUuid) {
        this.state = state;
        this.ownerUuid = ownerUuid != null ? ownerUuid.toString() : null;
    }

    public UUID getOwnerUuid() {
        return ownerUuid != null ? UUID.fromString(ownerUuid) : null;
    }

    public boolean isOwner(UUID playerUuid) {
        if (ownerUuid == null || playerUuid == null) return false;
        return playerUuid.equals(getOwnerUuid());
    }
}