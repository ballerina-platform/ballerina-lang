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

public class MinForeverFunctionExecutor extends FunctionExecutor {

    private MinForeverFunctionExecutor minForeverFunctionExecutor;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("MinForever function has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                minForeverFunctionExecutor = new MinForeverFunctionExecutorFloat();
                break;
            case INT:
                minForeverFunctionExecutor = new MinForeverFunctionExecutorInt();
                break;
            case LONG:
                minForeverFunctionExecutor = new MinForeverFunctionExecutorLong();
                break;
            case DOUBLE:
                minForeverFunctionExecutor = new MinForeverFunctionExecutorDouble();
                break;
            default:
                throw new OperationNotSupportedException("MinForever not supported for " + type);
        }

    }

    @Override
    protected Object execute(Object[] data) {
        return null;
    }

    @Override
    protected Object execute(Object data) {
        return minForeverFunctionExecutor.execute(data);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return minForeverFunctionExecutor.getReturnType();
    }

    @Override
    public Object[] currentState() {
        return minForeverFunctionExecutor.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        minForeverFunctionExecutor.restoreState(state);
    }


    class MinForeverFunctionExecutorLong extends MinForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.LONG;
        private long minValueUntilNow = Long.MAX_VALUE;

        @Override
        protected Object execute(Object data) {
            long currentValue = Long.parseLong(data.toString());
            if (minValueUntilNow > currentValue) {
                minValueUntilNow = currentValue;
            }
            return minValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            minValueUntilNow = (Long) state[0];
        }
    }

    class MinForeverFunctionExecutorInt extends MinForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.INT;
        private int minValueUntilNow = Integer.MAX_VALUE;

        @Override
        protected Object execute(Object data) {
            int currentValue = Integer.parseInt(data.toString());
            if (minValueUntilNow > currentValue) {
                minValueUntilNow = currentValue;
            }
            return minValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            minValueUntilNow = (Integer) state[0];
        }
    }

    class MinForeverFunctionExecutorDouble extends MinForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double minValueUntilNow = Double.MAX_VALUE;

        @Override
        protected Object execute(Object data) {
            double currentValue = Double.parseDouble(data.toString());
            if (minValueUntilNow > currentValue) {
                minValueUntilNow = currentValue;
            }
            return minValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            minValueUntilNow = (Double) state[0];
        }
    }


    class MinForeverFunctionExecutorFloat extends MinForeverFunctionExecutor {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private float minValueUntilNow = Float.MAX_VALUE;

        @Override
        protected Object execute(Object data) {
            float currentValue = Float.parseFloat(data.toString());
            if (minValueUntilNow > currentValue) {
                minValueUntilNow = currentValue;
            }
            return minValueUntilNow;
        }

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValueUntilNow};
        }

        @Override
        public void restoreState(Object[] state) {
            minValueUntilNow = (Float) state[0];
        }
    }

}
