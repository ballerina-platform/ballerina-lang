/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.transport.http.netty.message;

import io.netty.handler.codec.http.HttpContent;

/**
 * Allows listeners to register and get notified.
 */
public interface Observable {

    /**
     * Set listener interested for message events.
     * @param listener for message
     */
    void setListener(Listener listener);

    /**
     * Remove listener from the observable.
     */
    void removeListener();

    /**
     * Notify when content is added to message.
     * @param content of the message
     */
    void notifyAddListener(HttpContent content);

    /**
     * Notify when content is removed from message.
     * @param content of the message
     */
    void notifyGetListener(HttpContent content);

    void notifyReadInterest();

    Listener getListener();
}
