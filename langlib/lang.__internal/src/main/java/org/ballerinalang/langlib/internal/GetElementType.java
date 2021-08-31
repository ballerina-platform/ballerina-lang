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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.FiniteType;
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

    public static BTypedesc getElementType(Object td) {
        BTypedesc bTypedesc = (BTypedesc) td;
        return getTypeDescValue(bTypedesc.getDescribingType());
    }

    private static BTypedesc getTypeDescValue(Type type) {
        switch (type.getTag()) {
            case TypeTags.ARRAY_TAG:
                return ValueCreator.createTypedescValue(((ArrayType) type).getElementType());
            case TypeTags.TUPLE_TAG:
                return ValueCreator.createTypedescValue(
                        TypeCreator.createUnionType(((TupleType) type).getTupleTypes()));
            case TypeTags.STREAM_TAG:
                return ValueCreator.createTypedescValue(((StreamType) type).getConstrainedType());
            case TypeTags.FINITE_TYPE_TAG:
                if (((FiniteType) type).getValueSpace().size() == 1) {
                    return getTypeDescValue(
                            ((BValue) (((FiniteType) type).getValueSpace().iterator().next())).getType());
                }
                break;
            default:
                break;
        }
        return ValueCreator.createTypedescValue(PredefinedTypes.TYPE_NULL);
    }
}
