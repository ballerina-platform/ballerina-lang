/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Initializes and holds line number info for a module.
 * This includes line number info for a program file.
 *
 * @since 0.985.0
 *
 */
public class ModuleLineNumberInfo {

    private LineNumberInfo[] ipLineNos;

    //key - fileName:ln, value - LineNumberInfo
    Map<String, LineNumberInfo> lineNumbers = new HashMap<>();

    public ModuleLineNumberInfo(int instructionCount) {
        this.ipLineNos = new LineNumberInfo[instructionCount];
    }

    /**
     * Populates line number info for a module.
     *
     * @param beginIp beginning Ip for the line
     * @param endIp ending Ip for the line
     * @param lineNumberInfo line number info holder object
     */
    void populateLineNumbers(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {

        addLineNumberInfo(beginIp, endIp, lineNumberInfo);
        String fileNameAndNo = lineNumberInfo.getFileName() + ":" + lineNumberInfo.getLineNumber();
        lineNumbers.put(fileNameAndNo, lineNumberInfo);
    }

    // populates array (index is same as Ip index in program file) of line number info
    private void addLineNumberInfo(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {

        for (int i = beginIp; i < endIp; i++) {
            ipLineNos[i] = lineNumberInfo;
        }
        lineNumberInfo.setEndIp(endIp);
    }

    /**
     * Getter for the line number info for the given Ip.
     *
     * @param ip given Ip
     * @return line number info holder object
     */
    public LineNumberInfo getLineNumberInfo(int ip) {
        return ipLineNos[ip];
    }

    /**
     * Getter for the line number info map for the program file.
     *
     * @return line number info map for the program file
     */
    public Map<String, LineNumberInfo> getLineNumbers() {
        return lineNumbers;
    }
}
