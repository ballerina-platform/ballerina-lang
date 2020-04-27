/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TupleValueImpl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR;

/**
 * This class contains the utility methods required by the table implementation.
 *
 * @since 1.3.0
 */

public class TableUtils {

    /**
     * Generates a hash value which is same for the same shape.
     *
     * @param obj Ballerina value which the hash is generated from
     * @return The hash value
     */
    public static Integer hash(Object obj, List<Object> checkedValues) {
        int result;

        if (checkedValues.contains(obj)) {
            throw new BallerinaException(CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE,
                            TypeChecker.getType(obj)));
        }

        checkedValues.add(obj);

        if (obj instanceof RefValue) {
            RefValue refValue = (RefValue) obj;
            BType refType = refValue.getType();
            if (refType.getTag() == TypeTags.MAP_TAG || refType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                MapValue mapValue = (MapValue) refValue;
                result = mapValue.getType().hashCode();
                for (Object entry : mapValue.entrySet()) {
                    result = 31 * result + hash(((Map.Entry) entry).getKey(), checkedValues) +
                            (((Map.Entry) entry).getValue() == null ? 0 : hash(((Map.Entry) entry).getValue(),
                                    checkedValues));
                }
                return result;
            } else if (refType.getTag() == TypeTags.ARRAY_TAG) {
                ArrayValue arrayValue = (ArrayValue) refValue;
                result = Objects.hash(refType, arrayValue.getElementType());
                IteratorValue arrayIterator = arrayValue.getIterator();
                while (arrayIterator.hasNext()) {
                    result = 31 * result + hash(arrayIterator.next(), checkedValues);
                }
                return result;
            } else if (refType.getTag() == TypeTags.TUPLE_TAG) {
                TupleValueImpl arrayValue = (TupleValueImpl) refValue;
                result = Objects.hash(refType, ((BTupleType) refType).getTupleTypes());
                IteratorValue arrayIterator = arrayValue.getIterator();
                while (arrayIterator.hasNext()) {
                    result = 31 * result + hash(arrayIterator.next(), checkedValues);
                }
                return result;
            } else {
                return obj.hashCode();
            }
        } else {
            return obj.hashCode();
        }
    }

    /**
     * Handles table insertion/store functionality.
     *
     * @param tableValue Table value which the values are inserted to
     * @param key        The key associated with the value
     * @param value      The value being inserted
     */
    public static void handleTableStore(TableValue<Object, Object> tableValue, Object key, Object value) {
        tableValue.put(key, value);
    }
}
