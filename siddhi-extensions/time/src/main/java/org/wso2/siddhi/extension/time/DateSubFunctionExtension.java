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
 * dateSub(dateValue,expr,unit,dateFormat)/dateSub(dateValue,expr,unit)/dateSub(timestampInMilliseconds,expr,unit)
 * Returns subtracted specified time interval to a date.
 * dateValue - value of date. eg: "2014-11-11 13:23:44.657", "2014-11-11" , "13:23:44.657"
 * unit - Which part of the date format you want to manipulate. eg: "MINUTE" , "HOUR" , "MONTH" , "YEAR" , "QUARTER" ,
 *        "WEEK" , "DAY" , "SECOND"
 * expr - In which amount, selected date format part should be decremented. eg: 2 ,5 ,10 etc
 * dateFormat - Date format of the provided date value. eg: yyyy-MM-dd HH:mm:ss.SSS
 * timestampInMilliseconds - date value in milliseconds.(from the epoch) eg: 1415712224000L
 * Accept Type(s) for dateSub(dateValue,expr,unit,dateFormat):
 *         dateValue : STRING
 *         expr : INT
 *         unit : STRING
 *         dateFormat : STRING
 * Accept Type(s) for dateSub(timestampInMilliseconds, expr,unit):
 *         timestampInMilliseconds : LONG
 *         expr : INT
 *         unit : STRING
 * Return Type(s): STRING
 */
public class DateSubFunctionExtension extends FunctionExecutor {

    private Attribute.Type returnType = Attribute.Type.STRING;
    private static final Logger log = Logger.getLogger(DateSubFunctionExtension.class);
    private boolean useDefaultDateFormat = false;
    private String dateFormat = null;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if(attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG && attributeExpressionExecutors
                .length == 3){
            useDefaultDateFormat = true;
            dateFormat = TimeExtensionConstants.EXTENSION_TIME_DEFAULT_DATE_FORMAT;
        }
        if (attributeExpressionExecutors.length == 4) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "time:dateSub(dateValue,expr,unit,dateFormat) function, " + "required " + Attribute.Type
                        .STRING +" but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.INT) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "time:dateSub(dateValue,expr,unit,dateFormat) function, " + "required " + Attribute.Type.INT +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of " +
                        "time:dateSub(dateValue,expr,unit,dateFormat) function, " + "required " + Attribute.Type
                        .STRING +" but found " + attributeExpressionExecutors[2].getReturnType().toString());
            }
            if (attributeExpressionExecutors[3].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the fourth argument of " +
                        "time:dateSub(dateValue,expr,unit,dateFormat) function, " + "required " + Attribute.Type
                        .STRING +" but found " + attributeExpressionExecutors[3].getReturnType().toString());
            }
        } else if (attributeExpressionExecutors.length == 3) {
            if(useDefaultDateFormat){
                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "time:dateSub(dateValue,expr,unit) function, " + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.INT) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:dateSub(dateValue,expr,unit) function, " + "required " + Attribute.Type.INT +
                            " but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
                if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:dateSub(dateValue,expr,unit) function, " + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[2].getReturnType().toString());
                }
            } else{
                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "time:dateSub(timestampInMilliseconds,expr,unit) function, " +
                            "required " + Attribute.Type.LONG +" but found " + attributeExpressionExecutors[0]
                            .getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.INT) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:dateSub(timestampInMilliseconds,expr,unit) function, " + "required " + Attribute.Type.INT +
                            " but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
                if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:dateSub(timestampInMilliseconds,expr,unit) function, " + "required " + Attribute.Type.STRING +
                            " but found " + attributeExpressionExecutors[2].getReturnType().toString());
                }
            }
        } else {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to time:dateSub() function, " +
                    "required 3 or 4, but found " + attributeExpressionExecutors.length);
        }

    }

    @Override
    protected Object execute(Object[] data) {

        Calendar calInstance = Calendar.getInstance();
        String unit;
        int expression;
        String date = null;
        FastDateFormat formattedDate;

        if (data.length == 4 || useDefaultDateFormat) {
            try {
                if (data[0] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:dateSub(date,expr," +
                            "unit,dateFormat) function" + ". First " + "argument cannot be null");
                }
                if (data[1] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:dateSub(date,expr," +
                            "unit,dateFormat) function" + ". Second " + "argument cannot be null");
                }
                if (data[2] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to str:dateSub(date,expr," +
                            "unit,dateFormat) function" + ". Third " + "argument cannot be null");
                }
                if(!useDefaultDateFormat){
                    if (data[3] == null) {
                        throw new ExecutionPlanRuntimeException("Invalid input given to str:dateSub(date,expr," +
                                "unit,dateFormat) function" + ". Fourth " + "argument cannot be null");
                    }
                    dateFormat = (String) data[3];
                }

                date = (String) data[0];
                expression = (Integer) data[1];
                expression = -expression;
                unit = (String) data[2];
                formattedDate = FastDateFormat.getInstance(dateFormat);
                Date userSpecifiedDate = formattedDate.parse(date);
                calInstance.setTime(userSpecifiedDate);
                getProcessedCalenderInstance(unit, calInstance, expression);
                return formattedDate.format(calInstance.getTime());
            } catch (ParseException e) {
                String errorMsg = "Provided format " + dateFormat + " does not match with the timestamp " + date + e
                        .getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg,e);
            } catch (ClassCastException e){
                String errorMsg ="Provided Data type cannot be cast to desired format. " + e.getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg,e);
            }

        } else if(data.length == 3){

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to time:dateSub(timestampInMilliseconds," +
                        "expr,unit) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to time:dateSub(timestampInMilliseconds," +
                        "expr,unit) function" + ". Second " + "argument cannot be null");
            }
            if (data[2] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to time:dateSub(timestampInMilliseconds," +
                        "expr,unit) function" + ". Third " + "argument cannot be null");
            }

            try {
                long dateInMills = (Long)data[0];
                calInstance.setTimeInMillis(dateInMills);
                unit = (String) data[2];
                expression = (Integer) data[1];
                expression = -expression;
                getProcessedCalenderInstance(unit, calInstance, expression);
                return String.valueOf((calInstance.getTimeInMillis()));
            } catch (ClassCastException e){
                String errorMsg ="Provided Data type cannot be cast to desired format. " + e.getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg,e);
            }
        } else {
            throw new ExecutionPlanRuntimeException("Invalid set of arguments given to time:dateSub() function." +
                    "Arguments should be either 3 or 4. ");
        }
    }

    public Calendar getProcessedCalenderInstance(String unit, Calendar calInstance, int expression){

        unit = unit.toUpperCase();

        if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_YEAR)) {
            calInstance.add(Calendar.YEAR, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_MONTH)) {
            calInstance.add(Calendar.MONTH, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_SECOND)) {
            calInstance.add(Calendar.SECOND, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_MINUTE)) {
            calInstance.add(Calendar.MINUTE, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_HOUR)) {
            calInstance.add(Calendar.HOUR, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_DAY)) {
            calInstance.add(Calendar.DAY_OF_MONTH, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_WEEK)) {
            calInstance.add(Calendar.WEEK_OF_YEAR, expression);

        } else if (unit.equals(TimeExtensionConstants.EXTENSION_TIME_UNIT_QUARTER)) {
            calInstance.add(Calendar.MONTH, expression * 3);
        }

        return calInstance;
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
