/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.Iterator;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_ACCESS_FIELD;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_FIELD;


/**
 * Implementation of 'next' method of iterator object returned by query.
 *
 * @since 2201.13.0
 */
public class IteratorObject {
    public static Object next(Object itr) {
        Iterator<BMap<BString, Object>> iterator = (Iterator<BMap<BString, Object>>) itr;

        try {
            if (iterator.hasNext() && iterator.next() instanceof BMap<BString, Object> frame) {
                Object value = frame.get(VALUE_ACCESS_FIELD);
                if (value instanceof BError error) {
                    return error;
                }
                BMap<BString, Object> record = ValueCreator
                        .createRecordValue(BALLERINA_QUERY_PKG_ID, "nextRecord");
                record.put(VALUE_FIELD, value);
                return record;
            }
        } catch (QueryException e) {
            return e.getError();
        }

        return null;
    }
}
