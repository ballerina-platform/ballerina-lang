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
import java.util.TimeZone;

/**
 * extract(unit,dateExp,dateFormat)/extract(timestampInMilliseconds,unit)
 * Returns date attributes from a date expression.
 * Return Type(s): INT
 */
public class ExtractAttributesFunctionExtension extends FunctionExecutor {

    static final Logger log = Logger.getLogger(ExtractAttributesFunctionExtension.class);
    Attribute.Type returnType = Attribute.Type.INT;
    Boolean useDefaultDateFormat = false;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if(attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG && attributeExpressionExecutors
                .length == 2){
            useDefaultDateFormat = true;
        }
        if (attributeExpressionExecutors.length == 3) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "str:extract(unit,dateExp,dateFormat) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "str:extract(unit,dateExp,dateFormat) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of " +
                        "str:extract(unit,dateExp,dateFormat) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[2].getReturnType().toString());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if(useDefaultDateFormat){
                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "str:extract(unit,dateExp,dateFormat) function, " + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "str:extract(unit,dateExp,dateFormat) function, " + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
            } else{
                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "str:extract(timestampInMilliseconds,unit) function, " + "required " + Attribute.Type.LONG +
                            " but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "str:extract(timestampInMilliseconds,unit) function, " + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
            }
        } else {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to str:extract() function, " +
                    "required 2 or 3, but found " + attributeExpressionExecutors.length);
        }

    }

    @Override
    protected Object execute(Object[] data) {

        Calendar cal;
        String userFormat = null;
        String source = null;
        String unit;
        if (data.length == 3 || useDefaultDateFormat) {
            try {
                if (data[0] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(unit,dateExp," +
                            "dateFormat) function" + ". First " + "argument cannot be null");
                }
                if (data[1] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(unit,dateExp," +
                            "dateFormat) function" + ". Second " + "argument cannot be null");
                }
                if(!useDefaultDateFormat){
                    if (data[2] == null) {
                        throw new ExecutionPlanRuntimeException("Invalid input given to str:dateAdd(date,expr," +
                                "unit,dateFormat) function" + ". Third " + "argument cannot be null");
                    }
                    userFormat = (String) data[2];
                } else {
                    userFormat = TimeExtensionConstants.EXTENSION_TIME_DEFAULT_DATE_FORMAT;
                }

                cal = Calendar.getInstance();
                FastDateFormat userSpecificFormat;
                source = (String) data[1];
                unit = (String) data[0];
                userSpecificFormat = FastDateFormat.getInstance(userFormat);
                Date userSpecifiedDate = userSpecificFormat.parse(source);
                cal.setTime(userSpecifiedDate);
            } catch (ParseException e) {
                String errorMsg = "Provided format " + userFormat + " does not match with the timestamp " + source + e
                        .getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            } catch (ClassCastException e){
                String errorMsg ="Provided Data type cannot be cast to desired format. " + e.getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            }
        } else {

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(timestampInMilliseconds," +
                        "unit) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(timestampInMilliseconds," +
                        "unit) function" + ". Second " + "argument cannot be null");
            }

            try {
                cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                long millis = (Long)data[0];
                unit = (String) data[1];
                cal.setTimeInMillis(millis);
            } catch (ClassCastException e){
                String errorMsg ="Provided Data type cannot be cast to desired format. " + e.getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            }
        }

        int returnValue = 0;
        unit = unit.toUpperCase();

        if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_YEAR)) {
            returnValue = cal.get(Calendar.YEAR);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_MONTH)) {
            returnValue = (cal.get(Calendar.MONTH) + 1);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_SECOND)) {
            returnValue = cal.get(Calendar.SECOND);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_MINUTE)) {
            returnValue = cal.get(Calendar.MINUTE);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_HOUR)) {
            returnValue = cal.get(Calendar.HOUR_OF_DAY);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_DAY)) {
            returnValue = cal.get(Calendar.DAY_OF_MONTH);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_WEEK)) {
            returnValue = cal.get(Calendar.WEEK_OF_YEAR);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_QUARTER)) {
            returnValue = (cal.get(Calendar.MONTH) / 3) + 1;
        }
        return returnValue;
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
