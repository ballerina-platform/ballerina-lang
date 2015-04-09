/*
 *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.siddhi.extension.time;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.extension.time.util.TimeExtensionConstants;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * dateFormat(dateValue,dateSourceFormat,dateTargetFormat)/dateFormat(unixTimestamp,dateTargetFormat)
 * Returns a formatted date string.
 * Return Type(s): STRING
 */
public class DateFormatFunctionExtension extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.STRING;
    static final Logger log = Logger.getLogger(DateFormatFunctionExtension.class);

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length == 3) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "str:dateFormat(dateValue,dateSourceFormat,dateTargetFormat) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "str:dateFormat(dateValue,dateSourceFormat,dateTargetFormat) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of " +
                        "str:dateFormat(dateValue,dateSourceFormat,dateTargetFormat) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[2].getReturnType().toString());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "str:dateFormat(unixTimestamp,dateTargetFormat) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "str:dateFormat(unixTimestamp,dateTargetFormat) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
        } else {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to dateFormat() function, " +
                    "required 2 or 3, but found " + attributeExpressionExecutors.length);
        }

    }

    @Override
    protected Object execute(Object[] data) {

        Date userSpecifiedSourceDate;

        if (data.length == 3) {
            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:dateFormat(sourceDate," +
                        "dateSourceFormat,dateTargetFormat) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:dateFormat(sourceDate," +
                        "dateSourceFormat,dateTargetFormat) function" + ". Second " + "argument cannot be null");
            }
            if (data[2] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:dateFormat(sourceDate," +
                        "dateSourceFormat,dateTargetFormat) function" + ". Third " + "argument cannot be null");
            }

            String sourceDate = (String) data[0];
            String sourceDateFormat = (String) data[1];
            String targetDataFormat = (String) data[2];

            FastDateFormat userSpecifiedSourceFormat = FastDateFormat.getInstance(sourceDateFormat);

            try {
                userSpecifiedSourceDate = userSpecifiedSourceFormat.parse(sourceDate);
            } catch (ParseException e) {
                String errorMsg = "Provided format " + sourceDateFormat + " does not match with the timestamp " +
                        sourceDate + e.getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            }

            // Format the Date to specified Format
            FastDateFormat targetFormat = FastDateFormat.getInstance(targetDataFormat);
            return targetFormat.format(userSpecifiedSourceDate);
        } else if(data.length == 2){

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to dateFormat(unixTimestamp," +
                        "dateTargetFormat) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to dateFormat(unixTimestamp," +
                        "dateTargetFormat) function" + ". Second " + "argument cannot be null");
            }

            String targetDataFormat = (String) data[1];
            // Format the Date to specified Format
            FastDateFormat targetFormat = FastDateFormat.getInstance(targetDataFormat);
            long dateInMills = Long.parseLong((String) data[0]) * TimeExtensionConstants.EXTENSION_TIME_THOUSAND;
            Calendar calInstance = Calendar.getInstance();
            calInstance.setTimeInMillis(dateInMills);
            userSpecifiedSourceDate = calInstance.getTime();
            String formattedNewDateValue = targetFormat.format(userSpecifiedSourceDate);

            try {

                long newFormattedDateInUnix = (targetFormat.parse(formattedNewDateValue).getTime() /
                        TimeExtensionConstants.EXTENSION_TIME_THOUSAND);
                return  String.valueOf(newFormattedDateInUnix);
            } catch (ParseException e) {
                String errorMsg = "Provided format " + targetDataFormat + " does not match with the timestamp " +
                        formattedNewDateValue + e.getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            }

        } else {
            throw new ExecutionPlanRuntimeException("Invalid set of arguments given to str:dateFormat() function." +
                    "Arguments should be either 2 or 3. ");
        }
    }

    @Override
    protected Object execute(Object data) {
        return null;//Since the EpochToDateFormat function takes in 2 parameters, this method does not get called. Hence, not implemented.

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[0]; //No need to maintain a state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}
