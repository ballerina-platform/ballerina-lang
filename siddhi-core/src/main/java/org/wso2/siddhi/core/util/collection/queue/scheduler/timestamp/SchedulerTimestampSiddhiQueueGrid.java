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
package org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp;

import com.hazelcast.query.SqlPredicate;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerElement;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueueGrid;

import java.util.*;

public class SchedulerTimestampSiddhiQueueGrid<T extends StreamEvent> extends SchedulerSiddhiQueueGrid<T> implements ISchedulerTimestampSiddhiQueue<T> {
    static final Logger log = Logger.getLogger(SchedulerTimestampSiddhiQueueGrid.class);
    private EntrySetComparator entrySetComparator = new EntrySetComparator();

    public SchedulerTimestampSiddhiQueueGrid(String elementId, SchedulerElement schedulerElement,
                                             SiddhiContext siddhiContext, boolean async) {
        super(elementId, schedulerElement, siddhiContext, async);
    }

    public synchronized Collection<T> poll(long expiryTime) {
        if (!map.isEmpty()) {
            try {
                List<T> resultList = new ArrayList<T>();
                SqlPredicate sqlPredicate = new SqlPredicate(" expiryTime <= " + expiryTime + ")");
                ArrayList<Map.Entry<String, T>> results = new ArrayList<Map.Entry<String, T>>(map.entrySet(sqlPredicate));
                Collections.sort(results, entrySetComparator);
                for (Map.Entry<String, T> result : results) {
                    if (result.getKey().equals(String.valueOf(firstIndex.get() + 1))) {
                        resultList.add(result.getValue());
                        removeFirst();
                    } else {
                        return resultList;
                    }
                }
                if (resultList.size() > 0) {
                    return resultList;
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.error(e);
                return null;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Poll to SchedulerTimestampQueueGrid elementId:" + elementId + " " + null);
            }
            return null;
        }

    }

    public class EntrySetComparator implements Comparator<Map.Entry<String, T>> {

        @Override
        public int compare(Map.Entry<String, T> o1, Map.Entry<String, T> o2) {
            long o11 = Long.parseLong(o1.getKey());
            long o21 = Long.parseLong(o2.getKey());
            return (o11 > o21 ? 1 : (o11 == o21 ? 0 : -1));
        }
    }
}
