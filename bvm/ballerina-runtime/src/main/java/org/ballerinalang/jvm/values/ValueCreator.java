/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code ValueCreator} is an API that will be implemented by all the module init classed from jvm codegen.
 * This provides facility for creating runtime record, object, error values.
 *
 * @since 0.995.0
 */
public abstract class ValueCreator {

    private static final Map<String, ValueCreator> runtimeValueCreators = new HashMap<>();

    public static void addValueCreator(String key, ValueCreator valueCreater) {
        if (!key.equals(".") && runtimeValueCreators.containsKey(key)) {
            // silently fail
            return;
        }

        runtimeValueCreators.put(key, valueCreater);
    }

    public static ValueCreator getValueCreator(String key) {
        if (!runtimeValueCreators.containsKey(key)) {
            throw new BallerinaException("Value creator object is not available");
        }

        return runtimeValueCreators.get(key);
    }

    public abstract MapValue<String, Object> createRecordValue(String recordTypeName);

    public abstract ObjectValue createObjectValue(String objectTypeName, Scheduler scheduler, Strand parent,
                                                  Map<String, Object> properties, Object[] args);
}
