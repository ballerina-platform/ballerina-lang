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

/**
 * Base interface for an entry in the Constant pool.
 *
 * @since 0.87
 */
public interface ConstantPoolEntry {

    EntryType getEntryType();

    /**
     * @since 0.87
     */
    enum EntryType {
        CP_ENTRY_UTF8((byte) 1),
        CP_ENTRY_INTEGER((byte) 2),
        CP_ENTRY_FLOAT((byte) 3),
        CP_ENTRY_STRING((byte) 4),
        CP_ENTRY_PACKAGE((byte) 5),
        CP_ENTRY_FUNCTION_REF((byte) 6),
        CP_ENTRY_ACTION_REF((byte) 7),
        CP_ENTRY_FUNCTION_CALL_ARGS((byte) 8),
        CP_ENTRY_STRUCTURE_REF((byte) 9),
        CP_ENTRY_TYPE_REF((byte) 10),
        CP_ENTRY_FORK_JOIN((byte) 11),
        CP_ENTRY_WRKR_DATA_CHNL_REF((byte) 12),
        CP_ENTRY_TRANSFORMER_REF((byte) 13),
        CP_ENTRY_BLOB((byte) 14);

        byte value;

        EntryType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }
}
