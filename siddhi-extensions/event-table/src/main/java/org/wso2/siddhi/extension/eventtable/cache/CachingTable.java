/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.eventtable.cache;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.LinkedList;

public class CachingTable {

    private static final Logger log = Logger.getLogger(CachingTable.class);
    private final LinkedList<StreamEvent> list;
    private final ZeroStreamEventConverter eventConverter;
    private final StreamEventPool streamEventPool;
    private String elementId;
    private CacheManager cacheManager;

    public static final long DEFAULT_CACHE_SIZE = 4096;

    public static final String CACHING_ALGO_LRU = "lru";
    public static final String CACHING_ALGO_LFU = "lfu";

    public CachingTable(String cachingAlgorithm, String cacheSize, ExecutionPlanContext executionPlanContext, TableDefinition tableDefinition) {
        this.elementId = executionPlanContext.getElementIdGenerator().createNewId();
        this.list = new LinkedList<StreamEvent>();
        long cacheLimit = DEFAULT_CACHE_SIZE;
        if (cacheSize != null) {
            try {
                cacheLimit = Integer.parseInt(cacheSize);
            } catch (Exception ex) {
                log.error("Can't parse the cache size. Creating cache with the default size.");
            }
        }
        if (CACHING_ALGO_LFU.equalsIgnoreCase(cachingAlgorithm)) {
            this.cacheManager = new LFUCacheManager(list, cacheLimit);
        } else if (CACHING_ALGO_LRU.equalsIgnoreCase(cachingAlgorithm)) {
            this.cacheManager = new LRUCacheManager(list, cacheLimit);
        } else {
            this.cacheManager = new BasicCacheManager(list, cacheLimit);
        }

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        eventConverter = new ZeroStreamEventConverter();

    }

    public void add(ComplexEvent complexEvent) {
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        eventConverter.convertStreamEvent(complexEvent, streamEvent);
        list.add(streamEvent);
        cacheManager.add(streamEvent);
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }


    public void delete(StreamEvent deletingEvent) {
        cacheManager.delete(deletingEvent);
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public void update(StreamEvent updatingEvent) {

        StreamEvent streamEvent = streamEventPool.borrowEvent();
        eventConverter.convertStreamEvent(updatingEvent, streamEvent);
        cacheManager.update(streamEvent);

        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public void overwriteOrAdd(StreamEvent updatingEvent) {
        if (cacheManager.isContains(updatingEvent)) {
            StreamEvent streamEvent = streamEventPool.borrowEvent();
            eventConverter.convertStreamEvent(updatingEvent, streamEvent);
            cacheManager.update(streamEvent);
        }else {
            StreamEvent streamEvent = streamEventPool.borrowEvent();
            eventConverter.convertStreamEvent(updatingEvent, streamEvent);
            list.add(streamEvent);
            cacheManager.add(streamEvent);
        }

        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public void contains(StreamEvent matchingEvent) {
        cacheManager.read(matchingEvent);
    }

    public LinkedList<StreamEvent> getCacheList() {
        return list;
    }

    public void invalidateCache() {
        cacheManager.invalidateCache();
    }

}
