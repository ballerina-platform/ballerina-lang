/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.executor.expression;

import org.wso2.siddhi.core.event.*;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.table.predicate.PredicateBuilder;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.utils.SiddhiConstants;

import java.util.List;

public class VariableExpressionExecutor implements ExpressionExecutor {
    Attribute.Type type;
    int streamPosition = -1;
    int attributePosition = -1;
    int innerStreamPosition = -1;  //Simple Event (Default)
    String attributeName;
    String streamReference;

    public VariableExpressionExecutor(String streamIdOfVariable, String attributeName, int position,
                                      List<QueryEventSource> queryEventSourceList,
                                      String currentStreamReference, boolean processInDefinition) {
        this.attributeName = attributeName;
        if (streamIdOfVariable != null) {
            streamReference = streamIdOfVariable;
        } else {
            streamReference = currentStreamReference;
        }
        AbstractDefinition definition = null;
        int queryEventStreamListSize = queryEventSourceList.size();

        for (int i = 0; i < queryEventStreamListSize; i++) {
            QueryEventSource queryEventSource = queryEventSourceList.get(i);
            String referenceSourceId = queryEventSource.getReferenceSourceId();
            if (referenceSourceId != null && referenceSourceId.equals(streamReference)) {
                definition = updateAttributeData(position, processInDefinition, i, queryEventSource);
                if (definition != null) {
                    break;
                }
            }
        }
        if (definition == null) {
            for (int i = 0; i < queryEventStreamListSize; i++) {
                QueryEventSource queryEventSource = queryEventSourceList.get(i);
                String sourceId = queryEventSource.getSourceId();
                if (sourceId != null && sourceId.equals(streamReference)) {
                    definition = updateAttributeData(position, processInDefinition, i, queryEventSource);
                    if (definition != null) {
                        break;
                    }
                }
            }
        }
        if (definition == null) {
            if (processInDefinition) {
                definition = queryEventSourceList.get(0).getInDefinition();
            } else {
                definition = queryEventSourceList.get(0).getOutDefinition();
            }
        }
        type = definition.getAttributeType(attributeName);
        attributePosition = definition.getAttributePosition(attributeName);

    }

    private AbstractDefinition updateAttributeData(int position, boolean processInDefinition, int i, QueryEventSource queryEventSource) {
        AbstractDefinition definition;
        if (processInDefinition) {
            definition = queryEventSource.getInDefinition();
        } else {
            definition = queryEventSource.getOutDefinition();
        }
        streamPosition = i;
        if (position > -1) { //for known positions
            innerStreamPosition = position;
        } else if (position == SiddhiConstants.PREV) {
            innerStreamPosition = SiddhiConstants.PREV;
        } else if (queryEventSource.isCounterStream()) { //to get the last event
            innerStreamPosition = SiddhiConstants.LAST;
        }
        return definition;
    }

    @Override
    public Object execute(AtomicEvent event) {
        try {
            if (event instanceof Event) {
                return ((Event) event).getData()[attributePosition];
            } else if (innerStreamPosition == SiddhiConstants.PREV) {
                StreamEvent streamEvent = ((StateEvent) event).getStreamEvent(streamPosition);
                if (streamEvent instanceof ListEvent) {
                    int prevLast = ((ListEvent) streamEvent).getActiveEvents() - 2;
                    if (prevLast > 0) {
                        streamEvent = ((ListEvent) streamEvent).getEvent(prevLast);
                        if (streamEvent != null) {
                            return ((Event) streamEvent).getData(attributePosition);
                        }
                    }
                }
                if (streamPosition - 1 >= 0) {
                    streamEvent = ((StateEvent) event).getStreamEvent(streamPosition - 1);
                    if (streamEvent instanceof ListEvent) {
                        int prevLast = ((ListEvent) streamEvent).getActiveEvents() - 1;
                        if (prevLast > 0) {
                            streamEvent = ((ListEvent) streamEvent).getEvent(prevLast);
                            if (streamEvent != null) {
                                return ((Event) streamEvent).getData(attributePosition);
                            }
                        }
                    } else if (streamEvent instanceof Event) {
                        return ((Event) streamEvent).getData(attributePosition);
                    }
                }
                if (streamPosition - 2 >= 0) {
                    streamEvent = ((StateEvent) event).getStreamEvent(streamPosition - 2);
                    if (streamEvent instanceof ListEvent) {
                        int prevLast = ((ListEvent) streamEvent).getActiveEvents() - 1;
                        if (prevLast > 0) {
                            streamEvent = ((ListEvent) streamEvent).getEvent(prevLast);
                            if (streamEvent != null) {
                                return ((Event) streamEvent).getData(attributePosition);
                            }
                        }
                    } else if (streamEvent instanceof Event) {
                        return ((Event) streamEvent).getData(attributePosition);
                    }
                }
                return null;
//
            } else { //State Event
                StreamEvent streamEvent = ((StateEvent) event).getStreamEvent(streamPosition);
                if (streamEvent == null) {
                    return null;
                } else if (streamEvent instanceof Event) { //Single Event
                    return ((Event) streamEvent).getData(attributePosition);
                } else if (innerStreamPosition == SiddhiConstants.LAST) { //Event List Unknown Size (get the last event)
                    streamEvent = ((ListEvent) streamEvent).getEvent(((ListEvent) streamEvent).getActiveEvents() - 1);
                    if (streamEvent == null) {
                        return null;
                    } else {
                        return ((Event) streamEvent).getData(attributePosition);
                    }
                } else {    //Event List Known Size
                    streamEvent = ((ListEvent) streamEvent).getEvent(innerStreamPosition);
                    if (streamEvent == null) {
                        return null;
                    } else {
                        return ((Event) streamEvent).getData(attributePosition);
                    }
                }
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Attribute.Type getReturnType() {
        return type;
    }


    public String constructFilterQuery(AtomicEvent newEvent, int level) {
        Object obj = execute(newEvent);
        if (obj == null) {
            StringBuilder sb = new StringBuilder();
            if (streamPosition >= 0 && level == 0) {
                sb.append("event").append(streamPosition).append(".");
            }
            if (innerStreamPosition >= 0) {
                sb.append("event").append(innerStreamPosition).append(".");
            }
            if (attributePosition >= 0) {
                sb.append("data").append(attributePosition);
            }
            return sb.toString();
        } else if (obj instanceof String) {
            return "'" + obj.toString() + "'";
        } else {
            return obj.toString();
        }
    }

    public PredicateTreeNode constructPredicate(AtomicEvent newEvent, TableDefinition tableDefinition, PredicateBuilder predicateBuilder) {
//            if (!((Event) newEvent).getStreamId().equals(streamReference)) {
        //TODO check what if user has given "as" reference for the table in Join
        //TODO is only TableId used ?
        if (tableDefinition.getTableId().equals(streamReference)) {
            Object[] attributeArray = new Object[attributePosition + 1];
            attributeArray[attributePosition] = attributeName;
//                newEvent = new InEvent("dbEventStream", System.currentTimeMillis(), attributeArray);
            newEvent = new InEvent(streamReference, System.currentTimeMillis(), attributeArray);
            return predicateBuilder.buildVariableExpression(execute(newEvent).toString());
        } else {
            Object obj = execute(newEvent);
            Object value = ((Event) newEvent).getData(attributePosition);
            // wrap with quotes for string types.
                /*if (value != null && value instanceof String) {
                    return "'" + obj.toString() + "'";
                }
                return obj.toString();*/

            // todo - check the validity of this line.
            return predicateBuilder.buildValue(obj);
        }

    }
}
