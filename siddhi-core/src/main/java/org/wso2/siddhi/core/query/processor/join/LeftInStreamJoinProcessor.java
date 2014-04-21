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
package org.wso2.siddhi.core.query.processor.join;

import org.wso2.siddhi.core.event.*;
import org.wso2.siddhi.core.event.in.InListAtomicEvent;
import org.wso2.siddhi.core.event.in.InStateEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;

import java.util.concurrent.locks.Lock;

public class LeftInStreamJoinProcessor extends JoinProcessor {

    public LeftInStreamJoinProcessor(ConditionExecutor onConditionExecutor, boolean triggerEvent,
                                     boolean distributedProcessing, Lock lock, boolean fromDB) {
        super(onConditionExecutor, triggerEvent, distributedProcessing, lock, fromDB);
    }

    @Override
    protected boolean triggerEventTypeCheck(ComplexEvent complexEvent) {
        return complexEvent instanceof InStream;
    }

    @Override
    protected ListAtomicEvent createNewListAtomicEvent() {
        return new InListAtomicEvent();
    }

    protected StateEvent createNewEvent(ComplexEvent complexEvent, ComplexEvent complexEvent1) {
        return new InStateEvent(new Event[]{((Event) complexEvent), ((Event) complexEvent1)});
    }

}
