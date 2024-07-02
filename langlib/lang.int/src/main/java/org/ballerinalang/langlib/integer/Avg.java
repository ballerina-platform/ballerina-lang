/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.values.BDecimal;

/**
 * Native implementation of lang.int:avg(int, int...).
 *
 * @since 2201.6.0
 */
public final class Avg {

    private Avg() {
    }

    public static BDecimal avg(long n, long[] ns) {
        BDecimal sum = BDecimal.valueOf(n);
        for (long l : ns) {
            sum = sum.add(BDecimal.valueOf(l));
        }
        return sum.divide(BDecimal.valueOf(ns.length + 1));
    }
}
