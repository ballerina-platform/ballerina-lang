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
import java.util.concurrent.TimeUnit;

/**
 * dateDiff(dateValue1,dateFormat1,dateValue2,dateFormat2)/dateDiff(unixTimeStamp1,unixTimeStamp2)
 * Returns time between two dates.
 * Return Type(s): INT
 */
public class DateDifferenceFunctionExtension extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.LONG;
    static final Logger log = Logger.getLogger(DateDifferenceFunctionExtension.class);

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length == 4) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "str:dateDiff(dateValue1,dateFormat1,dateValue2,dateFormat2) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "str:dateDiff(dateValue1,dateFormat1,dateValue2,dateFormat2) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of " +
                        "str:dateDiff(dateValue1,dateFormat1,dateValue2,dateFormat2) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[2].getReturnType().toString());
            }
            if (attributeExpressionExecutors[3].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the fourth argument of " +
                        "str:dateDiff(dateValue1,dateFormat1,dateValue2,dateFormat2) function, " + "required "
                        + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[3].getReturnType().toString());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "str:dateDiff(unixTimeStamp1,unixTimeStamp2) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "str:dateDiff(unixTimeStamp1,unixTimeStamp2) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
        } else {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to str:dateDiff() function, " +
                    "required 2 or 4, but found " + attributeExpressionExecutors.length);
        }

    }

    @Override
    protected Object execute(Object[] data) {

        Calendar firstCalInstance = Calendar.getInstance();
        Calendar secondCalInstance = Calendar.getInstance();

        if (data.length == 4) {
            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(unit,dateExp," +
                        "dateFormat) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(unit,dateExp," +
                        "dateFormat) function" + ". Second " + "argument cannot be null");
            }
            if (data[2] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(unit,dateExp," +
                        "dateFormat) function" + ". Third " + "argument cannot be null");
            }
            if (data[3] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to str:extract(unit,dateExp," +
                        "dateFormat) function" + ". Fourth " + "argument cannot be null");
            }

            String firstDate = (String) data[0];
            String firstDateFormat = (String) data[1];
            String secondDate = (String) data[2];
            String secondDateFormat = (String) data[3];
            FastDateFormat userSpecifiedFirstFormat = FastDateFormat.getInstance(firstDateFormat);
            FastDateFormat userSpecifiedSecondFormat = FastDateFormat.getInstance(secondDateFormat);

            try {
                Date userSpecifiedFirstDate = userSpecifiedFirstFormat.parse(firstDate);
                firstCalInstance.setTime(userSpecifiedFirstDate);
            } catch (ParseException e) {
                String errorMsg =
                        "Provided format " + firstDateFormat + " does not match with the timestamp " + firstDate + e
                                .getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            }

            try {
                Date userSpecifiedSecondDate = userSpecifiedSecondFormat.parse(secondDate);
                secondCalInstance.setTime(userSpecifiedSecondDate);
            } catch (ParseException e) {
                String errorMsg =
                        "Provided format " + secondDateFormat + " does not match with the timestamp " + secondDate + e
                                .getMessage();
                log.error(errorMsg, e);
                throw new ExecutionPlanRuntimeException(errorMsg);
            }

        } else if(data.length == 2){

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to dateDiff(unixTimeStamp1," +
                        "unixTimeStamp1) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to dateDiff(unixTimeStamp1," +
                        "unixTimeStamp1) function" + ". Second " + "argument cannot be null");
            }

            long firstDateInMills = Long.parseLong((String) data[0]) * TimeExtensionConstants.EXTENSION_TIME_THOUSAND;
            long secondDateInMills = Long.parseLong((String) data[1]) * TimeExtensionConstants.EXTENSION_TIME_THOUSAND;
            firstCalInstance.setTimeInMillis(firstDateInMills);
            secondCalInstance.setTimeInMillis(secondDateInMills);
        } else {
            throw new ExecutionPlanRuntimeException("Invalid set of arguments given to str:dateDiff() function." +
                    "Arguments should be either 2 or 4. ");
        }

        long dateDifference = firstCalInstance.getTimeInMillis() - secondCalInstance.getTimeInMillis();
        return TimeUnit.DAYS.convert(dateDifference, TimeUnit.MILLISECONDS);
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
