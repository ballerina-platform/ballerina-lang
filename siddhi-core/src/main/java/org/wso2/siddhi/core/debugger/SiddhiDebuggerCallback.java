/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.debugger;

import org.wso2.siddhi.core.event.ComplexEvent;

/**
 * Callback to get notification about the events passing through the break points.
 */
public interface SiddhiDebuggerCallback {
    /**
     * Receive the events passing through the registered breakpoints.
     *
     * @param event         ComplexEvent waiting at the breakpoint
     * @param queryName     Name of the query where the current breakpoint is
     * @param queryTerminal IN or OUT terminal of the query
     * @param debugger      SiddhiDebugger to have control over the debug flow within the event caller
     */
    void debugEvent(ComplexEvent event, String queryName, SiddhiDebugger.QueryTerminal queryTerminal, SiddhiDebugger
            debugger);
}
