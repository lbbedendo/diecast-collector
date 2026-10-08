package com.diecastcollector.api.enums;

/**
 * Physical state of the diecast itself, independent of its {@link ModelPackaging} (see
 * CONTEXT.md). Kept generic on purpose so it applies across brands instead of encoding any one
 * brand's own grading scale.
 */
public enum ModelCondition {
    MINT,
    GOOD,
    FAIR,
    POOR
}
