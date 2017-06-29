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
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Sum incremental aggregation
 */
@Extension(
        name = "sum",
        namespace = "incrementalAggregator",
        description = "TBD",
        examples = @Example(
                syntax = "TBD",
                description = "TBD"
        )
)
public class SumIncrementalAttributeAggregator extends CompositeAggregator {

    private Attribute[] incrementalAttributes;
    private Expression[] initialValues;

    @Override
    public void init(String attributeName, Attribute.Type attributeType) {
        Attribute sum;
        Expression sumInitialValue;

        if (attributeType.equals(Attribute.Type.FLOAT) || attributeType.equals(Attribute.Type.DOUBLE)) {
            sum = new Attribute("_SUM_".concat(attributeName), Attribute.Type.DOUBLE);
            sumInitialValue = Expression.function("convert", Expression.variable(attributeName),
                    Expression.value("double"));

        } else if (attributeType.equals(Attribute.Type.INT) || attributeType.equals(Attribute.Type.LONG)) {
            sum = new Attribute("_SUM_".concat(attributeName), Attribute.Type.LONG);
            sumInitialValue = Expression.function("convert", Expression.variable(attributeName),
                    Expression.value("long"));

        } else {
            throw new ExecutionPlanRuntimeException(
                    "Sum aggregation cannot be executed on " + "attribute type " + attributeType.toString());
        }
        this.incrementalAttributes = new Attribute[] { sum };
        this.initialValues = new Expression[] { sumInitialValue }; // Original attribute names
        // used for initial values, since those would be executed using original meta

        assert incrementalAttributes.length == initialValues.length;
    }

    @Override
    public Object aggregate(Object... results) {
        if (results == null) {
            throw new ArithmeticException("Cannot calculate sum since sum base aggregate expected "
                    + "for calculation. Expected 1 base value (sum). However, received no base values");
        }
        if (results.length != 2) {
            throw new ArithmeticException("Cannot calculate sum since sum base aggregate expected "
                    + "for calculation. Expected 1 base value (sum). However, received " + results.length
                    + " values");
        }
        return results[0];
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
        return new Expression[] { sumAggregator };
    }
}
