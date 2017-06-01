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
package org.ballerinalang.util.codegen.cpentries;

import java.util.Arrays;

/**
 * {@code FunctionReturnCPEntry} contains register indexes of the return values.
 * <p>
 * Values in these registers will be copied to the callers stack frame.
 *
 * @since 0.87
 */
public class FunctionReturnCPEntry implements ConstantPoolEntry {

    // Register indexes of the return values;
    private int[] regIndexes;

    public FunctionReturnCPEntry(int[] regIndexes) {
        this.regIndexes = regIndexes;
    }

    public int[] getRegIndexes() {
        return regIndexes;
    }

    @Override
    public EntryType getEntryType() {
        return ConstantPoolEntry.EntryType.CP_ENTRY_FUNCTION_RET;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(regIndexes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FunctionReturnCPEntry && Arrays.equals(regIndexes,
                ((FunctionReturnCPEntry) obj).regIndexes);
    }
}
