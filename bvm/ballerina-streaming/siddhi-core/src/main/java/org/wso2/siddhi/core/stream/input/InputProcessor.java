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

package org.wso2.siddhi.core.stream.input;

import org.wso2.siddhi.core.event.Event;

import java.util.List;

/**
 * InputProcessor act as the adaptor between {@link InputHandler} and
 * {@link org.wso2.siddhi.core.stream.StreamJunction}. Input Handler will send incoming events into the processor.
 */
public interface InputProcessor {

    void send(Event event, int streamIndex);

    void send(Event[] events, int streamIndex);

    void send(List<Event> events, int streamIndex);

    void send(long timestamp, Object[] data, int streamIndex);

}

