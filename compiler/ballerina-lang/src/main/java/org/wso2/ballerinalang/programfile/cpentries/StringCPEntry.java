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
 * {@code StringCPEntry} represents a Ballerina string value in the constant pool.
 *
 * @since 0.87
 */
@Deprecated
public class StringCPEntry implements ConstantPoolEntry {

    // Index to a valid UTF8 entry in the constant pool
    private int stringCPIndex;

    private String value;

    public StringCPEntry(int stringCPIndex, String value) {
        this.stringCPIndex = stringCPIndex;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getStringCPIndex() {
        return stringCPIndex;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_STRING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringCPEntry that = (StringCPEntry) o;
        if (stringCPIndex != that.stringCPIndex) {
            return false;
        }

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = stringCPIndex;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
