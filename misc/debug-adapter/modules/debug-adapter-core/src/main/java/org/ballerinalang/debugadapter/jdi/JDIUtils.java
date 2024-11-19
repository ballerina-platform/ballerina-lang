/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.jdi;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import org.ballerinalang.debugadapter.ExecutionContext;

/**
 * JDI utility functions.
 */
public class JDIUtils {

    /**
     * When invoking methods in the remote JVM, it can cause deadlocks if 'invokeMethod' is called from the
     * client's event handler thread. In that case, the thread will be waiting for the invokeMethod to complete
     * and won't read the EventSet that comes in for the new event. If this new EventSet is in 'SUSPEND_ALL'
     * mode, then a deadlock will occur because no one will resume the EventSet.
     * <p>
     * Therefore, to avoid this, we are disabling possible event requests (only class prepare and breakpoint requests
     * for now) before invoking the method.
     */
    public static void disableJDIRequests(ExecutionContext context) {
        EventRequestManager eventManager = context.getEventManager();
        eventManager.classPrepareRequests().forEach(EventRequest::disable);
        eventManager.breakpointRequests().forEach(EventRequest::disable);
    }

    /**
     * Used to re-enable the event requests after the method invocation is completed.
     */
    public static void enableJDIRequests(ExecutionContext context) {
        EventRequestManager eventManager = context.getEventManager();
        eventManager.breakpointRequests().forEach(EventRequest::enable);
    }

    /**
     * Sleep for the given number of milliseconds.
     *
     * @param millis number of milliseconds to sleep
     */
    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
