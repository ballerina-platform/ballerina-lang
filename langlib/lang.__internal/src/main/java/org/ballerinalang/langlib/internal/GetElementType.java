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
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BTypedesc;

/**
 * Native implementation of lang.internal:getElementType(typedesc).
 *
 * @since 1.2.0
 */
public class GetElementType {

    public static BTypedesc getElementType(Object td) {
        BTypedesc bTypedesc = (BTypedesc) td;
        Type type = bTypedesc.getDescribingType();
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            return ValueCreator.createTypedescValue(((ArrayType) type).getElementType());
        } else if (type.getTag() == TypeTags.STREAM_TAG) {
            return ValueCreator.createTypedescValue(((StreamType) type).getConstrainedType());
        } else if (type.getTag() == TypeTags.TABLE_TAG) {
            return ValueCreator.createTypedescValue(((TableType) type).getConstrainedType());
        }

        return ValueCreator.createTypedescValue(PredefinedTypes.TYPE_NULL);
    }
}
