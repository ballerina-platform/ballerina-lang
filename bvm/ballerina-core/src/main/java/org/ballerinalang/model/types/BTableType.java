/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.types;

import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BTableType} represents tabular data in Ballerina.
 *
 * @since 0.8.0
 */
public class BTableType extends BType {

    private BType constraint;

    /**
     * Create a {@code BTableType} which represents the SQL Result Set.
     *
     * @param typeName string name of the type
     */
    BTableType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BTable.class);
    }

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, BTable.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BTable();
    }

    @Override
    public TypeSignature getSig() {
        if (constraint == null) {
            return new TypeSignature(TypeSignature.SIG_TABLE);
        } else {
            return new TypeSignature(TypeSignature.SIG_TABLE, constraint.getPackagePath(), constraint.getName());
        }
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }

    @Override
    public String toString() {
        if (constraint == null) {
            return super.toString();
        } else {
            return super.toString() + "<" + constraint.getName() + ">";
        }
    }
}
