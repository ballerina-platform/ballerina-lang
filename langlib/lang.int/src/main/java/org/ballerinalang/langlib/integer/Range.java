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

package org.ballerinalang.langlib.integer;


import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.Iterator;

/**
 * Native implementation of lang.int:range(int, int, int).
 *
 * @since 2201.3.0
 */
public class Range implements Iterable<Long> {

    long rangeStart;
    long rangeEnd;
    long step;

    Range(long rangeStart, long rangeEnd, long step) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.step = step;
    }

    public static Object range(long rangeStart, long rangeEnd, long step) {
        return new Range(rangeStart, rangeEnd, step);
    }

    @Override
    public Iterator iterator() {
        return new Iterator<BMap<BString, Object>>() {
            private long currentIndex = rangeStart;

            @Override
            public boolean hasNext() {
                return currentIndex + step <= rangeEnd;
            }

            @Override
            public BMap<BString, Object> next() {
                if (hasNext()) {
                    currentIndex += step;
                    BMap<BString, Object> map = ValueCreator.createMapValue();
                    map.put(StringUtils.fromString("value"), currentIndex);
                    return ValueCreator.createRecordValue(map);
                }
                return null;
            }
        };
    }
}
