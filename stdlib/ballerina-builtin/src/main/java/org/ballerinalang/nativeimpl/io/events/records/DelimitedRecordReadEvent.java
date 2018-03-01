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
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.result.AlphaCollectionResult;

/**
 * Represents delimited record read event.
 */
public class DelimitedRecordReadEvent implements Event {
    /**
     * Source which the delimited records will be read from.
     */
    private DelimitedRecordChannel channel;

    public DelimitedRecordReadEvent(DelimitedRecordChannel channel) {
        this.channel = channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventResult call() throws Exception {
        String[] content = channel.read();
        return new AlphaCollectionResult(content);
    }
}
