/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.TypeId;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.List;

/**
 * This class contains a set of utility methods required for runtime api @{@link ErrorCreator} testing.
 *
 * @since 2.0.0
 */
public class Errors {

    private static Module errorModule = new Module("testorg", "runtime_api.errors", "1");

    public static BError getError(BString errorName) {
        BMap<BString, Object> errorDetails = ValueCreator.createMapValue();
        errorDetails.put(StringUtils.fromString("cause"), "Person age cannot be negative");
        return ErrorCreator.createError(errorModule, errorName.getValue(), StringUtils.fromString("Invalid age"),
                                        ErrorCreator.createError(StringUtils.fromString("Invalid data given")),
                                        errorDetails);
    }

    public static BArray getTypeIds(BError error) {
        List<TypeId> typeIds = ((ErrorType) error.getType()).getTypeIdSet().getIds();
        int size = typeIds.size();
        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING,
                size), size);
        int index = 0;
        for (TypeId typeId : typeIds) {
            arrayValue.add(index, StringUtils.fromString(typeId.getName()));
            index++;
        }
        return arrayValue;
    }
}
