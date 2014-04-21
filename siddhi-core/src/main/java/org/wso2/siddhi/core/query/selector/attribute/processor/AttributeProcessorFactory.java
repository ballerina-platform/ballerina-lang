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
import org.wso2.siddhi.core.query.selector.attribute.factory.OutputAttributeAggregatorFactory;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;

public class AttributeProcessorFactory {

    public static AttributeProcessor createAttributeProcessor(
            Expression[] expressions, List<QueryEventSource> queryEventSourceList, OutputAttributeAggregatorFactory outputAttributeAggregatorFactory, SiddhiContext siddhiContext,
            boolean groupBy) {
        String newElementId = siddhiContext.getElementIdGenerator().createNewId();
        if (groupBy) {
            if (siddhiContext.isDistributedProcessingEnabled()) {
                return new DistributedGroupByAggregationAttributeProcessor(expressions, queryEventSourceList, outputAttributeAggregatorFactory, newElementId, siddhiContext);
            } else {
                return new GroupByAttributeAggregatorProcessor(expressions, queryEventSourceList, outputAttributeAggregatorFactory, newElementId, siddhiContext);
            }
        } else {
            if (siddhiContext.isDistributedProcessingEnabled()) {
                return new DistributedAggregationAttributeProcessor(expressions, queryEventSourceList, outputAttributeAggregatorFactory, newElementId, siddhiContext);
            } else {
                return new AggregationAttributeProcessor(expressions, queryEventSourceList, outputAttributeAggregatorFactory, newElementId, siddhiContext);
            }
        }
    }

}