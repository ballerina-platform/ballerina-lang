/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

/**
 * Completion Item Priority enum.
 */
public enum  Priority {
    PRIORITY110(110),
    PRIORITY120(120),
    PRIORITY130(130),
    PRIORITY140(140),
    PRIORITY150(150),
    PRIORITY160(160),
    PRIORITY170(170),
    PRIORITY180(180),
    PRIORITY190(190),
    PRIORITY200(200),
    PRIORITY210(210),
    PRIORITY220(220),
    PRIORITY230(230),
    PRIORITY240(240);

    private final int priority;

    private Priority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }
    
    public String toString() {
        return Integer.toString(this.priority);
    }

    /**
     * Shift the base priority from the base priority and get the shifted new priority value.
     * @param basePriority  Base priority
     * @param offset        Offset to shift the base priority to get the new value
     * @return              {@link String} calculated new priority value
     */
    public static String shiftPriority(String basePriority, int offset) {
        return String.valueOf(Integer.parseInt(basePriority) + offset);
    }
}
