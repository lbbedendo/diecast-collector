package com.diecastcollector.api.enums;

/** Whether a Model is still in its original packaging (see CONTEXT.md). */
public enum ModelPackaging {
    /** Never opened. */
    SEALED,
    /** Opened, but the packaging was kept. */
    OPENED,
    /** No packaging. */
    LOOSE
}
