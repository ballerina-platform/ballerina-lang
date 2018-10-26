package org.ballerinalang.testerina.coverage;

import java.util.LinkedList;
import java.util.List;

public class LCovSourceFile {

    private String sourceFilePath;

    private List<LCovFN> lCovFNList = new LinkedList<>();

    private List<LCovFNDA> lCovFNDAList = new LinkedList<>();

    private List<LCovFNFHBlock> lCovFNFHBlockList = new LinkedList<>();

    private List<LCovBRDA> lCovBRDAList = new LinkedList<>();

    private List<LCovBRFHBlock> lCovBRFHBlockList = new LinkedList<>();

    private List<LCovDA> lCovDAList = new LinkedList<>();

    private int numOfLineExecuted;

    private int numOfInstrumentedLines;

    public LCovSourceFile(String sourceFilePath, int numOfLineExecuted,
                          int numOfInstrumentedLines) {
        this.sourceFilePath = sourceFilePath;
        this.numOfLineExecuted = numOfLineExecuted;
        this.numOfInstrumentedLines = numOfInstrumentedLines;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public List<LCovFN> getlCovFNList() {
        return lCovFNList;
    }

    public List<LCovFNDA> getlCovFNDAList() {
        return lCovFNDAList;
    }

    public List<LCovFNFHBlock> getlCovFNFHBlockList() {
        return lCovFNFHBlockList;
    }

    public List<LCovBRDA> getlCovBRDAList() {
        return lCovBRDAList;
    }

    public List<LCovBRFHBlock> getlCovBRFHBlockList() {
        return lCovBRFHBlockList;
    }

    public List<LCovDA> getlCovDAList() {
        return lCovDAList;
    }

    public int getNumOfLineExecuted() {
        return numOfLineExecuted;
    }

    public void setNumOfLineExecuted(int numOfLineExecuted) {
        this.numOfLineExecuted = numOfLineExecuted;
    }

    public int getNumOfInstrumentedLines() {
        return numOfInstrumentedLines;
    }

    public void setNumOfInstrumentedLines(int numOfInstrumentedLines) {
        this.numOfInstrumentedLines = numOfInstrumentedLines;
    }
}
