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
 * Lcov formatted branch coverage data info holder for found and hit count.
 *
 * @since 0.985.0
 */
public class LCovBRFHBlock {

    private int numOfBranchesFound;
    private int numOfBranchesHit;

    public LCovBRFHBlock(int numOfBranchesFound, int numOfBranchesHit) {
        this.numOfBranchesFound = numOfBranchesFound;
        this.numOfBranchesHit = numOfBranchesHit;
    }

    /**
     * Getter for the number of branches found.
     *
     * @return number of branches found
     */
    public int getNumOfBranchesFound() {
        return numOfBranchesFound;
    }

    /**
     * Setter for the number of branches found.
     *
     * @param numOfBranchesFound number of branches found
     */
    public void setNumOfBranchesFound(int numOfBranchesFound) {
        this.numOfBranchesFound = numOfBranchesFound;
    }

    /**
     * Getter for the number of branches hit.
     *
     * @return number of branches hit
     */
    public int getNumOfBranchesHit() {
        return numOfBranchesHit;
    }

    /**
     * Setter for the number of branches hit.
     *
     * @param numOfBranchesHit number of branches hit
     */
    public void setNumOfBranchesHit(int numOfBranchesHit) {
        this.numOfBranchesHit = numOfBranchesHit;
    }
}
