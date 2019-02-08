/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ballerinalang.stdlib.task.listener.utils;

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
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_CRON_EXPRESSION;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_DAYS_OF_MONTH;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_DAYS_OF_WEEK;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_HOURS;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_MINUTES;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_MONTHS;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_SECONDS;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_YEAR;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.RESOURCE_ON_TRIGGER;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TASK_ERROR_CODE;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TASK_ERROR_MESSAGE;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TASK_ERROR_RECORD;

/**
 * Utility functions used in ballerina task module.
 */
@SuppressWarnings("Duplicates")
public class Utils {

    public static BError createError(Context context, String message) {
        BMap<String, BValue> taskErrorRecord = createTaskErrorRecord(context);
        taskErrorRecord.put(TASK_ERROR_MESSAGE, new BString(message));
        return BLangVMErrors.createError(context, true, BTypes.typeError, TASK_ERROR_CODE, taskErrorRecord);
    }

    private static BMap<String, BValue> createTaskErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, PACKAGE_STRUCK_NAME, TASK_ERROR_RECORD);
    }

    public static String getCronExpressionFromAppointmentRecord(BMap<String, BValue> record) {
        String cronExpression;
        if (Objects.nonNull(record.get(FIELD_NAME_CRON_EXPRESSION))) {
            cronExpression = record.get(FIELD_NAME_CRON_EXPRESSION).stringValue();
        } else {
            cronExpression = buildCronExpression(record);
        }
        return cronExpression;
    }

    // Following code is reported as duplicates since all the lines doing same function call.
    private static String buildCronExpression(BMap<String, BValue> record) {

        String cronExpression = getStringFieldValue(record, FIELD_NAME_SECONDS) + " " +
                getStringFieldValue(record, FIELD_NAME_MINUTES) + " " +
                getStringFieldValue(record, FIELD_NAME_HOURS) + " " +
                getStringFieldValue(record, FIELD_NAME_DAYS_OF_MONTH) + " " +
                getStringFieldValue(record, FIELD_NAME_MONTHS) + " " +
                getStringFieldValue(record, FIELD_NAME_DAYS_OF_WEEK) + " " +
                getStringFieldValue(record, FIELD_NAME_YEAR);
        return cronExpression.trim();
    }

    private static String getStringFieldValue(BMap<String, BValue> struct, String fieldName) {
        if (Objects.nonNull(struct.get(fieldName))) {
            return struct.get(fieldName).stringValue();
        } else {
            return "* ";
        }
    }

    /*
     * TODO:
     * Runtime validation is done as compiler plugin does not work right now.
     * When compiler plugins can be run for the resources without parameters, this will be redundant.
     */
    public static void validateService(Service service) throws BLangRuntimeException {
        Resource[] resources = service.getResources();
        if (resources.length > 2 || resources.length < 1) {
            throw new BLangRuntimeException("Invalid number of resources found in service " + service.getName()
                    + ". Task service can only included one or two resource functions");
        }

        boolean isOnTriggerFound = false;
        for (Resource resource : resources) {
            if (isOnTriggerResource(resource)) {
                isOnTriggerFound = true;
            }
            validateResource(resource);
        }
        if (!isOnTriggerFound) {
            throw new BLangRuntimeException("Invalid resources found. Service " + service.getName()
                    + " must include resource function: \'" + RESOURCE_ON_TRIGGER + "\'.");
        }
    }

    private static void validateResource(Resource resource) {
        if (!isOnTriggerResource(resource) && !isOnErrorResource(resource)) {
            throw new BLangRuntimeException("Invalid resource function found: " + resource.getName()
                    + ". Expected: \'" + RESOURCE_ON_TRIGGER + "\' or \'" + RESOURCE_ON_ERROR + "\'.");
        }
    }

    private static boolean isOnTriggerResource(Resource resource) {
        return RESOURCE_ON_TRIGGER.equals(resource.getName());
    }

    private static boolean isOnErrorResource(Resource resource) {
        return RESOURCE_ON_ERROR.equals(resource.getName());
    }
}
