package com.diecastcollector.api.enums;

/**
 * Physical condition of a piece. Kept generic on purpose so it applies across brands
 * (Hot Wheels, Matchbox, California Collectibles, ...) instead of encoding any one
 * brand's own grading scale.
 */
public enum ModelCondition {
    SEALED,
    MINT,
    GOOD,
    FAIR,
    POOR,
    LOOSE
}
