package dev.anoopkrishnarao.bolted.lock;

public enum LockState {
    UNLOCKED,
    ENTITY_LOCKED,
    FULLY_LOCKED;

    /**
     * Cycles to the next state: UNLOCKED → ENTITY_LOCKED → FULLY_LOCKED → UNLOCKED
     */
    public LockState next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}