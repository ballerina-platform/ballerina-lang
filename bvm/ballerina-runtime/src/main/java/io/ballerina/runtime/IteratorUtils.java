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

package io.ballerina.runtime;

import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.TypeFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.types.BField;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.util.Flags;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the utility methods related to iterators.
 *
 * @since 1.2.0
 */
public class IteratorUtils {

    /**
     * Returns the pure type and anydata type flags if they are available, otherwise 0. This is only used to check if
     * the record type returned fro the next function of an iterator, is a pure type or anydata.
     * @param type The type being checked
     * @return The type flags
     */
    public static int getTypeFlags(Type type) {
        if (type.isAnydata()) {
            return TypeFlags.asMask(TypeFlags.PURETYPE, TypeFlags.ANYDATA);
        }

        if (type.isPureType()) {
            return TypeFlags.PURETYPE;
        }
        return 0;
    }

    public static BRecordType createIteratorNextReturnType(Type type) {
        Map<String, Field> fields = new HashMap<>();
        fields.put("value", new BField(type, "value", Flags.PUBLIC + Flags.REQUIRED));
        return new BRecordType(TypeConstants.ITERATOR_NEXT_RETURN_TYPE, null, 0, fields, null, true,
                getTypeFlags(type));
    }
}
