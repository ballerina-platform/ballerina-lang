package org.ballerinalang.testerina.coverage;

public class LCovBRFHBlock {

    private int numOfBranchesFound;

    private int numOfBranchesHit;

    public LCovBRFHBlock(int numOfBranchesFound, int numOfBranchesHit) {
        this.numOfBranchesFound = numOfBranchesFound;
        this.numOfBranchesHit = numOfBranchesHit;
    }

    public int getNumOfBranchesFound() {
        return numOfBranchesFound;
    }

    public void setNumOfBranchesFound(int numOfBranchesFound) {
        this.numOfBranchesFound = numOfBranchesFound;
    }

    public int getNumOfBranchesHit() {
        return numOfBranchesHit;
    }

    public void setNumOfBranchesHit(int numOfBranchesHit) {
        this.numOfBranchesHit = numOfBranchesHit;
    }
}
