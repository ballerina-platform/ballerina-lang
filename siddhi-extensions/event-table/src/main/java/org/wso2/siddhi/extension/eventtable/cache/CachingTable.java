/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.extension.eventtable.cache;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.LinkedList;

public class CachingTable {

    private static final Logger log = Logger.getLogger(CachingTable.class);
    private final LinkedList<StreamEvent> list;
    private String elementId;
    private CacheManager cacheManager;

    public static final long DEFAULT_CACHE_SIZE = 4096;

    public static final String CACHING_ALGO_LRU = "lru";
    public static final String CACHING_ALGO_LFU = "lfu";

    private final StreamEventCloner streamEventCloner;


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

        StreamEventPool streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
    }

    public void add(StreamEvent streamEvent) {
        StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
        list.add(clonedEvent);
        cacheManager.add(clonedEvent);
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }


    public void delete(StreamEvent deletingEventChunk) {
        cacheManager.delete(deletingEventChunk);
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public void update(StreamEvent updatingEventChunk) {

        StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(updatingEventChunk);
        cacheManager.update(clonedEvent);

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

}
