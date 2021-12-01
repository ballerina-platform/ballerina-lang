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

package org.ballerinalang.test.types.readonly;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Interop functions for readonly array creation.
 */
public class ReadonlyArrayCreator {

    public static BArray createIntArray() {
        long[] numbers = {1000, 2000, 3000};
        return ValueCreator.createReadonlyArrayValue(numbers);
    }

    public static BArray createBooleanArray() {
        boolean[] booleans = {true, false, true};
        return ValueCreator.createReadonlyArrayValue(booleans);
    }

    public static BArray createByteArray() {
        byte[] bytes = {1, 2, 3};
        return ValueCreator.createReadonlyArrayValue(bytes);
    }

    public static BArray createFloatArray() {
        double[] numbers = {1.1, 2.2, 3.3};
        return ValueCreator.createReadonlyArrayValue(numbers);
    }

    public static BArray createStringArray() {
        BString[] numbers = {StringUtils.fromString("a"), StringUtils.fromString("b"), StringUtils.fromString("c")};
        return ValueCreator.createReadonlyArrayValue(numbers);
    }

    public static BArray createArrayOfMaps(BMap map) {
        return ValueCreator.createArrayValue(TypeCreator.createArrayType(map.getType(), 0, true));
    }
}
