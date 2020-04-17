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

package org.ballerinalang.langlib.table;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Collection;

/**
 * Function for returning the values of the map as an array. T[] vals = m.toArray();
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.table",
        functionName = "toArray",
        args = {@Argument(name = "tbl", type = TypeKind.TABLE)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.ANYDATA)},
        isPublic = true
)
public class ToArray {

    public static ArrayValue toArray(Strand strand, TableValue tbl) {
        BType constrainedType = ((BTableType) tbl.getType()).getConstrainedType();

        Collection values = tbl.values();
        int size = values.size();
        int i = 0;
        switch (constrainedType.getTag()) {
            case TypeTags.INT_TAG:
                long[] intArr = new long[size];
                for (Object val : values) {
                    intArr[i++] = (Long) val;
                }
                return new ArrayValueImpl(intArr);
            case TypeTags.FLOAT_TAG:
                double[] floatArr = new double[size];
                for (Object val : values) {
                    floatArr[i++] = (Double) val;
                }
                return new ArrayValueImpl(floatArr);
            case TypeTags.BYTE_TAG:
                byte[] byteArr = new byte[size];
                for (Object val : values) {
                    byteArr[i++] = ((Integer) val).byteValue();
                }
                return new ArrayValueImpl(byteArr);
            case TypeTags.BOOLEAN_TAG:
                boolean[] booleanArr = new boolean[size];
                for (Object val : values) {
                    booleanArr[i++] = (Boolean) val;
                }
                return new ArrayValueImpl(booleanArr);
            case TypeTags.STRING_TAG:
                String[] stringArr = new String[size];
                for (Object val : values) {
                    stringArr[i++] = (String) val;
                }
                return new ArrayValueImpl(stringArr);
            default:
                return new ArrayValueImpl(values.toArray(), new BArrayType(constrainedType));
        }
    }
}
