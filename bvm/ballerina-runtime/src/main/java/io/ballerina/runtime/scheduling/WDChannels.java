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
package io.ballerina.runtime.scheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a worker data channel holder that is created for each strand to hold channels required.
 *
 * @since 0.995.0
 */
public class WDChannels {

    private Map<String, WorkerDataChannel> wDChannels;

    //TODO try to generalize this to a normal data channel, in that case we won't need these classes.
    public WDChannels() {
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
}
