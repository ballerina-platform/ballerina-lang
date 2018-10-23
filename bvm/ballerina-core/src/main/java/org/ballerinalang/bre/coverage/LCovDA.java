package org.ballerinalang.bre.coverage;

public class LCovDA {

    private int lineNumber;

    private int lineExecutionCount;

    private int checksum;

    public LCovDA(int lineNumber, int lineExecutionCount, int checksum) {
        this.lineNumber = lineNumber;
        this.lineExecutionCount = lineExecutionCount;
        this.checksum = checksum;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineExecutionCount() {
        return lineExecutionCount;
    }

    public void setLineExecutionCount(int lineExecutionCount) {
        this.lineExecutionCount = lineExecutionCount;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }
}
