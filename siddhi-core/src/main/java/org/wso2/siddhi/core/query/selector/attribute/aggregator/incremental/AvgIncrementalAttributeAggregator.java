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

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.Constant;

@Extension(
        name = "avg",
        namespace = "incrementalAggregator",
        description = "TBD",
        examples = @Example(
                syntax = "TBD",
                description = "TBD"
        )
)// TODO: 6/20/17 fix annotations
public class AvgIncrementalAttributeAggregator extends CompositeAggregator {

    private Attribute[] incrementalAttributes;
    private Expression[] initialValues;

    @Override
    public void init(String attributeName, Attribute.Type attributeType) {
        // Send the relevant attribute to this
        Attribute sum;
        Attribute count;
        Expression sumInitialValue;
        Expression countInitialValue;

        SumIncrementalAttributeAggregator sumIncrementalAttributeAggregator =
                new SumIncrementalAttributeAggregator();
        sumIncrementalAttributeAggregator.init(attributeName, attributeType);
        CountIncrementalAttributeAggregator countIncrementalAttributeAggregator =
                new CountIncrementalAttributeAggregator();
        countIncrementalAttributeAggregator.init(attributeName, attributeType);

        // Only one attribute exists for sum and count
        sum = sumIncrementalAttributeAggregator.getIncrementalAttributes()[0];
        count = countIncrementalAttributeAggregator.getIncrementalAttributes()[0];

        // Only one init value exists for sum and count
        sumInitialValue = sumIncrementalAttributeAggregator.getIncrementalAttributeInitialValues()[0];
        countInitialValue = countIncrementalAttributeAggregator.getIncrementalAttributeInitialValues()[0];


        this.incrementalAttributes = new Attribute[]{sum, count};
        this.initialValues = new Expression[]{sumInitialValue, countInitialValue};

        if (!((incrementalAttributes.length == initialValues.length)
                && (initialValues.length == getIncrementalAggregators().length))) {
            // TODO: 6/10/17 This is an error in implementation logic. What needs to be done?
            // For each incremental attribute, an initial value and base incremental aggregator must be defined
        }
    }

    @Override
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
