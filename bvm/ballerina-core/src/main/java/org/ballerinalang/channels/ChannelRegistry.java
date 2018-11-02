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
package org.ballerinalang.channels;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code ChannelRegistry} is the registry for channel entries.
 *
 * @since 0.982.0
 */
public class ChannelRegistry {
    private static ChannelRegistry channelRegistry = null;
    private Map<String, Map<String, LinkedList<PendingContext>>> channelList;

    private ChannelRegistry() {
        channelList = new ConcurrentHashMap<>();
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

    /**
     * Add a worker context to the channels map that is waiting for a message.
     * @param channel Channel the worker is waiting for
     * @param key key of the message
     * @param ctx requested context
     * @param regIndex variable index to assign the channel message
     */
    public void addWaitingContext(String channel, BValue key, WorkerExecutionContext ctx, int regIndex) {
        //add channel if absent
        addChannel(channel);
        Map<String, LinkedList<PendingContext>> channelEntries = channelList.get(channel);
        String keyVal = null;
        if (key != null) {
            if (key instanceof BMap) {
                keyVal = DatabaseUtils.sortBMap(((BMap) key).getMap());
            } else {
                keyVal = key.stringValue();
            }
        }
        LinkedList<PendingContext> ctxList = channelEntries.computeIfAbsent(keyVal,
                bValue -> new LinkedList<>());

        PendingContext pContext = new PendingContext();
        pContext.regIndex = regIndex;
        pContext.context = ctx;
        ctxList.add(pContext);
        channelEntries.put(keyVal, ctxList);
    }

    /**
     * Return a {@code PendingContext} that is waiting on a message from the given channel.
     * @param channel Channel to check the waiting context
     * @param key message key
     * @return Worker context or null
     */
    public PendingContext pollOnChannel(String channel, BValue key) {
        //add channel if absent
        addChannel(channel);
        String keyVal = null;
        if (key != null) {
            if (key instanceof BMap) {
                keyVal = DatabaseUtils.sortBMap(((BMap) key).getMap());
            } else {
                keyVal = key.stringValue();
            }
        }
        LinkedList<PendingContext> pendingCtxs = channelList.get(channel).get(keyVal);
        if (pendingCtxs != null) {
            return pendingCtxs.poll();
        }
        return null;
    }

    /**
     * Add a new channel if not exist and returns.
     * @param name Channel identifier
     * @return A channel entry in given name from channels list
     */
    public Map<String, LinkedList<PendingContext>> addChannel(String name) {
        return channelList.computeIfAbsent(name, chn -> new HashMap<>());
    }

    /**
     * Represents a worker waiting for a data from a Channel.
     */
    public static class PendingContext {
        public int regIndex;
        public WorkerExecutionContext context;
    }
}
