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
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

public class SumIncrementalAttributeAggregator implements CompositeAggregator {

    private Attribute[] incrementalAttributes;
    private Expression[] initialValues;

    public SumIncrementalAttributeAggregator(Attribute attribute) {
        Attribute sum;
        Expression sumInitialValue;

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
                    "Sum aggregation cannot be executed on " + "attribute type " + attribute.getType().toString());
        }
        this.incrementalAttributes = new Attribute[] { sum };
        this.initialValues = new Expression[] { sumInitialValue }; // Original attribute names
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
        return Attribute.Type.DOUBLE;
    } // TODO: 6/9/17 do we need this?

    public Object aggregate(Object... results) {
        if (results == null || results.length != 1) {
            // TODO: 3/3/17 exception
        }
        Double sum = (Double) results[0];
        return sum;
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
