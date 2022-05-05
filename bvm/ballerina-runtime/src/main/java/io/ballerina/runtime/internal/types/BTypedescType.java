/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;

/**
 * {@code BTypeType} represents type of type in Ballerina type system.
 *
 * @since 0.995.0
 */
public class BTypedescType extends BType implements TypedescType {
    private Type constraint;

    public BTypedescType(String typeName, Module pkg) {
        super(typeName, pkg, Object.class);
    }

    public BTypedescType(Type constraint) {
        super(TypeConstants.TYPEDESC_TNAME, null, TypedescValue.class);
        this.constraint = constraint;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new TypedescValueImpl(PredefinedTypes.TYPE_NULL);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TYPEDESC_TAG;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BTypedescType) {
            return constraint.equals(((BTypedescType) obj).getConstraint());
        }
        return false;
    }

    public Type getConstraint() {
        return constraint;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String toString() {
        return "typedesc" + "<" + constraint.toString() + ">";
    }
}
