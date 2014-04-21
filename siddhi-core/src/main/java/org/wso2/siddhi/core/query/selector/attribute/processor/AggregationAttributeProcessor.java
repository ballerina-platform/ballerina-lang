/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.query.selector.attribute.processor;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.query.selector.attribute.factory.OutputAttributeAggregatorFactory;
import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;

public class AggregationAttributeProcessor extends AbstractAggregationAttributeProcessor implements NonGroupingAttributeProcessor {

    private OutputAttributeAggregator outputAttributeAggregator;

    public AggregationAttributeProcessor(Expression[] expressions, List<QueryEventSource> queryEventSourceList, OutputAttributeAggregatorFactory outputAttributeAggregatorFactory, String elementId, SiddhiContext siddhiContext) {
        super(expressions, queryEventSourceList, outputAttributeAggregatorFactory, elementId, siddhiContext);
        this.outputAttributeAggregator = sampleOutputAttributeAggregator;
    }

    public synchronized Object process(AtomicEvent event) {
        return process(event, outputAttributeAggregator);
    }

    @Override
    public void lock() {

    }

    @Override
    public void unlock() {

    }

    @Override
    protected Object[] currentState() {
        return new Object[]{outputAttributeAggregator};
    }

    @Override
    protected void restoreState(Object[] data) {
        outputAttributeAggregator = (OutputAttributeAggregator) data[0];

    }


}
