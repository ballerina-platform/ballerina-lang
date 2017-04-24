/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
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

public class HazelcastPrimaryKeyEventHolder implements EventHolder, Map<Object, StreamEvent> {

    private IMap<Object, StreamEvent> storeEventMap;
    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter;
    private int indexPosition;
    private String indexAttribute;

    public HazelcastPrimaryKeyEventHolder(IMap storeEventMap, StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter, int indexPosition, String indexAttribute) {
        this.storeEventMap = storeEventMap;
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
            storeEventMap.put(streamEvent.getOutputData()[indexPosition], streamEvent);
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
        return storeEventMap.size();
    }

    @Override
    public boolean isEmpty() {
        return storeEventMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return storeEventMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return containsValue(value);
    }

    @Override
    public StreamEvent get(Object key) {
        return storeEventMap.get(key);
    }

    @Override
    public StreamEvent put(Object key, StreamEvent value) {
        return storeEventMap.put(key, value);
    }


    public StreamEvent replace(Object key, StreamEvent value) {
        return storeEventMap.replace(key, value);
    }

    @Override
    public StreamEvent remove(Object key) {
        return storeEventMap.remove(key);
    }

    @Override
    public void putAll(Map<?, ? extends StreamEvent> m) {
        storeEventMap.putAll(m);
    }

    @Override
    public void clear() {
        storeEventMap.clear();
    }

    @Override
    public Set<Object> keySet() {
        return storeEventMap.keySet();
    }

    @Override
    public Collection<StreamEvent> values() {
        return storeEventMap.values();
    }

    @Override
    public Set<Entry<Object, StreamEvent>> entrySet() {
        return storeEventMap.entrySet();
    }
}
