/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.programfile;

/**
 * Describes an error handing section defined using try block in a Ballerina program.
 */
public class ErrorTableEntry {

    protected int ipFrom;
    protected int ipTo;
    protected int ipTarget;
    // Defined order in try catch.
    protected int priority;
    protected int errorStructCPIndex;

    public ErrorTableEntry(int ipFrom, int ipTo, int ipTarget, int priority, int errorStructCPIndex) {
        this.ipFrom = ipFrom;
        this.ipTo = ipTo;
        this.ipTarget = ipTarget;
        this.priority = priority;
        this.errorStructCPIndex = errorStructCPIndex;
    }
}
