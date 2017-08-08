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
package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.selection.BasicSelector;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregation Definition API
 */
public class AggregationDefinition extends AbstractDefinition {

    private static final long serialVersionUID = 1L;
    private BasicSingleInputStream basicSingleInputStream = null;
    private Selector selector = null;
    private Variable aggregateAttribute = null;
    private TimePeriod timePeriod = null;
    private List<Annotation> annotations = new ArrayList<>();

    protected AggregationDefinition(String id) {
        super(id);
    }

    public static AggregationDefinition id(String aggregationName) {
        return new AggregationDefinition(aggregationName);
    }

    public AggregationDefinition select(BasicSelector selector) {
        this.selector = selector;
        return this;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public AggregationDefinition aggregateBy(Variable aggregateAttribute) {
        this.aggregateAttribute = aggregateAttribute;
        return this;
    }

    public Variable getAggregateAttribute() {
        return this.aggregateAttribute;
    }

    public AggregationDefinition every(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
        return this;
    }

    public TimePeriod getTimePeriod() {
        return this.timePeriod;
    }

    public AggregationDefinition from(BasicSingleInputStream basicSingleInputStream) {
        this.basicSingleInputStream = basicSingleInputStream;
        return this;
    }

    public BasicSingleInputStream getBasicSingleInputStream() {
        return this.basicSingleInputStream;
    }

    public AggregationDefinition annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public List<Annotation> getAnnotation() {
        return this.annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AggregationDefinition that = (AggregationDefinition) o;

        if (!basicSingleInputStream.equals(that.basicSingleInputStream)) {
            return false;
        }
        if (selector != null ? !selector.equals(that.selector) : that.selector != null) {
            return false;
        }
        if (!aggregateAttribute.equals(that.aggregateAttribute)) {
            return false;
        }
        if (timePeriod != null ? !timePeriod.equals(that.timePeriod) : that.timePeriod != null) {
            return false;
        }
        return annotations != null ? annotations.equals(that.annotations) : that.annotations == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + basicSingleInputStream.hashCode();
        result = 31 * result + (selector != null ? selector.hashCode() : 0);
        result = 31 * result + aggregateAttribute.hashCode();
        result = 31 * result + (timePeriod != null ? timePeriod.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }

}
