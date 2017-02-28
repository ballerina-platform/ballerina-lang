/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.aggregation.TimeSpecifier;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;

public class AggregationDefinition extends AbstractDefinition {

    private InputStream inputStream = null;
    private Selector selector = null;
    private Variable aggregateAttribute = null;
    private TimeSpecifier timeSpecifier = null;
    private Annotation annotation = null;


    protected AggregationDefinition(String id) {
        super(id);
    }

    public AggregationDefinition select(Selector selector) {
        this.selector = selector;
        return this;
    }

    public static AggregationDefinition id(String aggregationName) {
        return new AggregationDefinition(aggregationName);
    }

    public AggregationDefinition aggregateBy(Variable aggregateAttribute) {
        this.aggregateAttribute = aggregateAttribute;
        return this;
    }

    public AggregationDefinition every(TimeSpecifier timeSpecifier) {
        this.timeSpecifier = timeSpecifier;
        return this;
    }

    public AggregationDefinition from(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public AggregationDefinition annotation(Annotation annotation) {
        this.annotation = annotation;
        return this;
    }

    // TODO: 2/24/17 : toString and hashcode and equals ....
}
