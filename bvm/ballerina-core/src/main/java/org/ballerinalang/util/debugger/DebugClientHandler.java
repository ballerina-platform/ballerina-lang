/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.debugger;

import io.netty.channel.Channel;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.util.debugger.dto.MessageDTO;

import java.util.Map;

/**
 * Represents a debug client handler which is used to communicate with the debug client.
 *
 * @since 0.95.4
 */
public interface DebugClientHandler {

    /**
     * Called when adding a context.
     *
     * @param ctx to be added to the map.
     */
    void addWorkerContext(WorkerExecutionContext ctx);

    /**
     * Method to get worker context given the workerId.
     *
     * @param workerId of the thread.
     * @return  worker context.
     */
    WorkerExecutionContext getWorkerContext(String workerId);

    /**
     * Method to get all worker contexts.
     *
     * @return methodContext map.
     */
    Map<String, WorkerExecutionContext> getAllWorkerContexts();

    /**
     * Method to set web socket channel required for communication.
     *
     * @param channel required for communication.
     * @throws DebugException in case of failure.
     */
    void setChannel(Channel channel) throws DebugException;

    /**
     * Method to remove web socket channel.
     */
    void clearChannel();

    /**
     * Method to check whether channel is active or not.
     *
     * @return true if active.
     */
    boolean isChannelActive();

    /**
     * Called when main program exit.
     */
    void notifyExit();

    /**
     * Called when executor hits a break point.
     *
     * @param message to send to the client.
     */
    void notifyHalt(MessageDTO message);

    /**
     * Send a custom message to the client.
     *
     * @param message message to send to the client
     */
    void sendCustomMsg(MessageDTO message);
}
