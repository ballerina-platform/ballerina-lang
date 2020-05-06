/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TableValueImpl;

/**
 * {@code BTableType} represents tabular data in Ballerina.
 *
 * @since 1.3.0
 */
public class BTableType extends BType {

    private BType constraint;
    private BType keyType;
    private String[] fieldNames;

    public BTableType(BType constraint, String[] fieldNames) {
        super(TypeConstants.TABLE_TNAME, null, TableValue.class);
        this.constraint = constraint;
        this.fieldNames = fieldNames;
        this.keyType = null;
    }

    public BTableType(BType constraint) {
        super(TypeConstants.TABLE_TNAME, null, TableValue.class);
        this.constraint = constraint;
    }

    public BType getConstrainedType() {
        return constraint;
    }

    public BType getKeyType() {
        return keyType;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public <V> V getZeroValue() {
        return (V) new TableValueImpl<BAnydataType, V>(new BTableType(constraint));
    }

    @Override
    public <V> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TABLE_TAG;
    }

    @Override
    public String toString() {
        if (constraint == null) {
            return super.toString();
        }

        StringBuilder keyStringBuilder = new StringBuilder();
        if (fieldNames != null) {
            for (String fieldName : fieldNames) {
                if (!keyStringBuilder.toString().equals("")) {
                    keyStringBuilder.append(", ");
                }
                keyStringBuilder.append(fieldName);
            }
            return super.toString() + "<" + constraint.getName() + "> key(" + keyStringBuilder.toString() + ")";
        }

        return super.toString() + "<" + constraint.getName() + ">" +
                ((keyType != null) ? (" key<" + keyType + ">") : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BTableType)) {
            return false;
        }

        BTableType other = (BTableType) obj;
        if (constraint == other.constraint && keyType == other.keyType) {
            return true;
        }

        if (constraint == null || other.constraint == null) {
            return false;
        }

        if (keyType == null || other.keyType == null) {
            return false;
        }

        return constraint.equals(other.constraint) && keyType.equals(other.keyType);
    }
}
