/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.array.utils;

import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;

/**
 * Utility functions for dealing with ArrayValue.
 *
 * @since 1.0
 */
public class ArrayUtils {

    public static void add(ArrayValue arr, int elemTypeTag, long index, Object value) {
        switch (elemTypeTag) {
            case TypeTags.INT_TAG:
                arr.add(index, (long) value);
                break;
            case TypeTags.BOOLEAN_TAG:
                arr.add(index, (boolean) value);
                break;
            case TypeTags.BYTE_TAG:
                arr.add(index, (byte) value);
                break;
            case TypeTags.FLOAT_TAG:
                arr.add(index, (double) value);
                break;
            case TypeTags.STRING_TAG:
                arr.add(index, (String) value);
                break;
            default:
                arr.add(index, value);
        }
    }
}
