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
    PRIORITY10(10),
    PRIORITY20(20),
    PRIORITY30(30),
    PRIORITY40(40),
    PRIORITY50(50),
    PRIORITY60(60),
    PRIORITY70(70),
    PRIORITY80(80),
    PRIORITY90(90),
    PRIORITY100(100),
    PRIORITY110(110),
    PRIORITY120(120);

    private int priority;

    private Priority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }
    
    public String toString() {
        return Integer.toString(this.priority);
    }
}
