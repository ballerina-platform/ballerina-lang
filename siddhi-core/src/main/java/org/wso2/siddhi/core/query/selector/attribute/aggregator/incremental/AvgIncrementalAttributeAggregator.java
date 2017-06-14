/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.Constant;

public class AvgIncrementalAttributeAggregator implements CompositeAggregator {

    private Attribute[] incrementalAttributes;
    private Expression[] initialValues;

    public AvgIncrementalAttributeAggregator(Attribute attribute) {
        // Send the relevant attribute to this
        Attribute sum;
        Attribute count;
        Expression sumInitialValue;
        Expression countInitialValue;

        if (attribute.getType().equals(Attribute.Type.FLOAT) || attribute.getType().equals(Attribute.Type.DOUBLE)) {
            sum = new Attribute("_SUM_".concat(attribute.getName()), Attribute.Type.DOUBLE);
            sumInitialValue = Expression.function("convert", Expression.variable(attribute.getName()),
                    Expression.value("double"));

        } else if (attribute.getType().equals(Attribute.Type.INT) || attribute.getType().equals(Attribute.Type.LONG)) {
            sum = new Attribute("_SUM_".concat(attribute.getName()), Attribute.Type.LONG);
            sumInitialValue = Expression.function("convert", Expression.variable(attribute.getName()),
                    Expression.value("long"));

        } else {
            throw new ExecutionPlanRuntimeException(
                    "Average aggregation cannot be executed on " + "attribute type " + attribute.getType().toString());
        }

        // Since we set the initial value of count, we can simply set it as long
        // However, since count is summed internally (in avg incremental calculation),
        // ensure that either double or long is used here (since return value of sum is long or
        // double. Long is chosen here)
        count = new Attribute("_COUNT_".concat(attribute.getName()), Attribute.Type.LONG);
        countInitialValue = Expression.value(1L);

        this.incrementalAttributes = new Attribute[]{sum, count};
        this.initialValues = new Expression[]{sumInitialValue, countInitialValue}; //Original attribute names
        // used for initial values, since those would be executed using original meta

        if (!((incrementalAttributes.length == initialValues.length)
                && (initialValues.length == getIncrementalAggregators().length))) {
            // TODO: 6/10/17 This is an error in implementation logic. What needs to be done?
            // For each incremental attribute, an initial value and base incremental aggregator must be defined
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); // TODO: 6/9/17 do we need this?
    }

    public Attribute.Type getType() {
        return Attribute.Type.DOUBLE; // TODO: 6/9/17 do we need this?
    }

    public Object aggregate(Object... results) {
        if (results == null || results.length != 2) {
            // TODO: 3/3/17 exception
        }
        Double sum = (Double) results[0];
        Double count = (Double) results[1];
        if (count != 0) {
            return sum / count;
        } else {
            throw new ArithmeticException("Cannot calculate average since event count is 0");
        }
    }

    @Override
    public Attribute[] getIncrementalAttributes() {
        return this.incrementalAttributes;
    }

    @Override
    public Expression[] getIncrementalAttributeInitialValues() {
        return this.initialValues;
    }

    @Override
    public Expression[] getIncrementalAggregators() {
        Expression sumAggregator = Expression.function("sum",
                Expression.variable(getIncrementalAttributes()[0].getName()));
        Expression countAggregator = Expression.function("sum",
                Expression.variable(getIncrementalAttributes()[1].getName()));
        return new Expression[]{sumAggregator, countAggregator};
    }
}
