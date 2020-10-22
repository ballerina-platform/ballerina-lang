/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.observe.nativeimpl;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.observability.metrics.Counter;

/**
 * This is the native initialize function that's getting called when instantiating the Counter object.
 *
 * @since 0.980.0
 */
public class CounterInitialize {

    public static void initialize(BObject counterObj) {
        Counter counter = Counter.builder(counterObj.get(ObserveNativeImplConstants.NAME_FIELD).toString())
                .description(counterObj.get(ObserveNativeImplConstants.DESCRIPTION_FIELD).toString())
                .tags(Utils.toStringMap((BMap) counterObj.get(ObserveNativeImplConstants.TAGS_FIELD)))
                .build();
        counterObj.addNativeData(ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY, counter);
    }
}
