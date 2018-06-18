/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.events.data;

import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.channels.base.Representation;
import org.ballerinalang.stdlib.io.events.Event;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.result.NumericResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Writes float to a given source.
 */
public class WriteFloatEvent implements Event {
    /**
     * Will be used to write float.
     */
    private DataChannel channel;
    /**
     * Holds context to the event.
     */
    private EventContext context;
    /**
     * Represents the value which will be written.
     */
    private double value;
    /**
     * Holds the representation of the value which should be written.
     */
    private Representation representation;

    private static final Logger log = LoggerFactory.getLogger(WriteFloatEvent.class);

    public WriteFloatEvent(DataChannel dataChannel, double value, Representation representation, EventContext context) {
        this.channel = dataChannel;
        this.context = context;
        this.value = value;
        this.representation = representation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        NumericResult result;
        try {
            channel.writeDouble(value, representation);
            result = new NumericResult(context);
        } catch (IOException e) {
            log.error("Error occurred while writing float", e);
            context.setError(e);
            result = new NumericResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while writing float", e);
            context.setError(e);
            result = new NumericResult(context);
        }
        return result;
    }

}
