package io.ballerina.component;

/**
 * Analyze type of performance forecaster.
 *
 * @since 2.0.0
 */
public enum AnalyzeType {
    ADVANCED("advanced"),
    REALTIME("realtime");

    private String analyzeType;
    AnalyzeType(String analyzeType) {
        this.analyzeType = analyzeType;
    }

    public String getAnalyzeType() {

        return analyzeType;
    }
}
