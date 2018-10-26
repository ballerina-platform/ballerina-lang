package org.ballerinalang.testerina.coverage;

public class LCovFNFHBlock {

    private int numOfFuncFound;

    private int numOfFuncHit;

    public LCovFNFHBlock(int numOfFuncFound, int numOfFuncHit) {
        this.numOfFuncFound = numOfFuncFound;
        this.numOfFuncHit = numOfFuncHit;
    }

    public int getNumOfFuncFound() {
        return numOfFuncFound;
    }

    public void setNumOfFuncFound(int numOfFuncFound) {
        this.numOfFuncFound = numOfFuncFound;
    }

    public int getNumOfFuncHit() {
        return numOfFuncHit;
    }

    public void setNumOfFuncHit(int numOfFuncHit) {
        this.numOfFuncHit = numOfFuncHit;
    }
}
