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
 * date(dateValue,dateFormat)
 * Returns date part from a date or date/time expression.
 * dateValue - value of date. eg: "2014-11-11 13:23:44.657", "2014-11-11"
 * dateFormat - Date format of the provided date value. eg: yyyy-MM-dd HH:mm:ss.SSS
 * Accept Type(s) for date(dateValue,dateFormat):
 *         dateValue : STRING
 *         dateFormat : STRING
 * Return Type(s): STRING
 */
public class ExtractDateFunctionExtension extends FunctionExecutor {

    private static final Logger log = Logger.getLogger(ExtractDateFunctionExtension.class);
    private Attribute.Type returnType = Attribute.Type.STRING;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length > 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to time:date(dateValue," +
                    "dateFormat) function, " + "required 2, but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                    "time:date(dateValue,dateFormat) function, " + "required " + Attribute.Type.STRING +
                    " but found " + attributeExpressionExecutors[0].getReturnType().toString());
        }
        //User can omit sending the dateFormat thus using a default CEP Time format
        if (attributeExpressionExecutors.length > 0) {
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "time:date(dateValue,dateFormat) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
        }

    }

    @Override
    protected Object execute(Object[] data) {
        String userFormat;
        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to time:date(dateValue," +
                    "dateFormat) function" + ". First " + "argument cannot be null");
        }
        if (data.length > 0) {
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException(
                        "Invalid input given to time:date(dateValue,dateFormat) function" + ". Second " +
                                "argument cannot be null");
            }
            userFormat = (String) data[1];
        } else {
            userFormat = TimeExtensionConstants.EXTENSION_TIME_DEFAULT_DATE_FORMAT;
        }

        String source = null;
        FastDateFormat userSpecificFormat;
        Date userSpecifiedDate;
        try {
            source = (String) data[0];
            userSpecificFormat = FastDateFormat.getInstance(userFormat);
            userSpecifiedDate = userSpecificFormat.parse(source);
            FastDateFormat dataFormat = FastDateFormat.getInstance(TimeExtensionConstants.EXTENSION_TIME_DATE_FORMAT);
            return dataFormat.format(userSpecifiedDate);
        } catch (ParseException e) {
            String errorMsg = "Provided format " + userFormat + " does not match with the timestamp " + source + e
                    .getMessage();
            throw new ExecutionPlanRuntimeException(errorMsg,e);
        } catch (ClassCastException e){
            String errorMsg ="Provided Data type cannot be cast to desired format. " + e.getMessage();
            throw new ExecutionPlanRuntimeException(errorMsg,e);
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
