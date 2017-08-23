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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * EventHolder implementation where events will be indexed and stored. This will offer faster access compared to
 * other EventHolder implementations. User can only add unique events based on a given primary key.
 */
public class IndexEventHolder implements IndexedEventHolder {

    private static final Logger log = Logger.getLogger(IndexEventHolder.class);
    private final Map<Object, StreamEvent> primaryKeyData;
    private final Map<String, TreeMap<Object, Set<StreamEvent>>> indexData;
    private final PrimaryKeyReferenceHolder[] primaryKeyReferenceHolders;
    private String primaryKeyAttributes = null;
    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter;
    private Map<String, Integer> indexMetaData;
    private Map<String, Integer> multiPrimaryKeyMetaData = new LinkedHashMap<>();
    private Map<String, Integer> allIndexMetaData = new HashMap<>();

    public IndexEventHolder(StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter,
                            PrimaryKeyReferenceHolder[] primaryKeyReferenceHolders,
                            boolean isPrimaryNumeric, Map<String, Integer> indexMetaData) {
        this.tableStreamEventPool = tableStreamEventPool;
        this.eventConverter = eventConverter;
        this.primaryKeyReferenceHolders = primaryKeyReferenceHolders;
        this.indexMetaData = indexMetaData;

        if (primaryKeyReferenceHolders != null) {
            if (isPrimaryNumeric) {
                primaryKeyData = new TreeMap<Object, StreamEvent>();
            } else {
                primaryKeyData = new HashMap<Object, StreamEvent>();
            }
            if (primaryKeyReferenceHolders.length == 1) {
                allIndexMetaData.put(primaryKeyReferenceHolders[0].getPrimaryKeyAttribute(),
                        primaryKeyReferenceHolders[0].getPrimaryKeyPosition());
                primaryKeyAttributes = primaryKeyReferenceHolders[0].getPrimaryKeyAttribute();
            } else {
                StringBuilder primaryKeyAttributesBuilder = new StringBuilder();
                for (PrimaryKeyReferenceHolder primaryKeyReferenceHolder : primaryKeyReferenceHolders) {
                    multiPrimaryKeyMetaData.put(primaryKeyReferenceHolder.getPrimaryKeyAttribute(),
                            primaryKeyReferenceHolder.getPrimaryKeyPosition());
                    primaryKeyAttributesBuilder.append(primaryKeyReferenceHolder.getPrimaryKeyAttribute())
                            .append(SiddhiConstants.KEY_DELIMITER);
                }
                primaryKeyAttributes = primaryKeyAttributesBuilder.toString();
            }
        } else {
            primaryKeyData = null;
        }
        if (indexMetaData.size() > 0) {
            indexData = new HashMap<String, TreeMap<Object, Set<StreamEvent>>>();
            for (String indexAttributeName : indexMetaData.keySet()) {
                indexData.put(indexAttributeName, new TreeMap<Object, Set<StreamEvent>>());
            }
            allIndexMetaData.putAll(indexMetaData);
        } else {
            indexData = null;
        }

    }

    @Override
    public Set<Object> getAllPrimaryKeyValues() {
        if (primaryKeyData != null) {
            return primaryKeyData.keySet();
        } else {
            return null;
        }
    }

    @Override
    public PrimaryKeyReferenceHolder[] getPrimaryKeyReferenceHolders() {
        return primaryKeyReferenceHolders;
    }

    @Override
    public boolean isMultiPrimaryKeyAttribute(String attributeName) {
        return multiPrimaryKeyMetaData.containsKey(attributeName);
    }

    @Override
    public boolean isAttributeIndexed(String attribute) {
        return allIndexMetaData.containsKey(attribute);
    }

    @Override
    public boolean isAttributeIndexed(int position) {
        return allIndexMetaData.containsValue(position);
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

        StreamEvent existingValue = null;
        if (primaryKeyData != null) {
            Object primaryKey = constructPrimaryKey(streamEvent, primaryKeyReferenceHolders);
            existingValue = primaryKeyData.putIfAbsent(primaryKey, streamEvent);
            if (existingValue != null) {
                log.error("Drooping event :" + streamEvent + ", as there is already an event stored with primary key " +
                        "'" + primaryKey + "'");
            }
        }

        if (indexData != null) {
            for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                TreeMap<Object, Set<StreamEvent>> indexMap = indexData.get(indexEntry.getKey());
                Object key = streamEvent.getOutputData()[indexEntry.getValue()];
                Set<StreamEvent> values = indexMap.get(key);
                if (values == null) {
                    values = new HashSet<StreamEvent>();
                    values.add(streamEvent);
                    indexMap.put(streamEvent.getOutputData()[indexEntry.getValue()], values);
                } else {
                    values.add(streamEvent);
                }
            }
        }

    }

    private Object constructPrimaryKey(StreamEvent streamEvent,
                                       PrimaryKeyReferenceHolder[] primaryKeyReferenceHolders) {
        if (primaryKeyReferenceHolders.length == 1) {
            return streamEvent.getOutputData()[primaryKeyReferenceHolders[0].getPrimaryKeyPosition()];
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (PrimaryKeyReferenceHolder primaryKeyReferenceHolder : primaryKeyReferenceHolders) {
                stringBuilder.append(streamEvent.getOutputData()[primaryKeyReferenceHolder.getPrimaryKeyPosition()])
                        .append(SiddhiConstants.KEY_DELIMITER);
            }
            return stringBuilder.toString();
        }
    }

    @Override
    public void overwrite(StreamEvent streamEvent) {
        StreamEvent deletedEvent = null;
        if (primaryKeyData != null) {
            Object primaryKey = constructPrimaryKey(streamEvent, primaryKeyReferenceHolders);
            deletedEvent = primaryKeyData.put(primaryKey, streamEvent);
        }

        if (indexData != null) {
            for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                TreeMap<Object, Set<StreamEvent>> indexMap = indexData.get(indexEntry.getKey());
                Object key = streamEvent.getOutputData()[indexEntry.getValue()];
                if (deletedEvent != null) {
                    Set<StreamEvent> values = indexMap.get(key);
                    values.remove(deletedEvent);
                    if (values.size() == 0) {
                        indexMap.remove(key);
                    }
                }
                Set<StreamEvent> values = indexMap.get(key);
                if (values == null) {
                    values = new HashSet<StreamEvent>();
                    values.add(streamEvent);
                    indexMap.put(streamEvent.getOutputData()[indexEntry.getValue()], values);
                } else {
                    values.add(streamEvent);
                }
            }
        }
    }

    @Override
    public Collection<StreamEvent> getAllEvents() {
        if (primaryKeyData != null) {
            return primaryKeyData.values();
        } else if (indexData != null) {
            HashSet<StreamEvent> resultEventSet = new HashSet<StreamEvent>();
            Iterator<TreeMap<Object, Set<StreamEvent>>> iterator = indexData.values().iterator();
            if (iterator.hasNext()) {
                TreeMap<Object, Set<StreamEvent>> aIndexData = iterator.next();
                for (Set<StreamEvent> streamEvents : aIndexData.values()) {
                    resultEventSet.addAll(streamEvents);
                }
            }
            return resultEventSet;
        } else {
            return new HashSet<StreamEvent>();
        }
    }

    @Override
    public Collection<StreamEvent> findEvents(String attribute, Compare.Operator operator, Object value) {

        if (primaryKeyData != null && attribute.equals(primaryKeyAttributes)) {
            StreamEvent resultEvent;
            HashSet<StreamEvent> resultEventSet;

            switch (operator) {
                case LESS_THAN:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).headMap(value, false).values();
                case GREATER_THAN:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).tailMap(value, false).values();
                case LESS_THAN_EQUAL:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).headMap(value, true).values();
                case GREATER_THAN_EQUAL:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).tailMap(value, true).values();
                case EQUAL:
                    resultEventSet = new HashSet<StreamEvent>();
                    resultEvent = primaryKeyData.get(value);
                    if (resultEvent != null) {
                        resultEventSet.add(resultEvent);
                    }
                    return resultEventSet;
                case NOT_EQUAL:
                    if (primaryKeyData.size() > 0) {
                        resultEventSet = new HashSet<StreamEvent>(primaryKeyData.values());
                    } else {
                        return new HashSet<StreamEvent>();
                    }
                    resultEvent = primaryKeyData.get(value);
                    if (resultEvent != null) {
                        resultEventSet.remove(resultEvent);
                    }
                    return resultEventSet;
            }
        } else {
            HashSet<StreamEvent> resultEventSet = new HashSet<StreamEvent>();
            TreeMap<Object, Set<StreamEvent>> currentIndexedData = indexData.get(attribute);

            Set<StreamEvent> resultEvents;
            switch (operator) {
                case LESS_THAN:
                    for (Set<StreamEvent> eventSet : currentIndexedData.headMap(value, false).values()) {
                        resultEventSet.addAll(eventSet);
                    }
                    return resultEventSet;
                case GREATER_THAN:
                    for (Set<StreamEvent> eventSet : currentIndexedData.tailMap(value, false).values()) {
                        resultEventSet.addAll(eventSet);
                    }
                    return resultEventSet;
                case LESS_THAN_EQUAL:
                    for (Set<StreamEvent> eventSet : currentIndexedData.headMap(value, true).values()) {
                        resultEventSet.addAll(eventSet);
                    }
                    return resultEventSet;
                case GREATER_THAN_EQUAL:
                    for (Set<StreamEvent> eventSet : currentIndexedData.tailMap(value, true).values()) {
                        resultEventSet.addAll(eventSet);
                    }
                    return resultEventSet;
                case EQUAL:
                    resultEvents = currentIndexedData.get(value);
                    if (resultEvents != null) {
                        resultEventSet.addAll(resultEvents);
                    }
                    return resultEventSet;
                case NOT_EQUAL:
                    if (currentIndexedData.size() > 0) {
                        resultEventSet = new HashSet<StreamEvent>();
                        for (Set<StreamEvent> eventSet : currentIndexedData.values()) {
                            resultEventSet.addAll(eventSet);
                        }
                    } else {
                        resultEventSet = new HashSet<StreamEvent>();
                    }

                    resultEvents = currentIndexedData.get(value);
                    if (resultEvents != null) {
                        resultEventSet.removeAll(resultEvents);
                    }
                    return resultEventSet;
            }
        }
        throw new OperationNotSupportedException(operator + " not supported for '" + value + "' by " + getClass()
                .getName());
    }

    @Override
    public void deleteAll() {
        if (primaryKeyData != null) {
            primaryKeyData.clear();
        }
        if (indexData != null) {
            for (TreeMap<Object, Set<StreamEvent>> aIndexedData : indexData.values()) {
                aIndexedData.clear();
            }
        }
    }

    @Override
    public void deleteAll(Collection<StreamEvent> storeEventSet) {
        for (StreamEvent streamEvent : storeEventSet) {
            if (primaryKeyData != null) {
                Object primaryKey = constructPrimaryKey(streamEvent, primaryKeyReferenceHolders);
                StreamEvent deletedEvent = primaryKeyData.remove(primaryKey);
                if (indexData != null) {
                    deleteFromIndexes(deletedEvent);
                }
            } else if (indexData != null) {
                deleteFromIndexes(streamEvent);
            }
        }
    }

    @Override
    public void delete(String attribute, Compare.Operator operator, Object value) {
        if (primaryKeyData != null && attribute.equals(primaryKeyAttributes)) {
            switch (operator) {

                case LESS_THAN:
                    for (Iterator<StreamEvent> iterator = ((TreeMap<Object, StreamEvent>) primaryKeyData).
                            headMap(value, false).values().iterator();
                         iterator.hasNext(); ) {
                        StreamEvent toDeleteEvent = iterator.next();
                        iterator.remove();
                        deleteFromIndexes(toDeleteEvent);
                    }
                    return;
                case GREATER_THAN:
                    for (Iterator<StreamEvent> iterator = ((TreeMap<Object, StreamEvent>) primaryKeyData).
                            tailMap(value, false).values().iterator();
                         iterator.hasNext(); ) {
                        StreamEvent toDeleteEvent = iterator.next();
                        iterator.remove();
                        deleteFromIndexes(toDeleteEvent);
                    }
                    return;
                case LESS_THAN_EQUAL:
                    for (Iterator<StreamEvent> iterator = ((TreeMap<Object, StreamEvent>) primaryKeyData).
                            headMap(value, true).values().iterator();
                         iterator.hasNext(); ) {
                        StreamEvent toDeleteEvent = iterator.next();
                        iterator.remove();
                        deleteFromIndexes(toDeleteEvent);
                    }
                    return;
                case GREATER_THAN_EQUAL:
                    for (Iterator<StreamEvent> iterator = ((TreeMap<Object, StreamEvent>) primaryKeyData).
                            tailMap(value, true).values().iterator();
                         iterator.hasNext(); ) {
                        StreamEvent toDeleteEvent = iterator.next();
                        iterator.remove();
                        deleteFromIndexes(toDeleteEvent);
                    }
                    return;
                case EQUAL:
                    StreamEvent deletedEvent = primaryKeyData.remove(value);
                    if (deletedEvent != null) {
                        deleteFromIndexes(deletedEvent);
                    }
                    return;
                case NOT_EQUAL:
                    StreamEvent streamEvent = primaryKeyData.get(value);
                    deleteAll();
                    if (streamEvent != null) {
                        add(streamEvent);
                    }
                    return;
            }
        } else {
            switch (operator) {

                case LESS_THAN:
                    for (Iterator<Set<StreamEvent>> iterator = indexData.get(attribute).
                            headMap(value, false).values().iterator();
                         iterator.hasNext(); ) {
                        Set<StreamEvent> deletedEventSet = iterator.next();
                        deleteFromIndexesAndPrimaryKey(attribute, deletedEventSet);
                        iterator.remove();
                    }
                    return;
                case GREATER_THAN:
                    for (Iterator<Set<StreamEvent>> iterator = indexData.get(attribute).
                            tailMap(value, false).values().iterator();
                         iterator.hasNext(); ) {
                        Set<StreamEvent> deletedEventSet = iterator.next();
                        deleteFromIndexesAndPrimaryKey(attribute, deletedEventSet);
                        iterator.remove();
                    }
                    return;
                case LESS_THAN_EQUAL:
                    for (Iterator<Set<StreamEvent>> iterator = indexData.get(attribute).
                            headMap(value, true).values().iterator();
                         iterator.hasNext(); ) {
                        Set<StreamEvent> deletedEventSet = iterator.next();
                        deleteFromIndexesAndPrimaryKey(attribute, deletedEventSet);
                        iterator.remove();
                    }
                    return;
                case GREATER_THAN_EQUAL:
                    for (Iterator<Set<StreamEvent>> iterator = indexData.get(attribute).
                            tailMap(value, true).values().iterator();
                         iterator.hasNext(); ) {
                        Set<StreamEvent> deletedEventSet = iterator.next();
                        deleteFromIndexesAndPrimaryKey(attribute, deletedEventSet);
                        iterator.remove();
                    }
                    return;
                case EQUAL:
                    Set<StreamEvent> deletedEventSet = indexData.get(attribute).remove(value);
                    if (deletedEventSet != null && deletedEventSet.size() > 0) {
                        deleteFromIndexesAndPrimaryKey(attribute, deletedEventSet);
                    }
                    return;
                case NOT_EQUAL:
                    Set<StreamEvent> matchingEventSet = indexData.get(attribute).get(value);
                    deleteAll();
                    for (StreamEvent matchingEvent : matchingEventSet) {
                        add(matchingEvent);
                    }
                    return;
            }
        }
        throw new OperationNotSupportedException(operator + " not supported for '" + value + "' by " + getClass()
                .getName());
    }

    @Override
    public boolean containsEventSet(String attribute, Compare.Operator operator, Object value) {
        if (primaryKeyData != null && attribute.equals(primaryKeyAttributes)) {
            switch (operator) {
                case LESS_THAN:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).lowerKey(value) != null;
                case GREATER_THAN:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).higherKey(value) != null;
                case LESS_THAN_EQUAL:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).ceilingKey(value) != null;
                case GREATER_THAN_EQUAL:
                    return ((TreeMap<Object, StreamEvent>) primaryKeyData).floorKey(value) != null;
                case EQUAL:
                    return primaryKeyData.get(value) != null;
                case NOT_EQUAL:
                    return primaryKeyData.size() > 1;
            }
        } else {
            TreeMap<Object, Set<StreamEvent>> currentIndexedData = indexData.get(attribute);

            switch (operator) {

                case LESS_THAN:
                    return currentIndexedData.lowerKey(value) != null;
                case GREATER_THAN:
                    return currentIndexedData.higherKey(value) != null;
                case LESS_THAN_EQUAL:
                    return currentIndexedData.ceilingKey(value) != null;
                case GREATER_THAN_EQUAL:
                    return currentIndexedData.floorKey(value) != null;
                case EQUAL:
                    return currentIndexedData.get(value) != null;
                case NOT_EQUAL:
                    return currentIndexedData.size() > 1;
            }
        }
        throw new OperationNotSupportedException(operator + " not supported for '" + value + "' by " + getClass()
                .getName());
    }

    private void deleteFromIndexesAndPrimaryKey(String currentAttribute, Set<StreamEvent> deletedEventSet) {
        for (StreamEvent deletedEvent : deletedEventSet) {
            if (primaryKeyData != null) {
                Object primaryKey = constructPrimaryKey(deletedEvent, primaryKeyReferenceHolders);
                primaryKeyData.remove(primaryKey);
            }
            for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                if (!currentAttribute.equals(indexEntry.getKey())) {
                    TreeMap<Object, Set<StreamEvent>> indexMap = indexData.get(indexEntry.getKey());
                    Object key = deletedEvent.getOutputData()[indexEntry.getValue()];
                    Set<StreamEvent> values = indexMap.get(key);
                    values.remove(deletedEvent);
                    if (values.size() == 0) {
                        indexMap.remove(key);
                    }
                }
            }
        }
    }

    private void deleteFromIndexes(StreamEvent toDeleteEvent) {
        if (indexMetaData != null) {
            for (Map.Entry<String, Integer> indexEntry : indexMetaData.entrySet()) {
                TreeMap<Object, Set<StreamEvent>> indexMap = indexData.get(indexEntry.getKey());
                Object key = toDeleteEvent.getOutputData()[indexEntry.getValue()];
                Set<StreamEvent> values = indexMap.get(key);
                values.remove(toDeleteEvent);
                if (values.size() == 0) {
                    indexMap.remove(key);
                }
            }
        }
    }

}
