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
package org.ballerinalang.util.program;

/**
 * This class stores index values related to worker data.
 */
public class WorkerDataIndex {
    public int[] retRegs;
    public int longRegCount = 0;
    public int doubleRegCount = 0;
    public int stringRegCount = 0;
    public int intRegCount = 0;
    public int refRegCount = 0;
    public int byteRegCount = 0;
}
