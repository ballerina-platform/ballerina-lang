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

package org.ballerinalang.nats.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the utils which would be used for NATS_URL_PREFIX.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * message which will be propagated.
     */
    private static final String MESSAGE = "message";

    public static void throwBallerinaException(String message, Context context, Throwable throwable) {
        LOGGER.error(message, throwable);
        throw new BallerinaException(message + " " + throwable.getMessage(), throwable, context);
    }

    public static Struct getReceiverObject(Context context) {
        return BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
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
     * Extract NATS_URL_PREFIX Resource from the Ballerina Service.
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
