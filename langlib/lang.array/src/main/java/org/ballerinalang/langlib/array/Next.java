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

package org.ballerinalang.langlib.array;

import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.BValueCreator;
import io.ballerina.jvm.values.AbstractArrayValue;
import io.ballerina.jvm.values.IteratorValue;
import io.ballerina.jvm.values.MapValueImpl;
import io.ballerina.jvm.values.ObjectValue;

/**
 * Native implementation of lang.array.ArrayIterator:next().
 *
 * @since 1.0
 */
public class Next {
    //TODO: refactor hard coded values
    public static Object next(ObjectValue m) {
        IteratorValue arrIterator = (IteratorValue) m.getNativeData("&iterator&");
        AbstractArrayValue arr = (AbstractArrayValue) m.get(BStringUtils.fromString("m"));
        if (arrIterator == null) {
            arrIterator = arr.getIterator();
            m.addNativeData("&iterator&", arrIterator);
        }

        if (arrIterator.hasNext()) {
            Object element = arrIterator.next();
            return BValueCreator.createRecordValue(new MapValueImpl<>(arr.getIteratorNextReturnType()), element);
        }

        return null;
    }
}
