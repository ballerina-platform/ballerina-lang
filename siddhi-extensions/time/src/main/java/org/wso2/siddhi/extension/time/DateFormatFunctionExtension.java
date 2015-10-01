/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * dateFormat(dateValue,dateTargetFormat,dateSourceFormat)/dateFormat(dateValue,dateTargetFormat)/dateFormat(timestampInMilliseconds,dateTargetFormat)
 * Returns a formatted date string.
 * dateValue - value of date. eg: "2014-11-11 13:23:44.657", "2014-11-11" , "13:23:44.657"
 * dateTargetFormat - Date format which need to be converted to. eg: yyyy/MM/dd HH:mm:ss
 * dateSourceFormat - Date format of the provided date value. eg: yyyy-MM-dd HH:mm:ss.SSS
 * timestampInMilliseconds - date value in milliseconds.(from the epoch) eg: 1415712224000L
 * dateTargetFormat - Date format which need to be converted to. eg: yyyy/MM/dd HH:mm:ss
 * Accept Type(s) for dateFormat(dateValue,dateTargetFormat,dateSourceFormat):
 * dateValue : STRING
 * dateTargetFormat : STRING
 * dateSourceFormat : STRING
 * Accept Type(s) for dateFormat(timestampInMilliseconds,dateTargetFormat):
 * timestampInMilliseconds : LONG
 * dateTargetFormat : STRING
 * Return Type(s): STRING
 */
public class DateFormatFunctionExtension extends FunctionExecutor {

    private Attribute.Type returnType = Attribute.Type.STRING;
    private static final Logger log = Logger.getLogger(DateFormatFunctionExtension.class);
    private boolean useDefaultDateFormat = false;
    private String sourceDateFormat = null;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG && attributeExpressionExecutors
                .length == 2) {
            useDefaultDateFormat = true;
            sourceDateFormat = TimeExtensionConstants.EXTENSION_TIME_DEFAULT_DATE_FORMAT;
        }

        if (attributeExpressionExecutors.length == 3) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "time:dateFormat(dateValue,dateTargetFormat,dateSourceFormat) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "time:dateFormat(dateValue,dateTargetFormat,dateSourceFormat) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of " +
                        "time:dateFormat(dateValue,dateTargetFormat,dateSourceFormat) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[2].getReturnType().toString());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if (useDefaultDateFormat) {
                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "time:dateFormat(dateValue,dateTargetFormat,dateSourceFormat) function, " + "required "
                            + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:dateFormat(dateValue,dateTargetFormat,dateSourceFormat) function, " + "required "
                            + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
            } else {
                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "time:dateFormat(timestampInMilliseconds,dateTargetFormat) function, " +
                            "" + "required " + Attribute.Type.LONG +
                            " but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:dateFormat(timestampInMilliseconds,dateTargetFormat) function, " +
                            "" + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
            }
        } else {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to dateFormat() function, " +
                    "required 2 or 3, but found " + attributeExpressionExecutors.length);
        }

    }

    @Override
    protected Object execute(Object[] data) {

        Date userSpecifiedSourceDate;


        if (data.length == 3 || useDefaultDateFormat) {

            String sourceDate = null;
            String targetDataFormat;
            FastDateFormat userSpecifiedSourceFormat;

            try {
                if (data[0] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to time:dateFormat(dateValue," +
                            "dateTargetFormat,dateSourceFormat) function" + ". First " + "argument cannot be null");
                }
                if (data[1] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to time:dateFormat(dateValue," +
                            "dateTargetFormat,dateSourceFormat) function" + ". Second " + "argument cannot be null");
                }
                if (!useDefaultDateFormat) {
                    if (data[2] == null) {
                        throw new ExecutionPlanRuntimeException("Invalid input given to time:dateFormat(dateValue," +
                                "dateTargetFormat,dateSourceFormat) function" + ". Third " + "argument cannot be null");
                    }
                    sourceDateFormat = (String) data[2];
                }

                sourceDate = (String) data[0];
                targetDataFormat = (String) data[1];
                userSpecifiedSourceFormat = FastDateFormat.getInstance(sourceDateFormat);
                userSpecifiedSourceDate = userSpecifiedSourceFormat.parse(sourceDate);
                // Format the Date to specified Format
                FastDateFormat targetFormat = FastDateFormat.getInstance(targetDataFormat);
                return targetFormat.format(userSpecifiedSourceDate);
            } catch (ParseException e) {
                String errorMsg = "Provided format " + sourceDateFormat + " does not match with the timestamp " +
                        sourceDate + " " + e.getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg, e);
            } catch (ClassCastException e) {
                String errorMsg = "Provided Data type cannot be cast to desired format. " + e.getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg, e);
            }
        } else if (data.length == 2) {

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to dateFormat(timestampInMilliseconds," +
                        "dateTargetFormat) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to dateFormat(timestampInMilliseconds," +
                        "dateTargetFormat) function" + ". Second " + "argument cannot be null");
            }

            String targetDataFormat = null;
            // Format the Date to specified Format
            FastDateFormat targetFormat;
            long dateInMills;
            Calendar calInstance;
            String formattedNewDateValue = null;

            try {

                targetDataFormat = (String) data[1];
                // Format the Date to specified Format
                targetFormat = FastDateFormat.getInstance(targetDataFormat);
                dateInMills = (Long) data[0];
                calInstance = Calendar.getInstance();
                calInstance.setTimeInMillis(dateInMills);
                userSpecifiedSourceDate = calInstance.getTime();
                formattedNewDateValue = targetFormat.format(userSpecifiedSourceDate);
                return formattedNewDateValue;
            } catch (ClassCastException e) {
                String errorMsg = "Provided Data type cannot be cast to desired format. " + e.getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg, e);
            }

        } else {
            throw new ExecutionPlanRuntimeException("Invalid set of arguments given to time:dateFormat() function." +
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
