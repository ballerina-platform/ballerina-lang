/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.creator;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateManager;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.QueryPartComposite;
import org.wso2.siddhi.core.util.parser.QueryOutputParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.ExpressionValidator;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.SingleStream;
import org.wso2.siddhi.query.api.query.selection.attribute.ComplexAttribute;
import org.wso2.siddhi.query.api.query.selection.attribute.OutputAttribute;
import org.wso2.siddhi.query.api.query.selection.attribute.OutputAttributeExtension;
import org.wso2.siddhi.query.api.query.selection.attribute.SimpleAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public abstract class QueryCreator {

    protected final ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap;
    protected final ConcurrentMap<String, EventTable> eventTableMap;
    protected OutputRateManager outputRateManager;
    protected final SiddhiContext siddhiContext;
    protected final String queryId;
    protected final Query query;
    protected List<QueryEventSource> queryEventSourceList;
    protected StreamDefinition outputStreamDefinition;

    protected QueryCreator(String queryId, Query query, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, ConcurrentMap<String, EventTable> eventTableMap, OutputRateManager outputRateManager, SiddhiContext siddhiContext) {
        this.queryId = queryId;
        this.query = query;
        this.streamTableDefinitionMap = streamTableDefinitionMap;
        this.eventTableMap = eventTableMap;
        this.outputRateManager = outputRateManager;
        this.siddhiContext = siddhiContext;
    }

    protected void init() {
        queryEventSourceList = query.getInputStream().constructQueryEventSourceList(streamTableDefinitionMap, new ArrayList<QueryEventSource>());
        updateQueryEventSourceList(queryEventSourceList);
        validateOutput(queryEventSourceList, query);
        populateSelection(queryEventSourceList, query);
        QuerySelector querySelector = constructQuerySelector(outputRateManager);
        outputStreamDefinition = querySelector.getOutputStreamDefinition();
        if (outputRateManager instanceof WrappedSnapshotOutputRateManager) {
            ((WrappedSnapshotOutputRateManager) outputRateManager).setAggregateAttributePositionList(querySelector.getAggregateAttributePositionList());
            ((WrappedSnapshotOutputRateManager) outputRateManager).setAttributeSize(querySelector.getAttributeSize());
        }
    }

    protected abstract void updateQueryEventSourceList(List<QueryEventSource> queryEventSourceList);

    protected QuerySelector constructQuerySelector(OutputRateManager outputRateManager) {
        return QueryOutputParser.constructQuerySelector(query.getOutputStream(), query.getSelector(), outputRateManager, queryEventSourceList,
                                                        eventTableMap, siddhiContext);
    }


    protected void validateOutput(List<QueryEventSource> queryEventSourceList, Query query) {

        List<OutputAttribute> outputAttributeList = query.getSelector().getSelectionList();
        for (OutputAttribute outputAttribute : outputAttributeList) {
            if (outputAttribute instanceof SimpleAttribute) {
                ExpressionValidator.validate(((SimpleAttribute) outputAttribute).getExpression(), queryEventSourceList, null, false);
            } else if (outputAttribute instanceof ComplexAttribute) {
                for (Expression expression : ((ComplexAttribute) outputAttribute).getExpressions()) {
                    ExpressionValidator.validate(expression, queryEventSourceList, null, false);
                }
            } else {//Extension
                for (Expression expression : ((OutputAttributeExtension) outputAttribute).getExpressions()) {
                    ExpressionValidator.validate(expression, queryEventSourceList, null, false);
                }
            }
        }
    }

    protected void populateSelection(List<QueryEventSource> queryEventSourceList, Query query) {
        //populate selection for * case
        List<OutputAttribute> attributeList = query.getSelector().getSelectionList();
        if (attributeList.size() == 0) {
            if (query.getInputStream() instanceof SingleStream && queryEventSourceList.size() == 1) {
                QueryEventSource queryEventSource = queryEventSourceList.get(0);
                for (Attribute attribute : queryEventSourceList.get(0).getOutDefinition().getAttributeList()) {
                    attributeList.add(new SimpleAttribute(attribute.getName(), Expression.variable(queryEventSource.getReferenceSourceId(), attribute.getName())));
                }
            } else {
                for (QueryEventSource queryEventSource : queryEventSourceList) {
                    for (Attribute attribute : queryEventSource.getOutDefinition().getAttributeList()) {
                        attributeList.add(new SimpleAttribute(queryEventSource.getReferenceSourceId() + "_" + attribute.getName(), Expression.variable(queryEventSource.getReferenceSourceId(), attribute.getName())));
                    }
                }
            }
        }
    }

    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

    public abstract QueryPartComposite constructQuery();

    public List<QueryEventSource> getQueryEventSourceList() {
        return queryEventSourceList;
    }
}
