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
import org.wso2.siddhi.core.event.remove.RemoveStream;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.persistence.PersistenceStore;
import org.wso2.siddhi.core.snapshot.Snapshotable;
import org.wso2.siddhi.core.query.selector.attribute.factory.OutputAttributeAggregatorFactory;
import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.core.util.parser.ExecutorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAggregationAttributeProcessor implements AttributeProcessor, Snapshotable {

    protected String elementId;
    protected OutputAttributeAggregator sampleOutputAttributeAggregator;
    protected List<ExpressionExecutor> expressionExecutors;
    protected final OutputAttributeAggregatorFactory outputAttributeAggregatorFactory;
    protected SiddhiContext siddhiContext;
    private final int size;

    public AbstractAggregationAttributeProcessor(Expression[] expressions,
                                                 List<QueryEventSource> queryEventSourceList,
                                                 OutputAttributeAggregatorFactory outputAttributeAggregatorFactory,
                                                 String elementId, SiddhiContext siddhiContext) {
        this.outputAttributeAggregatorFactory = outputAttributeAggregatorFactory;
        this.siddhiContext = siddhiContext;
        this.expressionExecutors = new ArrayList<ExpressionExecutor>();
        for (Expression expression : expressions) {
            this.expressionExecutors.add(ExecutorParser.parseExpression(expression, queryEventSourceList, null, false, siddhiContext));
        }
        this.elementId = elementId;
        Attribute.Type[] attributeTypes = new Attribute.Type[expressionExecutors.size()];
        for (int i = 0; i < expressionExecutors.size(); i++) {
            attributeTypes[i] = expressionExecutors.get(i).getReturnType();
        }
        this.sampleOutputAttributeAggregator = outputAttributeAggregatorFactory.createAttributeAggregator(attributeTypes);
        siddhiContext.addEternalReferencedHolder(sampleOutputAttributeAggregator);
        size = expressionExecutors.size();
    }

    protected Object process(AtomicEvent event, OutputAttributeAggregator outputAttributeAggregator) {
        if (size > 1) {
            Object[] data = new Object[expressionExecutors.size()];
            for (int i = 0, size = data.length; i < size; i++) {
                data[i] = expressionExecutors.get(i).execute(event);
            }
            if (event instanceof RemoveStream) {
                return outputAttributeAggregator.processRemove(data);
            } else {
                return outputAttributeAggregator.processAdd(data);
            }
        } else {
            if (event instanceof RemoveStream) {
                return outputAttributeAggregator.processRemove(expressionExecutors.get(0).execute(event));
            } else {
                return outputAttributeAggregator.processAdd(expressionExecutors.get(0).execute(event));
            }
        }
    }


    @Override
    public Attribute.Type getOutputType() {
        return sampleOutputAttributeAggregator.getReturnType();
    }

    @Override
    public SnapshotObject snapshot() {
        return new SnapshotObject(currentState());
    }

    protected abstract Object[] currentState();


    @Override
    public void restore(SnapshotObject snapshotObject) {
        restoreState(snapshotObject.getData());
    }

    protected abstract void restoreState(Object[] data);


    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
