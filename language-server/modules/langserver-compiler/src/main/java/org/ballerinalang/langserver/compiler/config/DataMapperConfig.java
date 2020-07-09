package org.ballerinalang.langserver.compiler.config;

/**
 * Ballerina CodeLens Configuration.
 */
public class DataMapperConfig {
    private final boolean enabled;
    private final String url;

    DataMapperConfig() {
        this.enabled = false;
        this.url = "";
    }

    public boolean isEnabled() {

        return enabled;
    }

    public String getUrl() {

        return url;
    }
}