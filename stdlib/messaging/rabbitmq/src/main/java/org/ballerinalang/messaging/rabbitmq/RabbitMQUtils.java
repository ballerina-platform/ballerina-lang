/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class used to bridge the RabbitMQ connector's native code and the Ballerina API.
 *
 * @since 0.995.0
 */
public class RabbitMQUtils {

    public static ErrorValue returnErrorValue(String errorMessage) {
        return BallerinaErrors.createError(RabbitMQConstants.RABBITMQ_ERROR_CODE,
                populateRabbitMQErrorRecord(errorMessage));
    }

    public static boolean checkIfInt(Object object) {
        return TypeChecker.getType(object).getTag() == TypeTags.INT_TAG;
    }

    public static boolean checkIfBoolean(Object object) {
        return TypeChecker.getType(object).getTag() == TypeTags.BOOLEAN_TAG;
    }

    public static boolean checkIfString(Object object) {
        return TypeChecker.getType(object).getTag() == TypeTags.STRING_TAG;
    }

    private static MapValue populateRabbitMQErrorRecord(String msg) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(RabbitMQConstants.RABBITMQ_ERROR_MESSAGE, msg);
        return BallerinaValues.createRecordValue(RabbitMQConstants.PACKAGE_ID_RABBITMQ,
                RabbitMQConstants.RABBITMQ_ERROR_DETAILS, valueMap);
    }

    static ArrayList<ObjectValue> addToList(ArrayList<ObjectValue> arrayList, ObjectValue objectValue) {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        arrayList.add(objectValue);
        return arrayList;
    }

    /**
     * Removes a given element from the provided array list and returns the resulting list.
     *
     * @param arrayList   The original list
     * @param objectValue Element to be removed
     * @return Resulting list after removing the element
     */
    public static ArrayList<ObjectValue> removeFromList(ArrayList<ObjectValue> arrayList, ObjectValue objectValue) {
        if (arrayList != null) {
            arrayList.remove(objectValue);
        }
        return arrayList;
    }

    private RabbitMQUtils() {
    }
}
