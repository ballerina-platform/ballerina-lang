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
package org.wso2.ballerinalang.programfile.cpentries;

/**
 * {@code CharacterCPEntry} represents a Ballerina character value in the constant pool.
 *
 * @since 0.964
 */
public class CharacterCPEntry implements ConstantPoolEntry {

    private int value;

    public CharacterCPEntry(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_CHARACTER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CharacterCPEntry that = (CharacterCPEntry) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
