/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;

/**
 * Java POJO representation of Siddhi Window definition.
 * The window must be defined in the following format:
 * 'define window([attributes]) function([parameters]) output [event type];'
 */
public class WindowDefinition extends StreamDefinition {

    private static final long serialVersionUID = 1L;

    /**
     * Internal window which has to be used within the EventWindow
     */
    private Window window;

    /**
     * Output event type of the window definition.
     * Default output event type is all events (both current and expired events).
     */
    private OutputStream.OutputEventType outputEventType = OutputStream.OutputEventType.ALL_EVENTS;

    /**
     * Construct a WindowDefinition object using the window id.
     *
     * @param id the window id
     */
    protected WindowDefinition(String id) {
        super(id);
    }

    /**
     * Construct and return a WindowDefinition object using the given id.
     *
     * @param id the window id
     * @return new instance of WindowDefinition
     */
    public static WindowDefinition id(String id) {
        return new WindowDefinition(id);
    }

    /**
     * Set the internal window to the WindowDefinition.
     *
     * @param window the internal window
     * @return the WindowDefinition object for chained method call
     */
    public WindowDefinition window(Window window) {
        this.window = window;
        return this;
    }

    /**
     * Return the output event type of the window.
     *
     * @return the output event type
     */
    public OutputStream.OutputEventType getOutputEventType() {
        return outputEventType;
    }

    /**
     * Set output event type of the window. If not explicitly assigned, OutputEventType.ALL_EVENTS
     * will be used by default.
     *
     * @param outputEventType the output event type
     */
    public void setOutputEventType(OutputStream.OutputEventType outputEventType) {
        this.outputEventType = outputEventType;
    }

    /**
     * Return the internal window of the WindowDefinition.
     *
     * @return the internal window
     */
    public Window getWindow() {
        return this.window;
    }

    @Override
    public String toString() {
        return "WindowDefinition{" +
                "id='" + id + '\'' +
                ", attributeList=" + attributeList +
                ", annotations=" + annotations +
                ", window=" + window +
                '}';
    }
}
