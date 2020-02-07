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
package org.wso2.ballerinalang.programfile.cpentries;

/**
 * {@code ConstantPool} represents a table of symbolic information about constants, functions, actions, connectors,
 * structs etc.
 *
 * @since 0.90
 */
@Deprecated
public interface ConstantPool {

    int addCPEntry(ConstantPoolEntry cpEntry);

    ConstantPoolEntry getCPEntry(int index);

    int getCPEntryIndex(ConstantPoolEntry cpEntry);

    ConstantPoolEntry[] getConstPoolEntries();
}
