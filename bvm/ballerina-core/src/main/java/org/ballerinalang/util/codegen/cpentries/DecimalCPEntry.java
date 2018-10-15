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

package org.ballerinalang.util.codegen.cpentries;

import org.wso2.ballerinalang.compiler.semantics.model.types.util.Decimal;

/**
 * {@code DecimalCPEntry} represents a Ballerina decimal value in the constant pool.
 *
 * @since 0.982
 */
public class DecimalCPEntry implements ConstantPoolEntry {

    private Decimal value;

    public DecimalCPEntry(Decimal value) {
        this.value = value;
    }

    public Decimal getValue() {
        return value;
    }

    public EntryType getEntryType() {
        return EntryType.CP_ENTRY_DECIMAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecimalCPEntry that = (DecimalCPEntry) o;
        return value != null ? (value.compareTo(that.value) == 0) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
