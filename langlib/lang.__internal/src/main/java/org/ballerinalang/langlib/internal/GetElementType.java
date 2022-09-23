/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;

/**
 * Native implementation of lang.internal:getElementType(typedesc).
 *
 * @since 1.2.0
 */
public class GetElementType {

    private GetElementType() {
    }

    public static BTypedesc getElementType(Object td) {
        BTypedesc bTypedesc = (BTypedesc) td;
        return getElementTypeDescValue(bTypedesc.getDescribingType());
    }

    private static BTypedesc getElementTypeDescValue(Type type) {
        switch (type.getTag()) {
            case TypeTags.ARRAY_TAG:
                return ValueCreator.createTypedescValue(((ArrayType) type).getElementType());
            case TypeTags.TUPLE_TAG:
                return ValueCreator.createTypedescValue(
                        TypeCreator.createUnionType(((TupleType) type).getTupleTypes()));
            case TypeTags.FINITE_TYPE_TAG:
                // this is reached only for immutable values
                return getElementTypeDescValue(
                        ((BValue) (((FiniteType) type).getValueSpace().iterator().next())).getType());
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return getElementTypeDescValue(((ReferenceType) type).getReferredType());
            default:
                return ValueCreator.createTypedescValue(((StreamType) type).getConstrainedType());
        }
    }
}
