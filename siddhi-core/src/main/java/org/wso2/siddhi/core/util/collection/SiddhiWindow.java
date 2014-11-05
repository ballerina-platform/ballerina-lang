/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.collection;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

/**
 * API to be used by all window data structure implementations.
 * (Ex: Disk backed window, Bloom filters). All such implementations must
 * implement this interface. Join/Window?pattern Processors will access
 * relevant concrete implementation via this interface
 */
public interface SiddhiWindow {

    /**
     * Initialize the data structure. Relevant configuration details will be
     * passed as Expressions. Implementation should be ready to accept events
     * after this step.
     *
     * @param expressions
     */
    public void init(Expression[] expressions);

    /**
     * Adds an event to underlying data structure. event can be of type
     * {@link org.wso2.siddhi.core.event.stream.StreamEvent StreamEvent} or
     * {@link org.wso2.siddhi.core.event.state.StateEvent StateEvent}.
     * Implementation should handle accordingly.
     *
     * @param event
     */
    public void add(ComplexEvent event);

    /**
     * Returns the last event of the window. Particular event should be removed
     * base on passed boolean value.
     *
     * @param remove Whether to remove the event from window
     * @return Last event. Null if window is empty
     */
    public ComplexEvent getLast(Boolean remove);

    /**
     * Find and returns an list of events which matches given parameters.
     *
     * @param attributeName Name of the attribute
     * @param value         Expected value. Implementation must handle concrete object type
     * @return Matched list of events or null if no match found
     */
    public List<ComplexEvent> find(String attributeName, Object value);

    /**
     * Removes a specific event if present. Returns null if no match occurs.
     *
     * @param event Event to be removed.
     */
    public void remove(ComplexEvent event);

    /**
     * Returns elements of data structure(window) as an array for persistence purposes
     *
     * @return events inside window as an array.
     */
    public Object[] currentState();

    /**
     * Re-populate data structure(window) with given set of Objects
     *
     * @param objects Set of event to be inserted into window
     */
    public void restoreState(Object[] objects);

    /**
     * Returns number of events which resides inside window.
     *
     * @return Number of Events
     */
    public int size();

    /**
     * Clean all used resources within implementation.(Object references, etc)
     */
    public void destroy();


}
