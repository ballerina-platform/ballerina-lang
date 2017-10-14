/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.input.source;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.ExceptionUtil;

import java.util.List;

/**
 * This implementation of {@link InputEventHandler} that wraps {@link InputHandler} class
 */
public class InputEventHandlerImpl implements InputEventHandler {

    private static final Logger LOG = Logger.getLogger(InputEventHandlerImpl.class);

    private final ThreadLocal<String[]> trpProperties;
    private String sourceType;
    private SiddhiAppContext siddhiAppContext;
    private InputHandler inputHandler;
    private List<AttributeMapping> transportMapping;

    InputEventHandlerImpl(InputHandler inputHandler, List<AttributeMapping> transportMapping,
                      ThreadLocal<String[]> trpProperties, String sourceType, SiddhiAppContext siddhiAppContext) {
        this.inputHandler = inputHandler;
        this.transportMapping = transportMapping;
        this.trpProperties = trpProperties;
        this.sourceType = sourceType;
        this.siddhiAppContext = siddhiAppContext;
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        try {
            String[] transportProperties = trpProperties.get();
            trpProperties.remove();
            for (int i = 0; i < transportMapping.size(); i++) {
                AttributeMapping attributeMapping = transportMapping.get(i);
                event.getData()[attributeMapping.getPosition()] = transportProperties[i];
            }
            inputHandler.send(event);
        } catch (RuntimeException e) {
            LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                    " Error in applying transport property mapping for '" + sourceType
                    + "' source at '" + inputHandler.getStreamId() + "' stream.", e);
        } finally {
            trpProperties.remove();
        }

    }

    @Override
    public void sendEvents(Event[] events) throws InterruptedException {
        try {
            String[] transportProperties = trpProperties.get();
            for (int i = 0; i < transportMapping.size(); i++) {
                AttributeMapping attributeMapping = transportMapping.get(i);
                for (Event event : events) {
                    event.getData()[attributeMapping.getPosition()] = transportProperties[i];
                }
            }
            inputHandler.send(events);
        } catch (RuntimeException e) {
            LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                    " Error in applying transport property mapping for '" + sourceType
                    + "' source at '" + inputHandler.getStreamId() + "' stream.", e);
        } finally {
            trpProperties.remove();
        }
    }
}
