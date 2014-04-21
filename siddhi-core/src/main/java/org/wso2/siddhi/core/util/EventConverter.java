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
package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.remove.RemoveEvent;

public class EventConverter {

    public static RemoveEvent[] toRemoveEventArray(Event[] events, int activeEvents, long expireTime) {
        RemoveEvent[] removeEvents = new RemoveEvent[activeEvents];
        for (int i = 0; i < activeEvents; i++) {
            removeEvents[i] = new RemoveEvent(events[i], expireTime);
        }
        return removeEvents;
    }

//    public static Collection<? extends RemoveEvent> toRemoveEventList(List<InEvent> newEventList, long expireTime) {
//        ArrayList<RemoveEvent> removeEvents = new ArrayList<RemoveEvent>(newEventList.size());
//        for (InEvent aNewEventList : newEventList) {
//            removeEvents.add(new RemoveEvent(aNewEventList, expireTime));
//        }
//        return removeEvents;
//    }
}
