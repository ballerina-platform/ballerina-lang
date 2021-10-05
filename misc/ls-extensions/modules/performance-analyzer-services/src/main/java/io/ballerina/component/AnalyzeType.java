package io.ballerina.component;

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
