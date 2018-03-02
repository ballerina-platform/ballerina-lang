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

import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.Event;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.result.NumericResult;

/**
 * Represents write event of delimited records channel.
 */
public class DelimitedRecordWriteEvent implements Event {
    /**
     * Channel the record content will be written.
     */
    private DelimitedRecordChannel channel;
    /**
     * Content which will be written to the channel.
     */
    private BStringArray content;
    /**
     * Context of the event which will be called upon completion.
     */
    private EventContext context;

    public DelimitedRecordWriteEvent(DelimitedRecordChannel channel, BStringArray content) {
        this.channel = channel;
        this.content = content;
    }

    public DelimitedRecordWriteEvent(DelimitedRecordChannel channel, BStringArray content, EventContext context) {
        this.channel = channel;
        this.content = content;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult call() throws Exception {
        channel.write(content);
        return new NumericResult(-1);
    }
}
