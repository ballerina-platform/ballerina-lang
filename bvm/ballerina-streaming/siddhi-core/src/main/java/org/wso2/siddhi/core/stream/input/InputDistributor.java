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

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link InputProcessor} which inject the event to a set of Input Processors.
 */
public class InputDistributor implements InputProcessor {

    private List<InputProcessor> inputProcessors = new ArrayList<InputProcessor>();

    @Override
    public void send(Event event, int streamIndex) {
        inputProcessors.get(streamIndex).send(event, streamIndex);
    }

    @Override
    public void send(Event[] events, int streamIndex) {
        inputProcessors.get(streamIndex).send(events, streamIndex);
    }

    @Override
    public void send(List<Event> events, int streamIndex) {
        inputProcessors.get(streamIndex).send(events, streamIndex);
    }

    @Override
    public void send(long timestamp, Object[] data, int streamIndex) {
        inputProcessors.get(streamIndex).send(timestamp, data, streamIndex);
    }

    public void addInputProcessor(InputProcessor inputProcessor) {
        inputProcessors.add(inputProcessor);

    }
}
