/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * This class contains a set of utility methods required for runtime api testing.
 *
 * @since 2201.9.0
 */
public class Stream {

    public static Object convertStringToType(BString s, BTypedesc t) {
        String str = s.getValue();
        InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        Object result = ValueUtils.parse(stream, TypeUtils.getImpliedType(t.getDescribingType()));
        return result;
    }

    public static void streamWithType(BArray arr) {
        Object[] typedescs = arr.getValues();

//        String[] strArray = new String[]{"[[12, \"thisstr\"], [true, 6.7]]", "[[12, \"thisstr\"], [true, 6.7]]"};
//        String[] strArray = new String[]{"\"strrrrr\"", "[\"thisstr\", \"thatstr\"]"};
//        String[] strArray = new String[]{"\"12\"", "[\"thisstr\", 32.5]"};
        String[] strArray = new String[]{"\"12\"", "[\"thisstr\", 32.5]"};
        InputStream stream;
        Object obj;

//        StringBuilder stringBuilder = new StringBuilder("[");
//        for (int i = 0; i < Math.pow(10, 7); i++) {
//            stringBuilder.append(i).append(",");
//        }
//        stringBuilder.append(0).append("]");
//        String largeString = stringBuilder.toString();

//        for (int i = 0; i < 100; i++) {
//            stream = new ByteArrayInputStream(largeString.getBytes(StandardCharsets.UTF_8));
//            obj = ValueUtils.parse(stream, TypeUtils.getImpliedType(((BTypedesc) typedescs[0]).getDescribingType()));
//            obj = ValueUtils.slowParse(stream,
//                TypeUtils.getImpliedType(((BTypedesc) typedescs[0]).getDescribingType()));
//        }

        for (int i = 0; i < arr.size(); i++) {
            stream = new ByteArrayInputStream(strArray[i].getBytes(StandardCharsets.UTF_8));
            obj = ValueUtils.parse(stream, TypeUtils.getImpliedType(((BTypedesc) typedescs[i]).getDescribingType()));
        }
//        for (int i = 0; i < 10000000; i++) {
//            stream = new ByteArrayInputStream(strArray[0].getBytes(StandardCharsets.UTF_8));
//            obj = ValueUtils.parse(stream, TypeUtils.getImpliedType(((BTypedesc) typedescs[0]).getDescribingType()));
//        }
    }
}
