package com.example.cocktailbar.model;

/**
 * Capability "can be prepared by the bartender".
 *
 * <p>It is an interface and not a method of Drink alone because the idea is not tied to drinks:
 * anything the bar prepares (a snack, later) could promise the same thing.
 */
public interface Preparable {

    /** Above this duration, a preparation is no longer considered "quick". */
    int QUICK_PREPARATION_MAX_MINUTES = 1;

    /** Time needed to prepare ONE unit, in minutes. */
    int getPreparationTimeMinutes();

    /**
     * Default method: every implementation gets this rule for free and cannot get it wrong,
     * because it only relies on the method above.
     */
    default boolean isQuickToPrepare() {
        return getPreparationTimeMinutes() <= QUICK_PREPARATION_MAX_MINUTES;
    }
}
