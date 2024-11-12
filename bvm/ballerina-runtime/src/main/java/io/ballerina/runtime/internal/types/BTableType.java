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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.TableValue;
import io.ballerina.runtime.internal.values.TableValueImpl;

import java.util.Optional;

/**
 * {@code BTableType} represents tabular data in Ballerina.
 *
 * @since 1.3.0
 */
public class BTableType extends BType implements TableType {

    private final Type constraint;
    private Type keyType;
    private String[] fieldNames = new String[]{};

    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;

    public BTableType(Type constraint, String[] fieldNames, boolean readonly) {
        this(constraint, readonly);
        this.fieldNames = fieldNames;
    }

    public BTableType(Type constraint, Type keyType, boolean readonly) {
        this(constraint, readonly);
        this.keyType = keyType;
    }

    public BTableType(Type constraint, boolean readonly) {
        super(TypeConstants.TABLE_TNAME, null, TableValue.class);
        this.constraint = readonly ? ReadOnlyUtils.getReadOnlyType(constraint) : constraint;
        this.readonly = readonly;
    }

    @Override
    public Type getConstrainedType() {
        return constraint;
    }

    public Type getKeyType() {
        return keyType;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public <V> V getZeroValue() {
        return (V) new TableValueImpl<BAnydataType, V>(new BTableType(constraint, readonly));
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
            return readonly ? super.toString().concat(" & readonly") : super.toString();
        }

        StringBuilder keyStringBuilder = new StringBuilder();
        String stringRep;
        if (fieldNames.length > 0) {
            for (String fieldName : fieldNames) {
                if (!keyStringBuilder.toString().isEmpty()) {
                    keyStringBuilder.append(", ");
                }
                keyStringBuilder.append(fieldName);
            }
            stringRep = super.toString() + "<" + constraint + "> key(" + keyStringBuilder.toString() + ")";
        } else {
            stringRep = super.toString() + "<" + constraint + ">" +
                    ((keyType != null) ? (" key<" + keyType + ">") : "");
        }

        return readonly ? stringRep.concat(" & readonly") : stringRep;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BTableType other)) {
            return false;
        }

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

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    @Override
    public boolean isAnydata() {
        return this.constraint.isAnydata();
    }
}
