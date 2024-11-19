/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.runtime.internal.utils;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;

/**
 * Util methods required for handling large arrays, tuples, maps and records.
 *
 * @since 2201.8.0
 */
public final class LargeStructureUtils {

    private LargeStructureUtils() {}

    public static HandleValue getListInitialValueEntryArray(long size) {
        return new HandleValue(new ListInitialValueEntry[(int) size]);
    }

    public static void setExpressionEntry(HandleValue arrayList, Object element, long index) {
        ListInitialValueEntry[] arr = (ListInitialValueEntry[]) arrayList.getValue();
        arr[(int) index] = new ListInitialValueEntry.ExpressionEntry(element);
    }

    public static void setSpreadEntry(HandleValue arrayList, Object element, long index) {
        ListInitialValueEntry[] arr = (ListInitialValueEntry[]) arrayList.getValue();
        arr[(int) index] = new ListInitialValueEntry.SpreadEntry((BArray) element);
    }

    public static HandleValue getBMapInitialValueEntryArray(long size) {
        return new HandleValue(new BMapInitialValueEntry[(int) size]);
    }

    public static void setKeyValueEntry(HandleValue arrayList, Object key, Object value, long index) {
        BMapInitialValueEntry[] arr = (BMapInitialValueEntry[]) arrayList.getValue();
        arr[(int) index] = new MappingInitialValueEntry.KeyValueEntry(key, value);
    }

    public static void setSpreadFieldEntry(HandleValue arrayList, Object spreadFieldEntry, long index) {
        BMapInitialValueEntry[] arr = (BMapInitialValueEntry[]) arrayList.getValue();
        arr[(int) index] = new MappingInitialValueEntry.SpreadFieldEntry((BMap<?, ?>) spreadFieldEntry);
    }

}
