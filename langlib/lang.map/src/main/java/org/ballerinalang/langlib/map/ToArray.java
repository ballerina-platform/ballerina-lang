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

package org.ballerinalang.langlib.map;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.langlib.map.util.MapLibUtils;

import java.util.Collection;

import static io.ballerina.runtime.internal.MapUtils.createOpNotSupportedError;

/**
 * Function for returning the values of the map as an array. T[] vals = m.toArray();
 *
 * @since 1.2.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.map",
//        functionName = "toArray",
//        args = {@Argument(name = "m", type = TypeKind.MAP)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.ANY)},
//        isPublic = true
//)
public class ToArray {

    public static BArray toArray(BMap<?, ?> m) {
        Type mapType = TypeUtils.getReferredType(m.getType());
        Type arrElemType;
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                arrElemType = ((MapType) mapType).getConstrainedType();
                break;
            case TypeTags.RECORD_TYPE_TAG:
                arrElemType = MapLibUtils.getCommonTypeForRecordField((RecordType) mapType);
                break;
            default:
                throw createOpNotSupportedError(mapType, "toArray()");
        }

        Collection values = m.values();
        int size = values.size();
        int i = 0;
        switch (arrElemType.getTag()) {
            case TypeTags.INT_TAG:
                long[] intArr = new long[size];
                for (Object val : values) {
                    intArr[i++] = (Long) val;
                }
                return ValueCreator.createArrayValue(intArr);
            case TypeTags.FLOAT_TAG:
                double[] floatArr = new double[size];
                for (Object val : values) {
                    floatArr[i++] = (Double) val;
                }
                return ValueCreator.createArrayValue(floatArr);
            case TypeTags.BYTE_TAG:
                byte[] byteArr = new byte[size];
                for (Object val : values) {
                    byteArr[i++] = ((Integer) val).byteValue();
                }
                return ValueCreator.createArrayValue(byteArr);
            case TypeTags.BOOLEAN_TAG:
                boolean[] booleanArr = new boolean[size];
                for (Object val : values) {
                    booleanArr[i++] = (Boolean) val;
                }
                return ValueCreator.createArrayValue(booleanArr);
            case TypeTags.STRING_TAG:
                BString[] stringArr = new BString[size];
                for (Object val : values) {
                    stringArr[i++] = (BString) val;
                }
                return ValueCreator.createArrayValue(stringArr);
            default:
                return ValueCreator.createArrayValue(values.toArray(), TypeCreator.createArrayType(arrElemType));

        }
    }
}
