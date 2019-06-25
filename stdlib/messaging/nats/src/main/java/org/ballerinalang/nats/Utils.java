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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Utilities for producing and consuming via NATS sever.
 */
public class Utils {
    /**
     * Message which will be propagated.
     */
    private static final String MESSAGE = "message";

    /**
     * Constructs a BallerinaException and throw it.
     *
     * @param message   error message which should be included.
     * @param context   context of the ballerina call.
     * @param throwable the exception stacktrace.
     */
    public static void throwBallerinaException(String message, Context context, Throwable throwable) {
        throw new BallerinaException(message + " " + throwable.getMessage(), throwable, context);
    }

    /**
     * Retrieve the receiver object.
     *
     * @param context Ballerina context which should be received.
     * @return the receiver object struct.
     */
    public static BMap<String, BValue> getReceiverObject(Context context) {
        return (BMap<String, BValue>) context.getRefArgument(0);
    }

    /**
     * Creates an error message.
     *
     * @param context context which is invoked.
     * @param errCode the error code.
     * @param errMsg  the cause for the error.
     * @return an error which will be propagated to ballerina user.
     */
    public static BError createError(Context context, String errCode, String errMsg) {
        BMap<String, BValue> ioErrorRecord = BLangConnectorSPIUtil.createBStruct(context, Constants.NATS_PACKAGE,
                Constants.IO_ERROR);
        ioErrorRecord.put(MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, errCode, ioErrorRecord);
    }

    /**
     * Extract NATS Resource from the Ballerina Service.
     *
     * @param service Service instance.
     * @return extracted resource.
     */
    public static Resource extractNATSResource(Service service) {
        Resource[] resources = service.getResources();
        if (resources.length == 0) {
            throw new BallerinaException("No resources found to handle the NATS_URL_PREFIX message in " +
                    service.getName());
        }
        if (resources.length > 1) {
            throw new BallerinaException("More than one resources found in NATS_URL_PREFIX service " + service.getName()
                    + ". NATS_URL_PREFIX Service should only have one resource");
        }
        return resources[0];
    }
}
