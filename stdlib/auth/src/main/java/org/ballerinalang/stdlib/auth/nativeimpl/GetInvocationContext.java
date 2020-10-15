/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.auth.nativeimpl;

import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_AUTH_PKG_ID;

/**
 * Extern function to get auth invocation context record.
 *
 */
public class GetInvocationContext {

    public static BMap<BString, Object> getInvocationContext() {
        return getInvocationContextRecord(Scheduler.getStrand());
    }

    private static final String AUTH_INVOCATION_CONTEXT_PROPERTY = "AuthInvocationContext";
    private static final String RECORD_TYPE_INVOCATION_CONTEXT = "InvocationContext";

    private static BMap<BString, Object> getInvocationContextRecord(Strand strand) {
        BMap<BString, Object> invocationContext =
                (BMap<BString, Object>) strand.getProperty(AUTH_INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            invocationContext = ValueCreator.createRecordValue(BALLERINA_AUTH_PKG_ID, RECORD_TYPE_INVOCATION_CONTEXT);
            strand.setProperty(AUTH_INVOCATION_CONTEXT_PROPERTY, invocationContext);
        }
        return invocationContext;
    }
}
