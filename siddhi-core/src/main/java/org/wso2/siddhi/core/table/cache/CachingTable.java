package org.wso2.siddhi.core.table.cache;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class CachingTable {

    static final Logger log = Logger.getLogger(CachingTable.class);
    private final LinkedList<StreamEvent> list;
    private String elementId;
    private String tableId;
    private CacheManager cacheManager;

    public static final long DEFAULT_CACHE_SIZE = 4096;

    public static final String CACHING_ALGO_BASIC = "basic";
    public static final String CACHING_ALGO_LRU = "lru";
    public static final String CACHING_ALGO_LFU = "lfu";

    private final StreamEventCloner streamEventCloner;
    private final StreamEventPool streamEventPool;


    public CachingTable(String tableId, String cachingAlgorithm, String cacheSize, ExecutionPlanContext executionPlanContext, TableDefinition tableDefinition) {
        this.elementId = executionPlanContext.getElementIdGenerator().createNewId();
        this.list = new LinkedList<StreamEvent>();
        this.tableId = tableId;
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
            //this.cacheManager = new BasicCacheManager(list, cacheLimit);
        }

        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            metaStreamEvent.addOutputData(attribute);
        }

        streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        streamEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
    }

    public synchronized void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            StreamEvent streamEvent = addingEventChunk.next();
            StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
            cacheManager.add(clonedEvent);
        }

        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }


    public synchronized void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Operator operator) {

        operator.delete(deletingEventChunk, list);
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StreamEvent deletingEvent = deletingEventChunk.next();
            StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(deletingEvent);
            cacheManager.delete(clonedEvent);
        }

        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public synchronized void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Operator operator, int[] mappingPosition) {

        operator.update(updatingEventChunk, list, mappingPosition);
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StreamEvent updatingEvent = updatingEventChunk.next();
            StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(updatingEvent);
            cacheManager.update(clonedEvent);
        }
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }


    public synchronized void invalidateCache() {
        list.clear();
    }

    public Iterator<StreamEvent> iterator() {
        return list.iterator();
    }

    public Iterator<StreamEvent> iterator(String SQLPredicate) {
        return list.iterator();
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getTableId() {
        return tableId;
    }
}
