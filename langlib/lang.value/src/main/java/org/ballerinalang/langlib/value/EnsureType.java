/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.value;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;

/**
 * Extern function lang.values:ensureType.
 *
 * @since 2.0.0
 */
public class EnsureType {
    public static Object ensureType(Object value, BTypedesc type) {
        if (TypeChecker.getType(value).getTag() == TypeTags.ERROR_TAG) {
            return value;
        }
        return convert(TypeUtils.getReferredType(type.getDescribingType()), value);
    }

    public static Object convert(Type convertType, Object inputValue) {
        try {
             return TypeChecker.checkCast(inputValue, convertType);
        } catch (BError e) {
            return e;
        }
    }
}
