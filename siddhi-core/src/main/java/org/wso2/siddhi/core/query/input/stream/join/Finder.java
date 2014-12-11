/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.input.stream.join;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;

/**
 * Created on 12/8/14.
 */
public class Finder {
    private StateEvent event = new StateEvent(2, 0, 0);
    private ExpressionExecutor expressionExecutor;
    private int candidateEventPosition;
    private int currentEventPosition;

    public Finder(ExpressionExecutor expressionExecutor, int candidateEventPosition, int currentEventPosition) {
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
        this.currentEventPosition = currentEventPosition;
    }

    public void setCurrentEvent(StreamEvent currentEvent) {
        this.event.setEvent(currentEventPosition, currentEvent);
    }

    public boolean execute(StreamEvent streamEvent) {
        event.setEvent(candidateEventPosition, streamEvent);
        boolean result = (Boolean) expressionExecutor.execute(event);
        event.setEvent(candidateEventPosition, null);
        return result;
    }

    public Finder cloneFinder() {
        return new Finder(expressionExecutor, candidateEventPosition, currentEventPosition);
    }
}
