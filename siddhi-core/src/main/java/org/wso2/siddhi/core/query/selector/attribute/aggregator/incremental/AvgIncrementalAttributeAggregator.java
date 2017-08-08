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
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Average incremental aggregation
 */
@Extension(
        name = "avg",
        namespace = "incrementalAggregator",
        description = "TBD",
        examples = @Example(
                syntax = "TBD",
                description = "TBD"
        )
)
// TODO: 6/20/17 fix annotations
public class AvgIncrementalAttributeAggregator extends IncrementalAttributeAggregator {

    private Attribute[] baseAttributes;
    private Expression[] baseAttributesInitialValues;

    @Override
    public void init(String attributeName, Attribute.Type attributeType) {
        // Send the relevant attribute to this
        Attribute sum;
        Attribute count;
        Expression sumInitialValue;
        Expression countInitialValue;

        SumIncrementalAttributeAggregator sumIncrementalAttributeAggregator = new SumIncrementalAttributeAggregator();
        sumIncrementalAttributeAggregator.init(attributeName, attributeType);
        CountIncrementalAttributeAggregator countIncrementalAttributeAggregator =
                new CountIncrementalAttributeAggregator();
        countIncrementalAttributeAggregator.init(attributeName, attributeType);

        // Only one attribute exists for sum and count
        sum = sumIncrementalAttributeAggregator.getBaseAttributes()[0];
        count = countIncrementalAttributeAggregator.getBaseAttributes()[0];

        // Only one init value exists for sum and count
        sumInitialValue = sumIncrementalAttributeAggregator.getBaseAttributeInitialValues()[0];
        countInitialValue = countIncrementalAttributeAggregator.getBaseAttributeInitialValues()[0];

        this.baseAttributes = new Attribute[]{sum, count};
        this.baseAttributesInitialValues = new Expression[]{sumInitialValue, countInitialValue};

        assert baseAttributes.length == baseAttributesInitialValues.length;
    }

    @Override
    public Object aggregate(Object... results) {
        if (results == null) {
            throw new ArithmeticException("Cannot calculate average since sum and count expected "
                    + "for calculation. Expected 2 base values sum and count. However, received no base values");
        }
        if (results.length != 2) {
            throw new ArithmeticException("Cannot calculate average since sum and count expected "
                    + "for calculation. Expected 2 base values sum and count. However, received " + results.length
                    + " values");
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
        Expression countAggregator = Expression.function("sum",
                Expression.variable(getBaseAttributes()[1].getName()));
        return new Expression[]{sumAggregator, countAggregator};
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.DOUBLE;
    }
}
