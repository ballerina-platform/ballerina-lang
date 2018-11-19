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
 * Lcov formatted function data.
 *
 * @since 0.985.0
 */
public class LCovFN {

    private int funcStartLineNum;
    private String functionName;

    public LCovFN(int funcStartLineNum, String functionName) {
        this.funcStartLineNum = funcStartLineNum;
        this.functionName = functionName;
    }

    /**
     * Getter for the function start line number.
     *
     * @return function start line number
     */
    public int getFuncStartLineNum() {
        return funcStartLineNum;
    }

    /**
     * Setter for the function start line number.
     *
     * @param funcStartLineNum function start line number
     */
    public void setFuncStartLineNum(int funcStartLineNum) {
        this.funcStartLineNum = funcStartLineNum;
    }

    /**
     * Getter for the function name.
     *
     * @return function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Setter for the function name.
     *
     * @param functionName function name
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
