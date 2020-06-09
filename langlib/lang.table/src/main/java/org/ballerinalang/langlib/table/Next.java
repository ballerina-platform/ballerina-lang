/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.table;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.ITERATOR_MUTABILITY_ERROR;
import static org.ballerinalang.util.BLangCompilerConstants.TABLE_VERSION;

/**
 * Native implementation of lang.table.TableIterator:next().
 *
 * @since 1.3.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.table", version = TABLE_VERSION, functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "TableIterator",
                structPackage = "ballerina/lang.table"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue t) {
        IteratorValue tableIterator = (IteratorValue) t.getNativeData("&iterator&");
        TableValueImpl table = (TableValueImpl) t.get(StringUtils.fromString("t"));
        ArrayValueImpl keys = (ArrayValueImpl) t.get(StringUtils.fromString("keys"));
        long initialSize = (long) t.get(StringUtils.fromString("size"));
        if (tableIterator == null) {
            tableIterator = table.getIterator();
            t.addNativeData("&iterator&", tableIterator);
            t.addNativeData("&returnedKeys&", new ArrayList<>());
        }

        // If the new values were added after the iterator is created, panic.
        // keys are the initial set of keys when the iterator was created.
        List<Object> returnedKeys = (ArrayList<Object>) t.getNativeData("&returnedKeys&");
        handleMutation(table, keys, returnedKeys, initialSize);
        if (tableIterator.hasNext()) {
            ArrayValue keyValueTuple = (ArrayValue) tableIterator.next();
            returnedKeys.add(keyValueTuple.get(0));
            return BallerinaValues.createRecord(new MapValueImpl<>(table.getIteratorNextReturnType()),
                    keyValueTuple.get(1));
        }

        return null;
    }

    private static void handleMutation(TableValueImpl table, ArrayValueImpl keys,
                                       List<Object> returnedKeys, long initialSize) {
        if (initialSize < table.size() ||
                // Key-less situation, mutation can occur only by calling add() or removeAll()
                (initialSize > 0 && table.size() == 0)) {
            throw BallerinaErrors.createError(ITERATOR_MUTABILITY_ERROR,
                    "Table was mutated after the iterator was created");
        }

        if (keys.isEmpty()) {
            return;
        }

        List<Object> currentKeys = new ArrayList<>(Arrays.asList(table.getKeys()));
        for (Object returnedValue : returnedKeys) {
            if (TypeChecker.isEqual(currentKeys.get(0), returnedValue)) {
                currentKeys.remove(0);
            }
        }

        ArrayValueImpl currentKeyArray = (ArrayValueImpl) BValueCreator.createArrayValue((BArrayType) keys.getType(),
                currentKeys.size());
        for (int i = 0; i < currentKeys.size(); i++) {
            Object key = currentKeys.get(i);
            currentKeyArray.add(i, key);
        }

        if (!TypeChecker.isEqual(currentKeyArray, keys)) {
            throw BallerinaErrors.createError(ITERATOR_MUTABILITY_ERROR,
                    "Table was mutated after the iterator was created");
        }

        keys.shift();
    }
}
