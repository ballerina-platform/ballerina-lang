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
import java.util.Date;

/**
 * timestampInMilliseconds() / timestampInMilliseconds(dateValue,dateFormat)/timestampInMilliseconds(dateValue)
 * Returns System time in milliseconds.
 * dateValue - value of date. eg: "2014-11-11 13:23:44.657", "2014-11-11" , "13:23:44.657"
 * dateFormat - Date format of the provided date value. eg: yyyy-MM-dd HH:mm:ss.SSS
 * Accept Type(s) for timestampInMilliseconds(dateValue,dateFormat):
 *         dateValue : STRING
 *         dateFormat : STRING
 * Return Type(s): LONG
 */
public class TimestampInMillisecondsFunctionExtension extends FunctionExecutor {

    private static final Logger log = Logger.getLogger(TimestampInMillisecondsFunctionExtension.class);
    private Attribute.Type returnType = Attribute.Type.LONG;
    private boolean useDefaultDateFormat = false;
    private String dateFormat = null;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length > 0) {
            if (attributeExpressionExecutors.length == 2) {

                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                            "time:timestampInMilliseconds(dateValue,dateFormat) function,required " + Attribute.Type.STRING
                            +" but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                            "time:timestampInMilliseconds(dateValue,dateFormat) function, " + "required " + Attribute.Type.STRING
                            +" but found " + attributeExpressionExecutors[1].getReturnType().toString());
                }
            } else if(attributeExpressionExecutors.length == 1) {

                if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                    throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "time:timestampInMilliseconds(dateValue,dateFormat) function, " + "required " + Attribute.Type.STRING +
                        "but found " + attributeExpressionExecutors[0].getReturnType().toString());
                }
                useDefaultDateFormat = true;
                dateFormat = TimeExtensionConstants.EXTENSION_TIME_DEFAULT_DATE_FORMAT;
            } else {
                throw new ExecutionPlanValidationException("Invalid no of arguments passed to time:timestampInMilliseconds" +
                        "(dateValue,dateFormat) function, " +"required 2, but found " + attributeExpressionExecutors
                        .length);
            }

        }
    }

    @Override
    protected Object execute(Object[] data) {

        long returnValue;

        if (data.length == 2){
            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to time:timestampInMilliseconds(dateValue," +
                        "dateFormat) function" + ". First argument cannot be null");
            }
            if(!useDefaultDateFormat) {
                if (data[1] == null) {
                    throw new ExecutionPlanRuntimeException("Invalid input given to time:timestampInMilliseconds(dateValue," +
                            "dateFormat) function" + ". First argument cannot be null");
                }
                dateFormat = (String) data[1];
            }
            String source = (String) data[0];
            FastDateFormat userSpecificFormat = FastDateFormat.getInstance(dateFormat);
            try {
                Date date = userSpecificFormat.parse(source);
                returnValue = date.getTime();
            } catch (ParseException e) {
                String errorMsg = "Provided format " + dateFormat + " does not match with the timestamp " + source + e
                        .getMessage();
                throw new ExecutionPlanRuntimeException(errorMsg,e);
            }
            return returnValue;
        } else {
            throw new ExecutionPlanRuntimeException("Invalid set of arguments" + data.length + " given to "+
                    "time:timestampInMilliseconds(dateValue,dateFormat) function. Only two arguments can be provided. ");
        }

    }

    @Override
    protected Object execute(Object data) {
        long returnValue;
        if(data == null){
            return System.currentTimeMillis();
        }
        String source = (String) data;
        FastDateFormat userSpecificFormat = FastDateFormat.getInstance(dateFormat);
        try {
            Date date = userSpecificFormat.parse(source);
            returnValue = date.getTime();
        } catch (ParseException e) {
            String errorMsg = "Provided format " + dateFormat + " does not match with the timestamp " + source + e
                    .getMessage();
            throw new ExecutionPlanRuntimeException(errorMsg,e);
        }
        return returnValue;
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
