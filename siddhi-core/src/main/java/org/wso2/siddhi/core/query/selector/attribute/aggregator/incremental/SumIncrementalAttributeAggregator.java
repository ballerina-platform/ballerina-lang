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
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
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
public class SumIncrementalAttributeAggregator extends IncrementalAttributeAggregator {

    private Attribute[] baseAttributes;
    private Expression[] baseAttributesInitialValues;
    private Attribute.Type returnType;

    @Override
    public void init(String attributeName, Attribute.Type attributeType) {
        Attribute sum;
        Expression sumInitialValue;

        if (attributeType.equals(Attribute.Type.FLOAT) || attributeType.equals(Attribute.Type.DOUBLE)) {
            sum = new Attribute("_SUM_".concat(attributeName), Attribute.Type.DOUBLE);
            sumInitialValue = Expression.function("convert", Expression.variable(attributeName),
                    Expression.value("double"));
            returnType = Attribute.Type.DOUBLE;
        } else if (attributeType.equals(Attribute.Type.INT) || attributeType.equals(Attribute.Type.LONG)) {
            sum = new Attribute("_SUM_".concat(attributeName), Attribute.Type.LONG);
            sumInitialValue = Expression.function("convert", Expression.variable(attributeName),
                    Expression.value("long"));
            returnType = Attribute.Type.LONG;
        } else {
            throw new SiddhiAppRuntimeException(
                    "Sum aggregation cannot be executed on " + "attribute type " + attributeType.toString());
        }
        this.baseAttributes = new Attribute[]{sum};
        this.baseAttributesInitialValues = new Expression[]{sumInitialValue}; // Original attribute names
        // used for initial values, since those would be executed using original meta

        assert baseAttributes.length == baseAttributesInitialValues.length;
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
    public Attribute[] getBaseAttributes() {
        return this.baseAttributes;
    }

    @Override
    public Expression[] getBaseAttributeInitialValues() {
        return this.baseAttributesInitialValues;
    }

    @Override
    public Expression[] getBaseAggregators() {
        Expression sumAggregator = Expression.function("sum",
                Expression.variable(getBaseAttributes()[0].getName()));
        return new Expression[]{sumAggregator};
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
