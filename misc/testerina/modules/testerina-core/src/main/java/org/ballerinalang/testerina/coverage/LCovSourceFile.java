/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.testerina.coverage;

import java.util.LinkedList;
import java.util.List;

/**
 * Lcov formatted source file data.
 *
 * @since 0.985.0
 */
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

    /**
     * Getter for the source file path.
     *
     * @return source file path
     */
    public String getSourceFilePath() {
        return sourceFilePath;
    }

    /**
     * Setter for the source file path.
     *
     * @param sourceFilePath source file path
     */
    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    /**
     * Getter for lcov formatted function data list.
     *
     * @return lcov formatted function data list
     */
    public List<LCovFN> getlCovFNList() {
        return lCovFNList;
    }

    /**
     * Getter for lcov formatted function hit count list.
     *
     * @return lcov formatted function hit count list
     */
    public List<LCovFNDA> getlCovFNDAList() {

        return lCovFNDAList;
    }

    /**
     * Getter for lcov formatted function blocks with found and hit count list.
     *
     * @return lcov formatted function blocks with found and hit count list
     */
    public List<LCovFNFHBlock> getlCovFNFHBlockList() {
        return lCovFNFHBlockList;
    }

    /**
     * Getter for lcov formatted branch data list.
     *
     * @return lcov formatted branch data list
     */
    public List<LCovBRDA> getlCovBRDAList() {

        return lCovBRDAList;
    }

    /**
     * Getter for lcov formatted branch hit count list.
     *
     * @return lcov formatted branch hit count list
     */
    public List<LCovBRFHBlock> getlCovBRFHBlockList() {
        return lCovBRFHBlockList;
    }

    /**
     * Getter for lcov formatted line hit count list.
     *
     * @return lcov formatted line hit count list
     */
    public List<LCovDA> getlCovDAList() {
        return lCovDAList;
    }

    /**
     * Getter for lcov formatted number of lines executed in the source file.
     *
     * @return lcov formatted number of lines executed in the source file
     */
    public int getNumOfLineExecuted() {
        return numOfLineExecuted;
    }

    /**
     * Setter for lcov formatted number of lines executed in the source file.
     *
     * @param numOfLineExecuted lcov formatted number of lines executed in the source file
     */
    public void setNumOfLineExecuted(int numOfLineExecuted) {
        this.numOfLineExecuted = numOfLineExecuted;
    }

    /**
     * Getter for lcov formatted number of instrumented lines in the source file.
     *
     * @return lcov formatted number of instrumented lines in the source file
     */
    public int getNumOfInstrumentedLines() {
        return numOfInstrumentedLines;
    }

    /**
     * Setter for lcov formatted number of instrumented lines in the source file.
     *
     * @param numOfInstrumentedLines lcov formatted number of instrumented lines in the source file
     */
    public void setNumOfInstrumentedLines(int numOfInstrumentedLines) {
        this.numOfInstrumentedLines = numOfInstrumentedLines;
    }
}
