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
package org.ballerinalang.stdlib.task;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_CRON_EXPRESSION;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_DAYS_OF_MONTH;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_DAYS_OF_WEEK;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_HOURS;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_MINUTES;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_MONTHS;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_SECONDS;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_YEAR;
import static org.ballerinalang.stdlib.task.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.TaskConstants.TASK_ERROR_CODE;
import static org.ballerinalang.stdlib.task.TaskConstants.TASK_ERROR_MESSAGE;
import static org.ballerinalang.stdlib.task.TaskConstants.TASK_ERROR_RECORD;

/**
 * Utility functions used in ballerina task module
 */
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
        String cronExpression = "";
        if (Objects.nonNull(record.get(FIELD_NAME_CRON_EXPRESSION))) {
            cronExpression = record.get(FIELD_NAME_CRON_EXPRESSION).stringValue();
        } else {
            cronExpression = buildCronExpression(record);
        }
        return cronExpression;
    }

    // Following code is reported as duplicates since all the lines doing same function call.
    @SuppressWarnings("Duplicates")
    private static String buildCronExpression(BMap<String, BValue> record) {
        StringBuilder cronExpression = new StringBuilder();
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_SECONDS)).append(" ");
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_MINUTES)).append(" ");
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_HOURS)).append(" ");
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_DAYS_OF_MONTH)).append(" ");
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_MONTHS)).append(" ");
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_DAYS_OF_WEEK)).append(" ");
        cronExpression.append(getStringFieldValue(record, FIELD_NAME_YEAR));

        String result = cronExpression.toString().trim();
        return result;
    }

    private static String getStringFieldValue(BMap<String, BValue> struct, String fieldName) {
        if (Objects.nonNull(struct.get(fieldName))) {
            return struct.get(fieldName).stringValue();
        } else {
            return "* ";
        }
    }
}
