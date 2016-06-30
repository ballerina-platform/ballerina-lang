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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import com.hazelcast.core.IMap;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.table.holder.EventHolder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by suho on 5/22/16.
 */
public class HazelcastPrimaryKeyEventHolder implements EventHolder, Map<Object, StreamEvent> {

    private IMap<Object, StreamEvent> candidateDataMap;
    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter;
    private int indexPosition;
    private String indexAttribute;

    public HazelcastPrimaryKeyEventHolder(IMap candidateDataMap, StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter, int indexPosition, String indexAttribute) {
        this.candidateDataMap = candidateDataMap;
        this.tableStreamEventPool = tableStreamEventPool;
        this.eventConverter = eventConverter;
        this.indexPosition = indexPosition;
        this.indexAttribute = indexAttribute;
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            ComplexEvent complexEvent = addingEventChunk.next();
            StreamEvent streamEvent = tableStreamEventPool.borrowEvent();
            eventConverter.convertComplexEvent(complexEvent, streamEvent);
            candidateDataMap.put(streamEvent.getOutputData()[indexPosition], streamEvent);
        }
    }

    public String getIndexAttribute() {
        return indexAttribute;
    }

    public int getIndexPosition() {
        return indexPosition;
    }

    @Override
    public int size() {
        return candidateDataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return candidateDataMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return candidateDataMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return containsValue(value);
    }

    @Override
    public StreamEvent get(Object key) {
        return candidateDataMap.get(key);
    }

    @Override
    public StreamEvent put(Object key, StreamEvent value) {
        return candidateDataMap.put(key, value);
    }


    public StreamEvent replace(Object key, StreamEvent value) {
        return candidateDataMap.replace(key, value);
    }

    @Override
    public StreamEvent remove(Object key) {
        return candidateDataMap.remove(key);
    }

    @Override
    public void putAll(Map<?, ? extends StreamEvent> m) {
        candidateDataMap.putAll(m);
    }

    @Override
    public void clear() {
        candidateDataMap.clear();
    }

    @Override
    public Set<Object> keySet() {
        return candidateDataMap.keySet();
    }

    @Override
    public Collection<StreamEvent> values() {
        return candidateDataMap.values();
    }

    @Override
    public Set<Entry<Object, StreamEvent>> entrySet() {
        return candidateDataMap.entrySet();
    }
}
