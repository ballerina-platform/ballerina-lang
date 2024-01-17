/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

/**
 * Create a record value from the provided value map.
 *
 * @since 2.0
 */
public class CreateRecordValue {

    public static Object createRecordFromMap(BMap<?, ?> value, BTypedesc t) {
        Type describingType = t.getDescribingType();
        BMap<BString, Object> valMap = (BMap<BString, Object>) value;

        if (describingType.isReadOnly()) {
            return ValueCreator.createReadonlyRecordValue(describingType.getPackage(),
                    describingType.getName(), valMap);
        } else {
            return ValueCreator.createRecordValue(describingType.getPackage(), describingType.getName(), valMap);
        }
    }

    private CreateRecordValue() {}
}
