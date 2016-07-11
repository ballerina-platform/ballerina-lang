/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.table.holder;

import com.google.common.collect.HashMultimap;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.*;

public class PrimaryKeyMultyMapIndexEventHolder implements IndexedEventHolder {

    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter;
    private int primaryKeyPosition;
    private String primaryKeyAttribute;
    private Map<String, Integer> indexMetaData;
    private TreeMap<Object, StreamEvent> data = new TreeMap<Object, StreamEvent>();
    private Map<String, HashMultimap<Object, StreamEvent>> indexedData = new HashMap<String, HashMultimap<Object, StreamEvent>>();

    public PrimaryKeyMultyMapIndexEventHolder(StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter,
                                      int primaryKeyPosition, String primaryKeyAttribute, Map<String, Integer> indexMetaData) {
        this.tableStreamEventPool = tableStreamEventPool;
        this.eventConverter = eventConverter;
        this.primaryKeyPosition = primaryKeyPosition;
        this.primaryKeyAttribute = primaryKeyAttribute;
        this.indexMetaData = indexMetaData;

        for (String indexAttributeName : indexMetaData.keySet()) {
            indexedData.put(indexAttributeName, HashMultimap.<Object, StreamEvent>create());
        }
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            ComplexEvent complexEvent = addingEventChunk.next();
            StreamEvent streamEvent = tableStreamEventPool.borrowEvent();
            eventConverter.convertComplexEvent(complexEvent, streamEvent);
            add(streamEvent);
        }
    }

    private void add(StreamEvent streamEvent) {
        StreamEvent deletedEvent = data.put(streamEvent.getOutputData()[primaryKeyPosition], streamEvent);

        for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
            HashMultimap<Object, StreamEvent> indexMap = indexedData.get(indexEntry.getKey());
            if (deletedEvent != null) {
                indexMap.remove(streamEvent.getOutputData()[indexEntry.getValue()], streamEvent);
            }
            indexMap.put(streamEvent.getOutputData()[indexEntry.getValue()], streamEvent);
        }
    }

    @Override
    public boolean isSupportedIndex(String attribute, Compare.Operator operator) {
        return isAttributeIndexed(attribute) &&
                (operator == Compare.Operator.EQUAL || operator == Compare.Operator.NOT_EQUAL);
    }

    @Override
    public boolean isAttributeIndexed(String attribute) {
        return primaryKeyAttribute.equalsIgnoreCase(attribute) || indexMetaData.containsKey(attribute);
    }

    @Override
    public Set<StreamEvent> getAllEvents() {
        return new HashSet<StreamEvent>(data.values());
    }

    @Override
    public Set<StreamEvent> findEvents(String attribute, Compare.Operator operator, Object value) {
        if (operator == Compare.Operator.EQUAL) {
            if (attribute.equals(primaryKeyAttribute)) {
                Set<StreamEvent> streamEventSet = new HashSet<StreamEvent>();
                StreamEvent resultEvent = data.get(value);
                if (resultEvent != null) {
                    streamEventSet.add(resultEvent);
                }
                return streamEventSet;
            } else {
                Set<StreamEvent> streamEventSet = new HashSet<StreamEvent>();
                Set<StreamEvent> resultEvent = indexedData.get(attribute).get(value);
                if (resultEvent != null) {
                    streamEventSet.addAll(resultEvent);
                }
                return streamEventSet;
            }
        } else if (operator == Compare.Operator.NOT_EQUAL) {
            if (attribute.equals(primaryKeyAttribute)) {
                Set<StreamEvent> streamEventSet;
                if (data.size() > 0) {
                    streamEventSet = new HashSet<StreamEvent>(data.values());
                } else {
                    streamEventSet = new HashSet<StreamEvent>();
                }

                StreamEvent resultEvent = data.get(value);
                if (resultEvent != null) {
                    streamEventSet.remove(resultEvent);
                }
                return streamEventSet;
            } else {
                Set<StreamEvent> streamEventSet;
                HashMultimap<Object, StreamEvent> aIndexedData = indexedData.get(attribute);

                if (aIndexedData.size() > 0) {
                    streamEventSet = new HashSet<StreamEvent>(aIndexedData.values());
                } else {
                    streamEventSet = new HashSet<StreamEvent>();
                }

                Set<StreamEvent> resultEvent = aIndexedData.get(value);
                if (resultEvent != null) {
                    streamEventSet.removeAll(resultEvent);
                }
                return streamEventSet;
            }
        } else if (operator == Compare.Operator.LESS_THAN) {
            if (attribute.equals(primaryKeyAttribute)) {
//                Set<StreamEvent> streamEventSet;
//                if (data.size() > 0) {
//                    streamEventSet = new HashSet<StreamEvent>(data.values());
//                } else {
//                    streamEventSet = new HashSet<StreamEvent>();
//                }
//
//                StreamEvent resultEvent = data.get(value);
//                if (resultEvent != null) {
//                    streamEventSet.remove(resultEvent);
//                }
                return null;
            } else {
                Set<StreamEvent> streamEventSet;
                HashMultimap<Object, StreamEvent> aIndexedData = indexedData.get(attribute);

                if (aIndexedData.size() > 0) {
                    streamEventSet = new HashSet<StreamEvent>(aIndexedData.values());
                } else {
                    streamEventSet = new HashSet<StreamEvent>();
                }

                Set<StreamEvent> resultEvent = aIndexedData.get(value);
                if (resultEvent != null) {
                    streamEventSet.removeAll(resultEvent);
                }
                return streamEventSet;
            }
        } else {
            throw new OperationNotSupportedException(operator + " not supported by " + getClass().getName());
        }
    }

    @Override
    public void deleteAll() {
        data.clear();
        for (HashMultimap<Object, StreamEvent> aIndexedData : indexedData.values()) {
            aIndexedData.clear();
        }
    }

    @Override
    public void deleteAll(Collection<StreamEvent> candidateEventSet) {
        for (StreamEvent streamEvent : candidateEventSet) {
            StreamEvent deletedEvent = data.remove(streamEvent.getOutputData()[primaryKeyPosition]);
            if (deletedEvent != null) {
                for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                    HashMultimap<Object, StreamEvent> indexMap = indexedData.get(indexEntry.getKey());
                    indexMap.remove(streamEvent.getOutputData()[indexEntry.getValue()], deletedEvent);
                }
            }
        }

    }

    @Override
    public void delete(String attribute, Compare.Operator operator, Object value) {
        if (primaryKeyAttribute.equalsIgnoreCase(attribute)) {
            if (operator == Compare.Operator.EQUAL) {
                StreamEvent deletedEvent = data.remove(value);
                if (deletedEvent != null) {
                    for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                        HashMultimap<Object, StreamEvent> indexMap = indexedData.get(indexEntry.getKey());
                        indexMap.remove(deletedEvent.getOutputData()[indexEntry.getValue()], deletedEvent);
                    }
                }
            } else if (operator == Compare.Operator.NOT_EQUAL) {
                StreamEvent streamEvent = data.get(value);
                deleteAll();
                if (streamEvent != null) {
                    add(streamEvent);
                }
            } else {
                throw new OperationNotSupportedException(operator + " not supported by " + getClass().getName());
            }
        } else {
            if (operator == Compare.Operator.EQUAL) {
                Set<StreamEvent> deletedEventSet = indexedData.get(attribute).removeAll(value);
                if (deletedEventSet != null && deletedEventSet.size() > 0) {
                    for (StreamEvent deletedEvent : deletedEventSet) {
                        data.remove(deletedEvent.getOutputData()[primaryKeyPosition]);
                        for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                            if (!attribute.equals(indexEntry.getKey())) {
                                HashMultimap<Object, StreamEvent> indexMap = indexedData.get(indexEntry.getKey());
                                indexMap.remove(deletedEvent.getOutputData()[indexEntry.getValue()], deletedEvent);
                            }
                        }
                    }

                }
            } else if (operator == Compare.Operator.NOT_EQUAL) {
                Set<StreamEvent> matchingEventSet = indexedData.get(attribute).get(value);
                deleteAll();
                for (StreamEvent matchingEvent : matchingEventSet) {
                    add(matchingEvent);
                }
            } else {
                throw new OperationNotSupportedException(operator + " not supported by " + getClass().getName());
            }
        }
    }

    @Override
    public boolean containsEventSet(String attribute, Compare.Operator operator, Object value) {
        return false;
    }
}
