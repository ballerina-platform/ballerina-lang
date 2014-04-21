/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.util.collection.queue;

import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.NoIdentifierException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SiddhiQueueGrid<T> extends SiddhiQueue<T> {
    static final Logger log = Logger.getLogger(SiddhiQueueGrid.class);

    protected IMap<String, T> map;
    protected IAtomicLong firstIndex;
    protected IAtomicLong lastIndex;
    protected String elementId;
    protected SiddhiContext siddhiContext;
    protected boolean async = true;


    public SiddhiQueueGrid(String elementId, SiddhiContext siddhiContext, boolean async) {
        this.async = async;
        this.siddhiContext = siddhiContext;
        if (elementId == null) {
            throw new NoIdentifierException(this.getClass().getSimpleName()+" elementId cannot be null");
        }
        this.elementId = elementId + "-"+this.getClass().getSimpleName();

        firstIndex = siddhiContext.getHazelcastInstance().getAtomicLong(this.elementId + "-FirstIndex");
        lastIndex = siddhiContext.getHazelcastInstance().getAtomicLong(this.elementId + "-LastIndex");
        map = siddhiContext.getHazelcastInstance().getMap(this.elementId);
    }


    public synchronized void put(T t) {
        if (log.isDebugEnabled()) {
            log.debug("Put to SchedulerQueueGrid of elementId:" + elementId + " " + t + " having :" + map.size());
        }
        if (async) {
            map.putAsync(String.valueOf(lastIndex.incrementAndGet()), t);
        } else {
            map.put(String.valueOf(lastIndex.incrementAndGet()), t);
        }
    }


    public synchronized T poll() {
        if (!map.isEmpty()) {
            T t = null;
            try {
                t = map.removeAsync(String.valueOf(firstIndex.incrementAndGet())).get();
            } catch (Exception e) {
                log.error(e);
            }
            if (log.isDebugEnabled()) {
                log.debug("Poll to SchedulerQueueGrid of elementId:" + elementId + " " + t);
            }
            return t;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Poll to SchedulerQueueGrid of elementId:" + elementId  + " " + null);
            }
            return null;
        }

    }

    protected void removeFirst() {
        if (async) {
            map.removeAsync(String.valueOf(firstIndex.incrementAndGet()));
        } else {
            map.remove(String.valueOf(firstIndex.incrementAndGet()));
        }
    }

    public synchronized T peek() {
        if (!map.isEmpty()) {
            try {
                return map.getAsync(String.valueOf(firstIndex.get() + 1)).get();
            } catch (Exception e) {
                log.error(e);
                return null;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Poll to SchedulerQueueGrid of elementId:" + elementId + " " + null);
            }
            return null;
        }
    }

    public synchronized Iterator<T> iterator() {
        return map.values().iterator();
    }

    public synchronized Iterator<T> iterator(String condition) {

        if (condition.trim().equals("*")) {
            return map.values().iterator();
        }
        return map.values(new SqlPredicate(condition)).iterator();
    }

    public Object[] currentState() {
        Map<String, T> tempMap = new HashMap<String, T>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            tempMap.put(entry.getKey(), entry.getValue());
        }
        if (log.isDebugEnabled()) {
            log.debug("map size bring persisted " + map.size());
        }
        //todo may be we throw an error saying we don't support for distributed cases
        return new Object[]{firstIndex.get(), lastIndex.get(), map.getName(), tempMap};
    }

    public void restoreState(Object[] objects) {
        firstIndex.set((Long) objects[0]);
        lastIndex.set((Long) objects[1]);
        map.putAll((Map<String, T>) objects[3]);
    }

    public int size() {
        long firstIndexValue = firstIndex.get();
        long lastIndexValue = lastIndex.get();
        if (firstIndexValue <= lastIndexValue) {
            return (int) (lastIndexValue - firstIndexValue);
        } else {
            return (int) ((Long.MAX_VALUE - firstIndexValue) + (lastIndexValue - Long.MIN_VALUE));
        }
    }

}
