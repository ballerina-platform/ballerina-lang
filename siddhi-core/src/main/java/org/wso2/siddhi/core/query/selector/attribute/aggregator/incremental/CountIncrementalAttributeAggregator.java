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
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

@Extension(
        name = "count",
        namespace = "incrementalAggregator",
        description = "TBD",
        examples = @Example(
                syntax = "TBD",
                description = "TBD"
        )
)
public class CountIncrementalAttributeAggregator extends CompositeAggregator {

    private Attribute[] incrementalAttributes;
    private Expression[] initialValues;

    @Override
    public void init(String attributeName, Attribute.Type attributeType) {
        Attribute count;
        Expression countInitialValue;

        // Since we set the initial value of count, we can simply set it as long
        // However, since count is summed internally (in avg incremental calculation),
        // ensure that either double or long is used here (since return value of sum is long or
        // double. Long is chosen here)
        count = new Attribute("_COUNT_".concat(attributeName), Attribute.Type.LONG);
        countInitialValue = Expression.value(1L);

        this.incrementalAttributes = new Attribute[] { count };
        this.initialValues = new Expression[] { countInitialValue };

        if (!((incrementalAttributes.length == initialValues.length)
                && (initialValues.length == getIncrementalAggregators().length))) {
            // TODO: 6/10/17 This is an error in implementation logic. What needs to be done?
            // For each incremental attribute, an initial value and base incremental aggregator must be defined
        }
    }

    @Override
    public Object aggregate(Object... results) {
        if (results == null || results.length != 1) {
            // TODO: 3/3/17 exception
        }
        Long count = (Long) results[0];
        return count;
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
        Expression countAggregator = Expression.function("sum",
                Expression.variable(getIncrementalAttributes()[0].getName()));
        return new Expression[] { countAggregator };
    }
}
