/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains a set of utility methods required for runtime enum values @{@link ValueCreator} testing.
 *
 * @since 2201.1.0
 */
public class Enums {

    private static final Module enumModule = new Module("testorg", "values.enum", "1");

    public static BArray createEnumArray(BString enumName) {
        List<Type> memberTypes = new ArrayList<>(2);
        Set<Object> valuesSpace = new LinkedHashSet<>();
        valuesSpace.add(StringUtils.fromString("OPEN"));
        memberTypes.add(TypeCreator.createFiniteType("type1", valuesSpace, TypeFlags.ANYDATA));
        valuesSpace = new LinkedHashSet<>();
        valuesSpace.add(StringUtils.fromString("CLOSE"));
        memberTypes.add(TypeCreator.createFiniteType("type2", valuesSpace, TypeFlags.ANYDATA));
        UnionType unionType = TypeCreator.createUnionType(memberTypes, enumName.getValue(), enumModule, 0, false,
                SymbolFlags.READONLY | SymbolFlags.ENUM);
        ArrayType arrayType = TypeCreator.createArrayType(unionType);
        return ValueCreator.createArrayValue(arrayType);
    }

    public static void testFiniteTypeUnionElements(BTypedesc t, BArray elements) {
        Set<String> members = new HashSet<>();
        members.addAll(Arrays.asList(elements.getStringArray()));
        BError notFiniteTypeError = ErrorCreator.createError(
                StringUtils.fromString("union member type is not a finite type"));
        BError matchError = ErrorCreator.createError(
                StringUtils.fromString("type name does not match given names"));

        Type describingType = TypeUtils.getReferredType(t.getDescribingType());
        if (describingType.getTag() == TypeTags.UNION_TAG) {
            UnionType unionType = (UnionType) describingType;
            for (Type memberType : unionType.getMemberTypes()) {
                if (memberType.getTag() == TypeTags.FINITE_TYPE_TAG) {
                    String typeName = memberType.getName();
                    if (members.contains(typeName)) {
                        members.remove(typeName);
                    } else {
                        throw matchError;
                    }
                } else {
                    throw notFiniteTypeError;
                }
            }
            if (!members.isEmpty()) {
                throw ErrorCreator.createError(StringUtils.fromString("all union member type names are not provided"));
            }
        } else {
            throw ErrorCreator.createError(StringUtils.fromString("given type is not a union type"));
        }
    }

    public static void addToEnumArray(BArray array, BString value) {
       array.append(value);
    }
}
