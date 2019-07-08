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

import io.nats.client.Message;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

/**
 * Utilities for producing and consuming via NATS sever.
 */
public class Utils {
    /**
     * Message which will be propagated.
     */
    private static final String MESSAGE = "message";


    public static ErrorValue createNatsError(String nuid, String detailedErrorMessage) {
        MapValue<String, Object> errorDetailRecord = BallerinaValues
                .createRecordValue(Constants.NATS_PACKAGE, Constants.NATS_ERROR_DETAIL_RECORD);
        MapValue<String, Object> populatedDetailRecord = BallerinaValues
                .createRecord(errorDetailRecord, nuid, detailedErrorMessage);
        return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, populatedDetailRecord);
    }

    public static ErrorValue createNatsError(String detailedErrorMessage) {
        MapValue<String, Object> errorDetailRecord = BallerinaValues
                .createRecordValue(Constants.NATS_PACKAGE, Constants.NATS_ERROR_DETAIL_RECORD);
        errorDetailRecord.put("message", detailedErrorMessage);
        return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, errorDetailRecord);
    }

    public static ObjectValue getMessageObject(Message message) {
        ObjectValue msgObj;
        if (message != null) {
            ArrayValue msgData = new ArrayValue(message.getData());
            msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE,
                    Constants.NATS_MESSAGE_OBJ_NAME, message.getSubject(), msgData, message.getReplyTo());
        } else {
            ArrayValue msgData = new ArrayValue(new byte[0]);
            msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE,
                    Constants.NATS_MESSAGE_OBJ_NAME, "", msgData, "");
        }
        return msgObj;
    }
}
