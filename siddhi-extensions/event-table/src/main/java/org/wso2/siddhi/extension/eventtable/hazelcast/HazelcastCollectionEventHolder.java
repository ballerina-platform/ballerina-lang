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

import com.hazelcast.core.IList;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.table.holder.EventHolder;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by suho on 5/22/16.
 */
public class HazelcastCollectionEventHolder implements EventHolder, Collection<StreamEvent> {

    private IList<StreamEvent> storeEventList;
    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter = new ZeroStreamEventConverter();

    public HazelcastCollectionEventHolder(IList storeEventList, StreamEventPool tableStreamEventPool,
                                          StreamEventConverter eventConverter) {
        this.storeEventList = storeEventList;
        this.tableStreamEventPool = tableStreamEventPool;
        this.eventConverter = eventConverter;
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            ComplexEvent complexEvent = addingEventChunk.next();
            StreamEvent streamEvent = tableStreamEventPool.borrowEvent();
            eventConverter.convertComplexEvent(complexEvent, streamEvent);
            storeEventList.add(streamEvent);
        }
    }

    @Override
    public int size() {
        return storeEventList.size();
    }

    @Override
    public boolean isEmpty() {
        return storeEventList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return storeEventList.contains(o);
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return storeEventList.iterator();
    }

    @Override
    public Object[] toArray() {
        return storeEventList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return storeEventList.toArray(a);
    }

    @Override
    public boolean add(StreamEvent streamEvent) {
        return storeEventList.add(streamEvent);
    }

    @Override
    public boolean remove(Object o) {
        return storeEventList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return storeEventList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends StreamEvent> c) {
        return storeEventList.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return storeEventList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return storeEventList.retainAll(c);
    }

    @Override
    public void clear() {
        storeEventList.clear();
    }

    public StreamEvent get(int i) {
        return storeEventList.get(i);
    }

    public void set(int i, StreamEvent storeEvent) {
        storeEventList.set(i, storeEvent);
    }
}
