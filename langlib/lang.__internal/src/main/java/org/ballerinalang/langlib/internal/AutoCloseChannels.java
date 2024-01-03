/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.scheduling.WorkerDataChannel;

/**
 * Native implementation of lang.internal:AutoCloseChannels.
 *
 * @since 2201.9.0
 */
public class AutoCloseChannels {

    /**
     * Auto-closes the specified worker channels if they exist; otherwise, closes them upon creation.
     *
     * @param channelIds channel IDs of the channels to be closed
     */
    public static void autoClose(BString[] channelIds) {
        Strand parent = Scheduler.getStrand().parent;
        for (BString channelId : channelIds) {
            String channelName = channelId.getValue() + ":" + (parent.functionInvocation - 1);
            autoCloseChannel(channelName, parent);
        }
    }

    private static void autoCloseChannel(String channelId, Strand parent) {
        WorkerDataChannel workerDataChannel = parent.wdChannels.getWorkerDataChannel(channelId);
        workerDataChannel.autoClose();
    }
}