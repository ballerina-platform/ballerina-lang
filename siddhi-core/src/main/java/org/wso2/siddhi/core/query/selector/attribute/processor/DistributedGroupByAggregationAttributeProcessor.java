/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.selector.attribute.processor;

import com.hazelcast.core.IMap;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.query.selector.attribute.factory.OutputAttributeAggregatorFactory;
import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class DistributedGroupByAggregationAttributeProcessor extends AbstractAggregationAttributeProcessor implements GroupByAttributeProcessor {

    static final Logger log = Logger.getLogger(DistributedGroupByAggregationAttributeProcessor.class);

    private IMap<String, OutputAttributeAggregator> distributedAggregatorMap;
    private volatile boolean lockedAcquired = false;
    private volatile Lock lock;
    private volatile Map<String, OutputAttributeAggregator> tempAggregatorMap = new HashMap<String, OutputAttributeAggregator>();

    public DistributedGroupByAggregationAttributeProcessor(Expression[] expressions, List<QueryEventSource> queryEventSourceList, OutputAttributeAggregatorFactory outputAttributeAggregatorFactory, String elementId, SiddhiContext siddhiContext) {
        super(expressions, queryEventSourceList, outputAttributeAggregatorFactory, elementId, siddhiContext);
        lock = siddhiContext.getHazelcastInstance().getLock(elementId+"-lock");
        distributedAggregatorMap = siddhiContext.getHazelcastInstance().getMap(elementId + "-GroupByMap");
    }

    public synchronized Object process(AtomicEvent event, String key) {

        OutputAttributeAggregator currentOutputAttributeAggregator = null;
        if (!lockedAcquired) {
            lock.lock();
            try {
                currentOutputAttributeAggregator = distributedAggregatorMap.getAsync(key).get();
                if (currentOutputAttributeAggregator == null) {
                    currentOutputAttributeAggregator = sampleOutputAttributeAggregator.newInstance();
                    siddhiContext.addEternalReferencedHolder(currentOutputAttributeAggregator);
                }
            } catch (Exception e) {
                log.error(e);
            }
        } else {
            currentOutputAttributeAggregator = tempAggregatorMap.get(key);
            if (currentOutputAttributeAggregator == null) {
                try {
                    currentOutputAttributeAggregator = distributedAggregatorMap.getAsync(key).get();
                    if (currentOutputAttributeAggregator == null) {
                        currentOutputAttributeAggregator = sampleOutputAttributeAggregator.newInstance();
                        siddhiContext.addEternalReferencedHolder(currentOutputAttributeAggregator);
                    }
                    tempAggregatorMap.put(key, currentOutputAttributeAggregator);
                } catch (Exception e) {
                    log.error(e);
                }
            }

        }
        Object value = process(event, currentOutputAttributeAggregator);
        if (!lockedAcquired) {
//            distributedAggregatorMap.putAndUnlock(key, currentOutputAttributeAggregator);
            distributedAggregatorMap.put(key, currentOutputAttributeAggregator);
            lock.unlock();
        }
        return value;
    }


    public Object[] currentState() {
        Map<String, OutputAttributeAggregator> tempMap = new HashMap<String, OutputAttributeAggregator>();
        for (Map.Entry<String, OutputAttributeAggregator> entry : distributedAggregatorMap.entrySet()) {
            tempMap.put(entry.getKey(), entry.getValue());
        }
        //todo may be we throw an error saying we don't support for distributed cases
        return new Object[]{tempMap};
    }

    public void restoreState(Object[] objects) {
        distributedAggregatorMap.putAll((Map<String, OutputAttributeAggregator>) objects[0]);
    }

    @Override
    public synchronized void lock() {
        if (!lockedAcquired) {
            lock.lock();
//            lockedAcquired = distributedAggregatorMap.lockMap(Integer.MAX_VALUE, TimeUnit.SECONDS);
            lockedAcquired = true;
            if (!lockedAcquired) {
                log.warn("Map lock for elementId " + elementId + " could not be acquired within " + Integer.MAX_VALUE + " Secs");
            }
        }
    }

    @Override
    public synchronized void unlock() {
        if (lockedAcquired) {
            for (Map.Entry<String, OutputAttributeAggregator> entry : tempAggregatorMap.entrySet()) {
                distributedAggregatorMap.putAsync(entry.getKey(), entry.getValue());
            }
            tempAggregatorMap.clear();
            lockedAcquired = false;
//            distributedAggregatorMap.unlockMap();
            lock.unlock();
        }
    }

}
