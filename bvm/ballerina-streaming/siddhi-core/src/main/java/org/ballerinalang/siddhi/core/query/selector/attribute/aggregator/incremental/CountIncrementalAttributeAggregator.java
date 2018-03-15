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
package org.ballerinalang.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.ballerinalang.siddhi.annotation.Example;
import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.annotation.ReturnAttribute;
import org.ballerinalang.siddhi.annotation.util.DataType;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.expression.Expression;

/**
 * {@link IncrementalAttributeAggregator} to calculate count based on an event attribute.
 */
@Extension(
        name = "count",
        namespace = "incrementalAggregator",
        description = "Returns the count of all events, in incremental event processing",
        parameters = {},
        returnAttributes = @ReturnAttribute(
                description = "Returns the event count as a long.",
                type = {DataType.LONG}),
        examples = @Example(
                syntax = " define aggregation cseEventAggregation\n from cseEventStream\n" +
                        " select count() as countEvents,\n aggregate by timeStamp every sec ... hour;",
                description = "count() returns the count of all the events based on their " +
                        "arrival and expiry. The count is calculated for sec, min and hour durations."
        )
)
public class CountIncrementalAttributeAggregator extends IncrementalAttributeAggregator {

    private Attribute[] baseAttributes;
    private Expression[] baseAttributesInitialValues;

    @Override
    public void init(String attributeName, Attribute.Type attributeType) {
        Attribute count;
        Expression countInitialValue;

        // Since we set the initial value of count, we can simply set it as long
        // However, since count is summed internally (in avg incremental calculation),
        // ensure that either double or long is used here (since return value of sum is long or
        // double. Long is chosen here)
        count = new Attribute("AGG_COUNT", Attribute.Type.LONG);
        countInitialValue = Expression.value(1L);

        this.baseAttributes = new Attribute[]{count};
        this.baseAttributesInitialValues = new Expression[]{countInitialValue};

        assert baseAttributes.length == baseAttributesInitialValues.length;
    }

    @Override
    public Expression aggregate() {
        return Expression.variable(baseAttributes[0].getName());
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
        Expression countAggregator = Expression.function("sum",
                Expression.variable(getBaseAttributes()[0].getName()));
        return new Expression[]{countAggregator};
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.LONG;
    }
}
