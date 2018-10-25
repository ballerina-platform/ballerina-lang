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

import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BValue;

import java.util.LinkedList;
import java.util.Map;

/**
 * Manages the access to channels.
 *
 * @since 0.982.0
 */
public class ChannelManager {

    public static BValue channelReceiverAction(String channelName, BValue key, BType keyType,
                                                            WorkerExecutionContext ctx, int regIndex,
                                                            BType receiverType) {

        Map<String, LinkedList<ChannelRegistry.PendingContext>> channel =
                ChannelRegistry.getInstance().addChannel(channelName);

        synchronized (channel) {
            BValue msg = DatabaseUtils.getMessage(channelName, key, keyType, receiverType);
            if (msg != null) {
                return msg;
            } else {
                ChannelRegistry.getInstance().addWaitingContext(channelName, key, ctx, regIndex);
                BLangScheduler.workerWaitForResponse(ctx);
            }
            return null;
        }
    }

    public static ChannelRegistry.PendingContext channelSenderAction(String channelName, BValue key,
                                                                                  BValue value, BType keyType,
                                                                                  BType valType) {
        Map<String, LinkedList<ChannelRegistry.PendingContext>> channel =
                ChannelRegistry.getInstance().addChannel(channelName);

        synchronized (channel) {
            ChannelRegistry.PendingContext ctx = ChannelRegistry.getInstance().pollOnChannel(channelName, key);
            if (ctx != null) {
                return ctx;
            }
            DatabaseUtils.addEntry(channelName, key, value, keyType, valType);
            return null;
        }
    }
}
