/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.transport.http.netty.contractimpl.common.states;

import org.wso2.transport.http.netty.contractimpl.listener.states.http2.ListenerState;
import org.wso2.transport.http.netty.contractimpl.sender.states.http2.SenderState;

/**
 * Context class to manipulate current state of the HTTP/2 message.
 *
 * @since 6.0.241
 */
public class Http2MessageStateContext {

    private ListenerState listenerState;
    private SenderState senderState;
    // Keeps the headers state to avoid sending headers multiple times in bidirectional streaming use case.
    private boolean headersSent;

    /**
     * Set the given listener state to the context.
     *
     * @param state the current state which represents the flow of packets receiving
     */
    public void setListenerState(ListenerState state) {
        this.listenerState = state;
    }

    /**
     * Get the given listener state from the context.
     *
     * @return the current state which represents the flow of packets receiving
     */
    public ListenerState getListenerState() {
        return listenerState;
    }

    /**
     * Get the given sender state from the context.
     *
     * @return the current state which represents the flow of packets sending
     */
    public SenderState getSenderState() {
        return senderState;
    }

    /**
     * Set the given sender state to the context.
     *
     * @param senderState the current state which represents the flow of packets sending
     */
    public void setSenderState(SenderState senderState) {
        this.senderState = senderState;
    }

    /**
     * Returns whether headers are sent or not.
     * @return true, if headers are already sent. false, otherwise.
     */
    public boolean isHeadersSent() {
        return headersSent;
    }

    /**
     * Set true when headers are sent by sender.
     *
     * @param headersSent true, when headers are sent.
     */
    public void setHeadersSent(boolean headersSent) {
        this.headersSent = headersSent;
    }
}
