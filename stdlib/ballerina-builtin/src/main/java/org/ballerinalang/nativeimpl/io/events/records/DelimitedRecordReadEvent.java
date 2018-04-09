/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io.events.records;


import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.Event;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.result.AlphaCollectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Represents delimited record read event.
 */
public class DelimitedRecordReadEvent implements Event {
    /**
     * Source which the delimited records will be read from.
     */
    private DelimitedRecordChannel channel;
    /**
     * Holds the context to the event.
     */
    private EventContext context;

    private static final Logger log = LoggerFactory.getLogger(DelimitedRecordReadEvent.class);

    public DelimitedRecordReadEvent(DelimitedRecordChannel channel) {
        this.channel = channel;
    }

    public DelimitedRecordReadEvent(DelimitedRecordChannel channel, EventContext context) {
        this.channel = channel;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult get() {
        AlphaCollectionResult result;
        try {
            String[] content = channel.read();
            result = new AlphaCollectionResult(content, context);
        } catch (IOException e) {
            log.error("Error occurred while reading from record channel", e);
            context.setError(e);
            result = new AlphaCollectionResult(context);
        } catch (Throwable e) {
            log.error("Unidentified error occurred while reading delimited records", e);
            context.setError(e);
            result = new AlphaCollectionResult(context);
        }
        return result;
    }
}
