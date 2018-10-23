package org.ballerinalang.bre.coverage;

public class LCovFNDA {

    private int executionCount;

    private String functionName;

    public LCovFNDA(int executionCount, String functionName) {
        this.executionCount = executionCount;
        this.functionName = functionName;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
