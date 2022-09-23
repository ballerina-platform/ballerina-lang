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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.TypeChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.ITERATOR_MUTABILITY_ERROR;

/**
 * Native implementation of lang.table.TableIterator:next().
 *
 * @since 1.3.0
 */
public class Next {

    private static final BString MUTATED_TABLE_ERROR_DETAIL =  StringUtils.fromString("Table was mutated after the " +
                                                                                               "iterator was created");
    //TODO: refactor hard coded values
    public static Object next(BObject t) {
        BIterator tableIterator = (BIterator) t.getNativeData("&iterator&");
        BTable table = (BTable) t.get(StringUtils.fromString("t"));
        BArray keys = (BArray) t.get(StringUtils.fromString("keys"));
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
            BArray keyValueTuple = (BArray) tableIterator.next();
            returnedKeys.add(keyValueTuple.get(0));
            return ValueCreator.createRecordValue(ValueCreator.createRecordValue(
                    (RecordType) table.getIteratorNextReturnType()), keyValueTuple.get(1));
        }

        return null;
    }

    private static void handleMutation(BTable table, BArray keys,
                                       List<Object> returnedKeys, long initialSize) {
        if (initialSize < table.size() ||
                // Key-less situation, mutation can occur only by calling add() or removeAll()
                (initialSize > 0 && table.size() == 0)) {
            throw ErrorCreator.createError(ITERATOR_MUTABILITY_ERROR, MUTATED_TABLE_ERROR_DETAIL);
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

        BArray currentKeyArray = ValueCreator.createArrayValue((ArrayType) TypeUtils.getReferredType(keys.getType()));
        for (int i = 0; i < currentKeys.size(); i++) {
            Object key = currentKeys.get(i);
            currentKeyArray.add(i, key);
        }

        if (!TypeChecker.isEqual(currentKeyArray, keys)) {
            throw ErrorCreator.createError(ITERATOR_MUTABILITY_ERROR, MUTATED_TABLE_ERROR_DETAIL);
        }

        keys.shift();
    }
}
