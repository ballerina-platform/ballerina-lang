package com.github.gtache.lsp.requests;

/**
 * Enumeration for the timeouts.
 */
public enum Timeouts {
    CODEACTION(2000),
    CODELENS(2000),
    COMPLETION(1000),
    DEFINITION(2000),
    DOC_HIGHLIGHT(1000),
    EXECUTE_COMMAND(2000),
    FORMATTING(2000),
    HOVER(2000),
    INIT(10000),
    REFERENCES(2000),
    SIGNATURE(1000),
    SHUTDOWN(5000),
    SYMBOLS(2000),
    WILLSAVE(2000);

    private final int defaultTimeout;

    Timeouts(final int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }
}
