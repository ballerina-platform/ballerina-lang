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

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Abstract class for incremental aggregators
 */
public abstract class IncrementalAttributeAggregator {

    public abstract void init(String attributeName, Attribute.Type attributeType);

    public abstract Object aggregate(Object... results);

    public abstract Attribute[] getBaseAttributes();

    public abstract Expression[] getBaseAttributeInitialValues();

    public abstract Expression[] getBaseAggregators();

    public abstract Attribute.Type getReturnType();
}
