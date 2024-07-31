/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This represents a worker data channel holder that is created for each strand to hold channels required.
 *
 * @since 0.995.0
 */
public class WDChannels {

    private Map<String, WorkerDataChannel> wDChannels;
    private final List<ErrorValue> errors = new ArrayList<>();

    // A worker receive field for multiple receive action.
    public record ReceiveField(String fieldName, String channelName) {
    }

     public synchronized WorkerDataChannel getWorkerDataChannel(String name) {
        if (this.wDChannels == null) {
            this.wDChannels = new HashMap<>();
        }
        WorkerDataChannel channel = this.wDChannels.get(name);
        if (channel == null) {
            channel = new WorkerDataChannel(name);
            this.wDChannels.put(name, channel);
        }
        return channel;
    }

    public Object receiveDataMultipleChannels(Strand strand, ReceiveField[] receiveFields, Type targetType)
            throws Throwable {
       return null;
    }

}
