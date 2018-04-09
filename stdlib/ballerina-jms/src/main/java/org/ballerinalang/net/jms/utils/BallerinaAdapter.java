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
 *
 */

package org.ballerinalang.net.jms.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Adapter class use used to bridge the connector native codes and Ballerina API.
 */
public class BallerinaAdapter {

    private BallerinaAdapter() {
    }

    public static Struct getReceiverObject(Context context) {
        return BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
    }

    public static <T> T getNativeObject(Struct struct, String objectId, Class<T> objectClass, Context context) {
        Object messageNativeData = struct.getNativeData(objectId);
        return verifyNativeObject(context, struct.getName(), objectClass, messageNativeData);
    }

    public static <T> T getNativeObject(BStruct struct, String objectId, Class<T> objectClass, Context context) {
        Object messageNativeData = struct.getNativeData(objectId);
        return verifyNativeObject(context, struct.getType().getName(), objectClass, messageNativeData);
    }

    private static <T> T verifyNativeObject(Context context, String structName, Class<T> objectClass, Object
            nativeData) {
        if (!objectClass.isInstance(nativeData)) {
            throw new BallerinaException(structName + " is not properly initialized.", context);
        }
        return objectClass.cast(nativeData);
    }
}
