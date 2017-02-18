/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.datatables;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

/**
 * Native function to get arrays values of a given column index.
 * ballerina.model.datatables:getArray(datatable, int)
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.datatables",
        functionName = "getArray",
        args = {@Argument(name = "dt", type = TypeEnum.DATATABLE),
                @Argument(name = "index", type = TypeEnum.INT)},
        returnType = { @ReturnType(type = TypeEnum.MAP) },
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Retrieves arrays values of a given column index.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "dt",
        value = "The datatable object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "index",
        value = "") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "map)",
        value = "The column value as map") })
public class GetArrayByIndex extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BDataTable dataTable = (BDataTable) getArgument(ctx, 0);
        int index = ((BInteger) getArgument(ctx, 1)).intValue();
        Map<String, Object> arrayMap = dataTable.getArray(index);
        BMap<BString, BValue> returnMap = new BMap<>();
        if (arrayMap != null && !arrayMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : arrayMap.entrySet()) {
                BString key = new BString(entry.getKey());
                Object obj = entry.getValue();
                if (obj instanceof String) {
                    returnMap.put(key, new BString(String.valueOf(obj)));
                } else if (obj instanceof Boolean) {
                    returnMap.put(key, new BBoolean(Boolean.valueOf(obj.toString())));
                } else if (obj instanceof Integer) {
                    returnMap.put(key, new BInteger(Integer.parseInt(obj.toString())));
                } else if (obj instanceof Long) {
                    returnMap.put(key, new BLong(Long.parseLong(obj.toString())));
                } else if (obj instanceof Float) {
                    returnMap.put(key, new BFloat(Float.parseFloat(obj.toString())));
                } else if (obj instanceof Double) {
                    returnMap.put(key, new BDouble(Double.parseDouble(obj.toString())));
                }
            }
        }
        return getBValues(returnMap);
    }
}
