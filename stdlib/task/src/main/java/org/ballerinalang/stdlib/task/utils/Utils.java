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
 */
package org.ballerinalang.stdlib.task.utils;

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
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_DAYS_OF_MONTH;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_DAYS_OF_WEEK;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_HOURS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_MINUTES;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_MONTHS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_SECONDS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_YEAR;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RECORD_APPOINTMENT_DATA;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RESOURCE_ON_TRIGGER;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_ERROR_CODE;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_ERROR_MESSAGE;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_ERROR_RECORD;
import static org.quartz.CronExpression.isValidExpression;

/**
 * Utility functions used in ballerina task module.
 *
 * @since 0.995.0
 */
public class Utils {

    private static BError createError(Context context, String message) {
        BMap<String, BValue> taskErrorRecord = createTaskErrorRecord(context);
        taskErrorRecord.put(TASK_ERROR_MESSAGE, new BString(message));
        return BLangVMErrors.createError(context, true, BTypes.typeError, TASK_ERROR_CODE, taskErrorRecord);
    }

    private static BMap<String, BValue> createTaskErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, PACKAGE_STRUCK_NAME, TASK_ERROR_RECORD);
    }

    public static void setError(Context context, String message) {
        context.setReturnValues(createError(context, message));
    }

    public static String getCronExpressionFromAppointmentRecord(BValue record) throws SchedulingException {
        String cronExpression;
        if (RECORD_APPOINTMENT_DATA.equals(record.getType().getName())) {
            cronExpression = buildCronExpression((BMap) record);
            if (!isValidExpression(cronExpression)) {
                throw new SchedulingException("AppointmentData \"" + record.stringValue() + "\" is invalid.");
            }
        } else {
            cronExpression = record.stringValue();
            if (!isValidExpression(cronExpression)) {
                throw new SchedulingException("Cron Expression \"" + cronExpression + "\" is invalid.");
            }
        }
        return cronExpression;
    }

    // Following code is reported as duplicates since all the lines doing same function call.
    private static String buildCronExpression(BMap<String, BValue> record) {
        String cronExpression = getStringFieldValue(record, FIELD_SECONDS) + " " +
                getStringFieldValue(record, FIELD_MINUTES) + " " +
                getStringFieldValue(record, FIELD_HOURS) + " " +
                getStringFieldValue(record, FIELD_DAYS_OF_MONTH) + " " +
                getStringFieldValue(record, FIELD_MONTHS) + " " +
                getStringFieldValue(record, FIELD_DAYS_OF_WEEK) + " " +
                getStringFieldValue(record, FIELD_YEAR);
        return cronExpression.trim();
    }

    private static String getStringFieldValue(BMap<String, BValue> struct, String fieldName) {
        if (FIELD_DAYS_OF_MONTH.equals(fieldName) && Objects.isNull(struct.get(FIELD_DAYS_OF_MONTH))) {
            return "?";
        } else if (Objects.nonNull(struct.get(fieldName))) {
            return struct.get(fieldName).stringValue();
        } else {
            return "*";
        }
    }

    /*
     * TODO: Runtime validation is done as compiler plugin does not work right now.
     *       When compiler plugins can be run for the resources without parameters, this will be redundant.
     *       Issue: https://github.com/ballerina-platform/ballerina-lang/issues/14148
     */
    public static void validateService(Service service) throws BLangRuntimeException {
        Resource[] resources = service.getResources();
        if (resources.length != 1) {
            throw new BLangRuntimeException("Invalid number of resources found in service \'" + service.getName()
                    + "\'. Task service should include only one resource.");
        }
        Resource resource = resources[0];

        if (RESOURCE_ON_TRIGGER.equals(resource.getName())) {
            validateOnTriggerResource(resource.getResourceInfo());
        } else {
            throw new BLangRuntimeException("Invalid resource function found: " + resource.getName()
                    + ". Expected: \'" + RESOURCE_ON_TRIGGER + "\'.");
        }
    }

    private static void validateOnTriggerResource(FunctionInfo functionInfo) {
        if (functionInfo.getRetParamTypes().length != 1 || functionInfo.getRetParamTypes()[0] != BTypes.typeNull) {
            throw new BLangRuntimeException("Invalid resource function signature: \'"
                    + RESOURCE_ON_TRIGGER + "\' should not return a value.");
        }
    }
}
