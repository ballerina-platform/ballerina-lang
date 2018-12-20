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
 * {@code FunctionReturnCPEntry} contains register indexes of function arguments and register indexes to which
 * the return values should be stored in the current stack frame.
 *
 * @since 0.87
 */
public class FunctionCallCPEntry implements ConstantPoolEntry {

    private int flags;
    
    // Registers which contains function arguments
    private int[] argRegs;

    // Registers to which return  values to be copied
    private int[] retRegs;

    public FunctionCallCPEntry(int flags, int[] argRegs, int[] retRegs) {
        this.flags = flags;
        this.argRegs = argRegs;
        this.retRegs = retRegs;
    }
    
    public int getFlags() {
        return flags;
    }

    public int[] getArgRegs() {
        return argRegs;
    }

    public int[] getRetRegs() {
        return retRegs;
    }

    public ConstantPoolEntry.EntryType getEntryType() {
        return ConstantPoolEntry.EntryType.CP_ENTRY_FUNCTION_CALL_ARGS;
    }

    @Override
    public int hashCode() {
        int result = flags;
        result = 31 * result + Arrays.hashCode(argRegs);
        result = 31 * result + Arrays.hashCode(retRegs);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FunctionCallCPEntry that = (FunctionCallCPEntry) o;
        return flags == that.flags &&
               Arrays.equals(argRegs, that.argRegs) &&
               Arrays.equals(retRegs, that.retRegs);
    }
}
