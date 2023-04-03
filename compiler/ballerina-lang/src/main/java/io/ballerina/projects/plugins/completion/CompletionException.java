package io.ballerina.projects.plugins.completion;

/**
 * A runtime exception thrown to capture exceptions captured while executing compiler plugins.
 *
 * @since 2.0.0
 */
public class CompletionException extends RuntimeException {
    private String providerName;
    public CompletionException(Throwable cause, String providerName) {
        super(cause);
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}
