package org.ballerinalang.testerina.coverage;

public class LCovBRDA {

    private int lineNumber;

    private int blockNumber;

    private int branchNumber;

    private String taken;

    public LCovBRDA(int lineNumber, int blockNumber, int branchNumber, String taken) {
        this.lineNumber = lineNumber;
        this.blockNumber = blockNumber;
        this.branchNumber = branchNumber;
        this.taken = taken;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getBranchNumber() {
        return branchNumber;
    }

    public void setBranchNumber(int branchNumber) {
        this.branchNumber = branchNumber;
    }

    public String getTaken() {
        return taken;
    }

    public void setTaken(String taken) {
        this.taken = taken;
    }
}
