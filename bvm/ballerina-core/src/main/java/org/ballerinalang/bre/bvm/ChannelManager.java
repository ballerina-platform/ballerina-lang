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

/**
 * Manages the access to channels.
 */
public class ChannelManager {

    public static void addNewChannel(String channelName) {
        //TODO:create a table in DB as well
        ChannelRegistry.getInstance().addChannel(channelName);
    }

    public static synchronized BValue channelReceiverAction(String channelName, BValue key, WorkerExecutionContext
            ctx, int regIndex) {
        //todo:check it in DB, following is when the value is not in DB
        ChannelRegistry.getInstance().addWaitingContext(channelName, key, ctx, regIndex);
        BLangScheduler.workerWaitForResponse(ctx);
        return null; //need to return data retrieved from DB
    }

    public static synchronized ChannelRegistry.PendingContext channelSenderAction(String channelName, BValue key) {
        ChannelRegistry.PendingContext ctx = ChannelRegistry.getInstance().pollOnChannel(channelName, key);
        if (ctx != null) {
            return ctx;
        }
        //todo:save in DB
        return null;
    }
}
