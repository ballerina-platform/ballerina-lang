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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.values.BValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * {@code ChannelRegistryManager} is the registry for channel entries.
 *
 * @since 0.981
 */
public class ChannelRegistry {
    private static ChannelRegistry channelRegistry = null;
    private Map<String, Map<BValue, LinkedList<WorkerExecutionContext>>> channelList;

    private ChannelRegistry() {
        channelList = new HashMap<>();
    }

    public static ChannelRegistry getInstance() {
        if (channelRegistry == null) {
            synchronized (ChannelRegistry.class) {
                if (channelRegistry == null) {
                    channelRegistry = new ChannelRegistry();
                }
            }
        }
        return channelRegistry;
    }

    public void addChannel(String name) {
        channelList.put(name, new HashMap<>());
    }

    public void addWaitingContext(String channel, BValue key, WorkerExecutionContext ctx) {
        Map<BValue, LinkedList<WorkerExecutionContext>> channelEntries = channelList.get(channel);
        LinkedList<WorkerExecutionContext> ctxList = channelEntries.get(key);
        if (ctxList == null) {
            ctxList = new LinkedList<>();
        }
        ctxList.add(ctx);
    }

    public WorkerExecutionContext pollOnChannel(String channel, BValue key) {
        return channelList.get(channel).get(key).poll();
    }
}
