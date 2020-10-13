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

import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.BValueCreator;
import io.ballerina.jvm.api.ValueCreator;
import io.ballerina.jvm.api.values.BMap;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.scheduling.Strand;

import java.util.UUID;

import static io.ballerina.jvm.util.BLangConstants.BALLERINA_RUNTIME_PKG_ID;

/**
 * Extern function to get invocation context record.
 *
 * @since 0.970.0
 */
public class GetInvocationContext {

    public static BMap<BString, Object> getInvocationContext() {
        return getInvocationContextRecord(Scheduler.getStrand());
    }

    private static final String RUNTIME_INVOCATION_CONTEXT_PROPERTY = "RuntimeInvocationContext";
    private static final String STRUCT_TYPE_INVOCATION_CONTEXT = "InvocationContext";
    private static final String INVOCATION_ID_KEY = "id";
    private static final String INVOCATION_ATTRIBUTES = "attributes";
    private static final ValueCreator valueCreator = ValueCreator.getValueCreator(BALLERINA_RUNTIME_PKG_ID.toString());

    private static BMap<BString, Object> getInvocationContextRecord(Strand strand) {
        BMap<BString, Object> invocationContext =
                (BMap<BString, Object>) strand.getProperty(RUNTIME_INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            invocationContext = initInvocationContext();
            strand.setProperty(RUNTIME_INVOCATION_CONTEXT_PROPERTY, invocationContext);
        }
        return invocationContext;
    }

    private static BMap<BString, Object> initInvocationContext() {
        BMap<BString, Object> invocationContextInfo =
                valueCreator.createRecordValue(STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        invocationContextInfo.put(BStringUtils.fromString(INVOCATION_ID_KEY),
                                  BStringUtils.fromString(invocationId.toString()));
        invocationContextInfo.put(BStringUtils.fromString(INVOCATION_ATTRIBUTES), BValueCreator.createMapValue());
        return invocationContextInfo;
    }
}
