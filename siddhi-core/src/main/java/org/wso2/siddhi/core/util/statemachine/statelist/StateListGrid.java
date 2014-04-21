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
package org.wso2.siddhi.core.util.statemachine.statelist;

import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StateEvent;

import java.util.*;

public class StateListGrid extends StateList<StateEvent> {
    static final Logger log = Logger.getLogger(StateListGrid.class);
    private IMap<String, StateEvent> map;
    private SiddhiContext siddhiContext;
    private IAtomicLong inited;
    private ValueComparator valueComparator = new ValueComparator();
//    private long firstIndex = Long.MIN_VALUE;
//    private long lastIndex = Long.MIN_VALUE;

    public StateListGrid(String id, SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
        if (id != null) {
            id = "SchedulerQueueGrid-" + id;
        } else {
            id = "SchedulerQueueGrid-" + UUID.randomUUID();
        }
        if(log.isDebugEnabled()){
            log.debug("StateListGrid created with id: "+id);
        }
        map = siddhiContext.getHazelcastInstance().getMap(id);
        inited=siddhiContext.getHazelcastInstance().getAtomicLong(id);
    }



//    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();

    public synchronized void put(StateEvent stateEvent) {
//        System.out.println(Thread.currentThread().getName()+" put f:"+firstIndex+" l:"+lastIndex);
        map.put(stateEvent.getEventId(), stateEvent);
//        if (lastIndex == Long.MAX_VALUE) {
//            lastIndex = Long.MIN_VALUE;
//        } else {
//            lastIndex++;
//        }

    }

    public  boolean isInited() {
        return inited.incrementAndGet() != 1;
    }


//    public synchronized StateEvent poll() {
////        System.out.println(Thread.currentThread().getName()+" poll f:"+firstIndex+" l:"+lastIndex);
//
////        int count = 0;
////        if (lastIndex != firstIndex) {
//            StateEvent t = map.remove(firstIndex + "");
////            T t = map.remove(firstIndex + "");
////            while (t == null && count < 5) {
////                t = map.remove(firstIndex + "");
////                count++;
////            }
//            if (firstIndex == Long.MAX_VALUE) {
//                firstIndex = Long.MIN_VALUE;
//            } else {
//                firstIndex++;
//            }
//            return t;
//        } else {
//            return null;
//        }
//    }


//    public synchronized T take() throws InterruptedException {
//        return linkedBlockingQueue.take();
//    }

//    public synchronized StateEvent peek() {
////        System.out.println(Thread.currentThread().getName()+" peek f:"+firstIndex+" l:"+lastIndex);
////        int count = 0;
//        if (lastIndex != firstIndex) {
//            return  map.get(firstIndex + "");
////            StateEvent t = map.get(firstIndex + "");
////            while (t == null && count < 5) {
////                t = map.get(firstIndex + "");
////                count++;
////            }
////            return t;
//        } else {
//            return null;
//        }
//    }

    public synchronized Iterator<StateEvent> iterator() {
        return map.values().iterator();
    }

    public synchronized Iterator<StateEvent> iterator(String condition) {

        if (condition.trim().equals("*")) {
            return map.values().iterator();
        }
        return map.values(new SqlPredicate(condition)).iterator();
    }


    public Object[] currentState() {
        Map<String, StateEvent> tempMap = new HashMap<String, StateEvent>();
        for (Map.Entry<String, StateEvent> entry : map.entrySet()) {
            tempMap.put(entry.getKey(), entry.getValue());
        }
        return new Object[]{map.getName(), tempMap};
    }

    public void restoreState(Object[] objects) {
//        firstIndex= (Long)objects[0];
//        lastIndex= (Long)objects[1];
        map = siddhiContext.getHazelcastInstance().getMap((String) objects[0]);
        map.putAll((Map<String, StateEvent>) objects[1]);
    }


    public void clear() {
        map.clear();
//        firstIndex = Long.MIN_VALUE;
//        lastIndex = Long.MIN_VALUE;

    }

    public void addAll(Object all) {
        map.putAll((Map<String, StateEvent>) all);
    }

    public Collection<StateEvent> getCollection() {
        List<StateEvent> stateEvents = new ArrayList<StateEvent>(map.values());
        Collections.sort(stateEvents, valueComparator);
        if(log.isDebugEnabled()){
            log.debug("Sorted StateEvents :"+stateEvents);
        }
        return stateEvents;
    }

    public Collection<StateEvent> getCollection(String condition) {
        Collection<StateEvent> collection;
        if (condition.trim().equals("*")) {
            collection = map.values();
        } else {
            collection = map.values(new SqlPredicate(condition));
        }
        List<StateEvent> stateEvents = new ArrayList<StateEvent>(collection);
        Collections.sort(stateEvents, valueComparator);
        return stateEvents;
    }

    public Object getAll() {
        return map;
    }

    public void update(StateEvent updateContainingStateEvent, int updatingState) {
        StateEvent oldEvent = map.get(updateContainingStateEvent.getEventId());
        if (oldEvent != null) {
            oldEvent.setStreamEvent(updatingState, updateContainingStateEvent.getStreamEvent(updatingState));
            oldEvent.setEventState(updateContainingStateEvent.getEventState());
            map.put(oldEvent.getEventId(), oldEvent);
        }
    }

    @Override
    public String toString() {
        return map.values().toString();
    }

    public void remove(StateEvent removingStateEvent) {
        map.remove(removingStateEvent.getEventId());
    }


    private class ValueComparator implements Comparator<StateEvent> {

        @Override
        public int compare(StateEvent o1, StateEvent o2) {
            return o1.getEventId().compareTo(o2.getEventId());
//            if ( > o2.getFirstEventTimeStamp()) {
//                return 1;
//            } else if (o1.getTimeStamp() == o2.getTimeStamp()) {
//                return 0;
//            } else {
//                return -1;
//            }
        }
    }

}
