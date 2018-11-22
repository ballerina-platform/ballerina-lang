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

/**
 * Lcov formatted branch coverage data info holder.
 *
 * @since 0.985.0
 */
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

    /**
     * Getter for line number for the branch.
     *
     * @return line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Setter for line number for the branch.
     *
     * @param lineNumber line number
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Getter for source code block number.
     * Generated Id by the compiler.
     *
     * @return source code block number
     */
    public int getBlockNumber() {
        return blockNumber;
    }

    /**
     * Setter for source code block number.
     * Generated Id by the compiler.
     *
     * @param blockNumber source code block number
     */
    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Getter for source code branch number.
     * Generated Id by the compiler.
     *
     * @return source code branch number
     */
    public int getBranchNumber() {
        return branchNumber;
    }

    /**
     * Setter for source code branch number.
     * Generated Id by the compiler.
     *
     * @param branchNumber source code branch number
     */
    public void setBranchNumber(int branchNumber) {
        this.branchNumber = branchNumber;
    }

    /**
     * Getter for status whether line is executed or not. Either # or "-" for never executed
     *
     * @return line executed status
     */
    public String getTaken() {
        return taken;
    }

    /**
     * Setter for status whether line is executed or not. Either # or "-" for never executed
     *
     * @param taken line executed status
     */
    public void setTaken(String taken) {
        this.taken = taken;
    }
}
