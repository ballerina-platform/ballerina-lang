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
 * Lcov formatted line execution count.
 *
 * @since 0.985.0
 */
public class LCovDA {

    private int lineNumber;
    private int lineExecutionCount;
    private int checksum;

    public LCovDA(int lineNumber, int lineExecutionCount, int checksum) {
        this.lineNumber = lineNumber;
        this.lineExecutionCount = lineExecutionCount;
        this.checksum = checksum;
    }

    /**
     * Getter for the executed line number.
     *
     * @return executed line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Setter for the executed line number.
     *
     * @param lineNumber executed line number
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Getter for the number of times a line is executed.
     *
     * @return number of times a line is executed
     */
    public int getLineExecutionCount() {
        return lineExecutionCount;
    }

    /**
     * Setter for the number of times a line is executed.
     *
     * @param lineExecutionCount number of times a line is executed
     */
    public void setLineExecutionCount(int lineExecutionCount) {
        this.lineExecutionCount = lineExecutionCount;
    }

    /**
     * Getter for the line checksum. Optional.
     *
     * @return checksum value
     */
    public int getChecksum() {

        return checksum;
    }

    /**
     * Setter for the line checksum. Optional.
     *
     * @param checksum checksum value
     */
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }
}
