/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.executor.function;


import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

public class MaxForeverFunctionExecutor extends FunctionExecutor {

    private MaxForeverFunctionExecutor maxForeverFunctionExecutor;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("MaxForever function has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                maxForeverFunctionExecutor = new MaxForeverFunctionExecutorFloat();
                break;
            case INT:
                maxForeverFunctionExecutor = new MaxForeverFunctionExecutorInt();
                break;
            case LONG:
                maxForeverFunctionExecutor = new MaxForeverFunctionExecutorLong();
                break;
            case DOUBLE:
                maxForeverFunctionExecutor = new MaxForeverFunctionExecutorDouble();
                break;
            default:
                throw new OperationNotSupportedException("MaxForever not supported for " + type);
        }

    }

    @Override
    protected Object execute(Object[] data) {
        return null;
    }

    @Override
    protected Object execute(Object data) {
        return maxForeverFunctionExecutor.execute(data);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return maxForeverFunctionExecutor.getReturnType();
    }

    @Override
    public Object[] currentState() {
        return maxForeverFunctionExecutor.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        maxForeverFunctionExecutor.restoreState(state);
    }


    class MaxForeverFunctionExecutorLong extends MaxForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.LONG;
        private long maxValueUntilNow = Long.MIN_VALUE;

        @Override
        protected Object execute(Object data) {
            long currentValue = Long.parseLong(data.toString());
            if (maxValueUntilNow < currentValue) {
                maxValueUntilNow = currentValue;
            }
            return maxValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValueUntilNow = (Long) state[0];
        }
    }

    class MaxForeverFunctionExecutorInt extends MaxForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.INT;
        private int maxValueUntilNow = Integer.MIN_VALUE;

        @Override
        protected Object execute(Object data) {
            int currentValue = Integer.parseInt(data.toString());
            if (maxValueUntilNow < currentValue) {
                maxValueUntilNow = currentValue;
            }
            return maxValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValueUntilNow = (Integer) state[0];
        }
    }

    class MaxForeverFunctionExecutorDouble extends MaxForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double maxValueUntilNow = Double.MIN_VALUE;

        @Override
        protected Object execute(Object data) {
            double currentValue = Double.parseDouble(data.toString());
            if (maxValueUntilNow < currentValue) {
                maxValueUntilNow = currentValue;
            }
            return maxValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValueUntilNow = (Double) state[0];
        }
    }


    class MaxForeverFunctionExecutorFloat extends MaxForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private float maxValueUntilNow = Float.MIN_VALUE;

        @Override
        protected Object execute(Object data) {
            float currentValue = Float.parseFloat(data.toString());
            if (maxValueUntilNow < currentValue) {
                maxValueUntilNow = currentValue;
            }
            return maxValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValueUntilNow = (Float) state[0];
        }
    }

}
