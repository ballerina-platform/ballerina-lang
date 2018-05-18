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

import java.util.Arrays;

/**
 * {@code BlobCPEntry} represents a Ballerina blob value in the constant pool.
 *
 * @since 0.975.0
 */
public class BlobCPEntry implements ConstantPoolEntry {

    private byte[] value;

    public BlobCPEntry(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_BLOB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlobCPEntry that = (BlobCPEntry) o;

        return value != null ? Arrays.equals(value, that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
