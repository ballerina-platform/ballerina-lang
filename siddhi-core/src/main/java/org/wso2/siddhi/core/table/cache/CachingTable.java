package org.wso2.siddhi.core.table.cache;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.*;
import org.wso2.siddhi.core.event.in.InStateEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;

import java.util.Iterator;
import java.util.List;

public class CachingTable {

    static final Logger log = Logger.getLogger(CachingTable.class);

    private SiddhiList<StreamEvent> list;
    private String elementId;
    private String tableId;
    private CacheManager cacheManager;
    private int cacheLimit;
    private boolean isFullyLoaded;

    public static final int DEFAULT_CACHE_SIZE = 4096;

    public static final String CACHING_ALGO_BASIC = "basic";
    public static final String CACHING_ALGO_LRU = "lru";
    public static final String CACHING_ALGO_LFU = "lfu";

    public CachingTable(String tableId, String cachingAlgorithm, String cacheSize, SiddhiContext siddhiContext) {
        this.elementId = siddhiContext.getElementIdGenerator().createNewId();
        this.list = new SiddhiList<StreamEvent>();
        this.tableId = tableId;
        cacheLimit = DEFAULT_CACHE_SIZE;
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
    }

    public synchronized void add(StreamEvent streamEvent) {
        cacheManager.add(streamEvent);
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public synchronized void addAll(List<StreamEvent> streamEvents) {
        for (StreamEvent event: streamEvents) {
            cacheManager.add(event);
        }
    }

    public synchronized void delete(StreamEvent streamEvent, ConditionExecutor conditionExecutor) {
        if (conditionExecutor != null) {
            if (streamEvent instanceof AtomicEvent) {
                Iterator<StreamEvent> iterator = list.iterator();
                StateEvent stateEvent = new InStateEvent(new StreamEvent[]{streamEvent, null});
                while (iterator.hasNext()) {
                    StreamEvent tableStreamEvent = iterator.next();
                    stateEvent.setStreamEvent(1, tableStreamEvent);
                    if (conditionExecutor.execute(stateEvent)) {
                        iterator.remove();
                        cacheManager.delete(tableStreamEvent);
                    }
                }
            } else {
                Iterator<StreamEvent> iterator = list.iterator();
                StateEvent stateEvent = new InStateEvent(new StreamEvent[2]);
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    stateEvent.setStreamEvent(0, ((ListEvent) streamEvent).getEvent(i));
                    while (iterator.hasNext()) {
                        StreamEvent tableStreamEvent = iterator.next();
                        stateEvent.setStreamEvent(1, tableStreamEvent);
                        if (conditionExecutor.execute(stateEvent)) {
                            iterator.remove();
                            cacheManager.delete(tableStreamEvent);
                        }
                    }
                }
            }
        } else {
            list.clear();
        }
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public synchronized void update(StreamEvent streamEvent, ConditionExecutor conditionExecutor, int[] attributeUpdateMappingPosition) {
        Iterator<StreamEvent> iterator = list.iterator();
        StateEvent stateEvent = new InStateEvent(new StreamEvent[2]);
        if (streamEvent instanceof AtomicEvent) {
            stateEvent.setStreamEvent(0, streamEvent);
            while (iterator.hasNext()) {
                StreamEvent tableStreamEvent = iterator.next();
                stateEvent.setStreamEvent(1, tableStreamEvent);
                if (conditionExecutor != null) {
                    if (conditionExecutor.execute(stateEvent)) {
                        for (int i = 0, size = attributeUpdateMappingPosition.length; i < size; i++) {
                            ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i]] = ((Event) streamEvent).getData()[i];
                        }
                        cacheManager.update(tableStreamEvent);
                    }
                } else {
                    for (int i = 0, size = attributeUpdateMappingPosition.length; i < size; i++) {
                        ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i]] = ((Event) streamEvent).getData()[i];
                    }
                    cacheManager.update(tableStreamEvent);
                }
            }
        } else {
            while (iterator.hasNext()) {
                StreamEvent tableStreamEvent = iterator.next();
                stateEvent.setStreamEvent(1, tableStreamEvent);
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    stateEvent.setStreamEvent(0, ((ListEvent) streamEvent).getEvent(i));
                    if (conditionExecutor != null) {
                        if (conditionExecutor.execute(stateEvent)) {
                            for (int i1 = 0, size1 = attributeUpdateMappingPosition.length; i1 < size1; i1++) {
                                ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i1]] = ((Event) streamEvent).getData()[i1];
                            }
                            cacheManager.update(tableStreamEvent);
                        }
                    } else {
                        for (int i1 = 0, size1 = attributeUpdateMappingPosition.length; i1 < size1; i1++) {
                            ((RemoveEvent) tableStreamEvent).getData()[attributeUpdateMappingPosition[i1]] = ((Event) streamEvent).getData()[i1];
                        }
                        cacheManager.update(tableStreamEvent);
                    }
                }
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("list " + elementId + " size " + list.size());
        }
    }

    public synchronized void invalidateCache() {
        list.clear();
    }

    public synchronized boolean contains(AtomicEvent atomicEvent, ConditionExecutor conditionExecutor) {
        if (conditionExecutor != null) {
            Iterator<StreamEvent> iterator = list.iterator();
            StateEvent stateEvent = new InStateEvent(new StreamEvent[]{(StreamEvent) atomicEvent, null});
            while (iterator.hasNext()) {
                StreamEvent tableStreamEvent = iterator.next();
                stateEvent.setStreamEvent(1, tableStreamEvent);
                if (conditionExecutor.execute(stateEvent)) {
                    cacheManager.read(tableStreamEvent);
                    return true;
                }
            }
        }
        return false;
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

    public int getCacheLimit() {
        return this.cacheLimit;
    }

    public boolean isFullyLoaded() {
        return isFullyLoaded;
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        isFullyLoaded = fullyLoaded;
    }
}
