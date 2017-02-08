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

    private IList<StreamEvent> candidateDataList;
    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter = new ZeroStreamEventConverter();

    public HazelcastCollectionEventHolder(IList candidateDataList, StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter) {
        this.candidateDataList = candidateDataList;
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
            candidateDataList.add(streamEvent);
        }
    }

    @Override
    public int size() {
        return candidateDataList.size();
    }

    @Override
    public boolean isEmpty() {
        return candidateDataList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return candidateDataList.contains(o);
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return candidateDataList.iterator();
    }

    @Override
    public Object[] toArray() {
        return candidateDataList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return candidateDataList.toArray(a);
    }

    @Override
    public boolean add(StreamEvent streamEvent) {
        return candidateDataList.add(streamEvent);
    }

    @Override
    public boolean remove(Object o) {
        return candidateDataList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return candidateDataList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends StreamEvent> c) {
        return candidateDataList.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return candidateDataList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return candidateDataList.retainAll(c);
    }

    @Override
    public void clear() {
        candidateDataList.clear();
    }

    public StreamEvent get(int i) {
        return candidateDataList.get(i);
    }

    public void set(int i, StreamEvent candidateEvent) {
        candidateDataList.set(i, candidateEvent);
    }
}
