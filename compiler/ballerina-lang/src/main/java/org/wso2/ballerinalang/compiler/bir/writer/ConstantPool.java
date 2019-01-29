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
package org.wso2.ballerinalang.compiler.bir.writer;


import java.util.ArrayList;
import java.util.List;

/**
 * A pool of constant values in the binary BIR file.
 *
 * @since 0.980.0
 */
public class ConstantPool {

    private List<CPEntry> cpEntries = new ArrayList<>();

    public int addCPEntry(CPEntry cpEntry) {
        if (cpEntries.contains(cpEntry)) {
            return cpEntries.indexOf(cpEntry);
        }

        cpEntries.add(cpEntry);
        return cpEntries.size() - 1;
    }

    public CPEntry getCPEntry(int index) {
        return cpEntries.get(index);
    }

    public int getCPEntryIndex(CPEntry cpEntry) {
        return cpEntries.indexOf(cpEntry);
    }

    public CPEntry[] getConstPoolEntries() {
        return cpEntries.toArray(new CPEntry[0]);
    }
}
