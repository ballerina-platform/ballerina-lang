/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.internal.values.ObjectValue;

/**
 * This class is used for Java interoperability tests.
 * <p>
 * adds function output form a given value to another value
 * <p>
 * f(a) + f(a+1) + f(a+2) + ... + f(b)
 *
 * @since 1.0.0
 */
public class Accumulator {

    public static long accumulate(Environment env, ObjectValue intFunction, long from, long to) {
        Runtime runtime = env.getRuntime();
        return accumulateI(intFunction, from, to, runtime, new long[1]);
    }

    private static long accumulateI(ObjectValue intFunction, long i, long to, Runtime runtime, long[] relay) {
        BFuture bFuture = runtime.startIsolatedWorker(intFunction, "invoke", null, null, null, i);
        relay[0] += (long) bFuture.get();
        if (i == to) {
            return relay[0];
        } else {
            return accumulateI(intFunction, i + 1, to, runtime, relay);
        }
    }

}
