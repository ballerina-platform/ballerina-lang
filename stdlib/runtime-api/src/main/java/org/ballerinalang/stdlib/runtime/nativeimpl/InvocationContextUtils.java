/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ValueCreator;

import java.util.UUID;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG_ID;

/**
 * This class contains the common constants and methods required for invocation context processing.
 *
 * @since 0.970.0
 */
public class InvocationContextUtils {

    private static final String INVOCATION_CONTEXT_PROPERTY = "InvocationContext";
    private static final String STRUCT_TYPE_INVOCATION_CONTEXT = "InvocationContext";
    private static final String INVOCATION_ID_KEY = "id";
    private static final String INVOCATION_ATTRIBUTES = "attributes";
    private static final ValueCreator valueCreator = ValueCreator.getValueCreator(BALLERINA_RUNTIME_PKG_ID.toString());

    public static MapValue<String, Object> getInvocationContextRecord(Strand strand) {
        MapValue<String, Object> invocationContext =
                (MapValue<String, Object>) strand.getProperty(InvocationContextUtils.INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            invocationContext = initInvocationContext(strand);
            strand.setProperty(InvocationContextUtils.INVOCATION_CONTEXT_PROPERTY, invocationContext);
        }
        return invocationContext;
    }

    private static MapValue<String, Object> initInvocationContext(Strand strand) {
        MapValue<String, Object> invocationContextInfo = valueCreator.createRecordValue(STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        invocationContextInfo.put(INVOCATION_ID_KEY, invocationId.toString());
        invocationContextInfo.put(INVOCATION_ATTRIBUTES, new MapValueImpl());
        return invocationContextInfo;
    }
}
