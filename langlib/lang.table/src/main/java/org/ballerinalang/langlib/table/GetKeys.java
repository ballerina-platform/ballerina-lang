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

import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Extern function to get key arrays from the table.
 * ballerina.model.table:keys()
 *
 * @since 1.3.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.table",
//        functionName = "keys",
//        args = {@Argument(name = "tbl", type = TypeKind.TABLE)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.ANYDATA)},
//        isPublic = true
//)
public class GetKeys {

    public static BArray keys(BTable tbl) {
        Type tableKeyType = tbl.getKeyType();
        Object[] keys = tbl.getKeys();
        switch (tableKeyType.getTag()) {
            case TypeTags.BOOLEAN:
                boolean[] boolArr  = new boolean[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
                    boolArr[i] = (boolean) key;
                }
                return (BArray) ValueCreator.createArrayValue(boolArr);
            case TypeTags.INT:
                long[] intArr  = new long[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
                    intArr[i] = (long) key;
                }
                return (BArray) ValueCreator.createArrayValue(intArr);
            case TypeTags.BYTE:
                byte[] byteArr  = new byte[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
                    byteArr[i] = (byte) key;
                }
                return (BArray) ValueCreator.createArrayValue(byteArr);
            case TypeTags.FLOAT:
                double[] floatArr  = new double[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
                    floatArr[i] = (double) key;
                }
                return (BArray) ValueCreator.createArrayValue(floatArr);
            case TypeTags.STRING:
                BString[] stringArr  = new BString[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    Object key = keys[i];
                    stringArr[i] = (BString) key;
                }
                return (BArray) ValueCreator.createArrayValue(stringArr);
            default:
                return (BArray) ValueCreator
                        .createArrayValue(tbl.getKeys(), TypeCreator.createArrayType(tbl.getKeyType()));
        }
    }
}
