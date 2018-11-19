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
 * Lcov formatted function execution data: found and hit count.
 *
 * @since 0.985.0
 */
public class LCovFNFHBlock {

    private int numOfFuncFound;
    private int numOfFuncHit;

    public LCovFNFHBlock(int numOfFuncFound, int numOfFuncHit) {
        this.numOfFuncFound = numOfFuncFound;
        this.numOfFuncHit = numOfFuncHit;
    }

    /**
     * Getter for number of functions found.
     *
     * @return number of functions found
     */
    public int getNumOfFuncFound() {
        return numOfFuncFound;
    }

    /**
     * Setter for number of functions found.
     *
     * @param numOfFuncFound number of functions found
     */
    public void setNumOfFuncFound(int numOfFuncFound) {
        this.numOfFuncFound = numOfFuncFound;
    }

    /**
     * Getter for number of functions hit.
     *
     * @return number of functions hit
     */
    public int getNumOfFuncHit() {
        return numOfFuncHit;
    }

    /**
     * Setter for number of functions hit.
     *
     * @param numOfFuncHit number of functions hit
     */
    public void setNumOfFuncHit(int numOfFuncHit) {
        this.numOfFuncHit = numOfFuncHit;
    }
}
