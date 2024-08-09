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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

 /**
 * This represents a worker channel that is created for each worker to worker interaction.
 *
 * @since 2201.11.0
 */

public class WorkerUtils {

    /*
     * Used to codegen worker async send.
     */
    @SuppressWarnings("unused")
    public static void asyncSend(Strand strand, String channelKey, Object result) {
        WorkerChannel channel = strand.workerChannelMap.get(channelKey);
        channel.write(result);
    }

    /*
     * Used to codegen worker sync send.
     */
    @SuppressWarnings("unused")
    public static void syncSend(Strand strand, String channelKey, Object result) {
        WorkerChannel channel = strand.workerChannelMap.get(channelKey);
        channel.write(result);
        Object waitResult = AsyncUtils.handleWait(strand, channel.readFuture);
        if (waitResult instanceof BError error) {
            throw error;
        }
    }

    public static Object flush(Strand strand, String[] workerChannelKeys) {
         CompletableFuture<?>[] futures = new CompletableFuture[workerChannelKeys.length];
        WorkerChannel[] channels = new WorkerChannel[workerChannelKeys.length];
        for (int i = 0; i < workerChannelKeys.length; i++) {
            WorkerChannel channel = strand.workerChannelMap.get(workerChannelKeys[i]);
            futures[i] = channel.resultFuture;
            channels[i] = channel;
        }
        if (strand.isIsolated) {
            CompletableFuture.allOf(futures);
        } else {
            AsyncUtils.handleNonIsolatedStrand(strand, () -> isAllFuturesCompleted(futures), () -> null);
        }

        for (WorkerChannel channel : channels) {
            Object result = channel.getResult();
            if (result instanceof ErrorValue errorValue) {
                return errorValue;
            }
        }
        return null;
    }

    public static Object receive(Strand strand, String channelKey) {
        WorkerChannel channel = strand.workerChannelMap.get(channelKey);
        if (strand.isIsolated) {
            return channel.read();
        }
        return AsyncUtils.handleNonIsolatedStrand(strand, channel.resultFuture::isDone, channel::read);
    }

    /*
     * Used to codegen worker alternative receive action.
     */
    @SuppressWarnings("unused")
    public static Object alternateReceive(Strand strand, String[] workerChannelKeys) {
        WorkerChannel[] channels = new WorkerChannel[workerChannelKeys.length];
        int count = 0;
        for (String workerChanelKey : workerChannelKeys) {
            channels[count++] = strand.workerChannelMap.get(workerChanelKey);
        }
        CompletableFuture<?>[] futures = new CompletableFuture[channels.length];
        for (int i = 0; i < channels.length; i++) {
            futures[i] = channels[i].resultFuture;
        }
        return getAlternativeReceiveResult(strand, futures, channels);
    }

    private static Object getAlternativeReceiveResult(Strand strand, CompletableFuture<?>[] completableFutures,
                                                      WorkerChannel[] channels) {
        Object result = AsyncUtils.handleWaitAny(strand, completableFutures);
        for (WorkerChannel channel : channels) {
            channel.read();
        }
        return result;
    }

    /*
     * Used to codegen worker multiple receive action.
     */
    @SuppressWarnings({"unused", "unchecked"})
    public static BMap<BString, Object> multipleReceive(Strand strand, Map<String, String> channelFieldNameMap,
                                                        BMapType mapType) {
        WorkerChannel[] channels = new WorkerChannel[channelFieldNameMap.size()];
        int count = 0;
        for (Map.Entry<String, String> entry : channelFieldNameMap.entrySet()) {
            channels[count++] = strand.workerChannelMap.get(entry.getValue());
        }
        CompletableFuture<?>[] futures = new CompletableFuture[channels.length];
        for (int i = 0; i < channels.length; i++) {
            futures[i] = channels[i].resultFuture;
        }
        if (strand.isIsolated) {
            return  getMultipleReceiveResult(strand, channelFieldNameMap, mapType, futures, channels);
        }
        return (BMap<BString, Object>) AsyncUtils.handleNonIsolatedStrand(strand, () -> isAllFuturesCompleted(futures),
                () -> getMultipleReceiveResult(strand, channelFieldNameMap, mapType, futures, channels));
    }

    private static Boolean isAllFuturesCompleted(CompletableFuture<?>[] futures) {
        for (CompletableFuture<?> future : futures) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    private static BMap<BString, Object> getMultipleReceiveResult(Strand strand,
                                                                  Map<String, String> channelFieldNameMap,
                                                                  BMapType mapType, CompletableFuture<?>[] futures,
                                                                  WorkerChannel[] channels) {
        int count;
        CompletableFuture.allOf(futures);
        count = 0;
        BMapInitialValueEntry[] initialValueEntries = new BMapInitialValueEntry[channels.length];
        for (Map.Entry<String, String> entry : channelFieldNameMap.entrySet()) {
            WorkerChannel channel = strand.workerChannelMap.get(entry.getValue());
            initialValueEntries[count++] = ValueCreator.createKeyFieldEntry(StringUtils.fromString(entry.getKey()),
                    channel.read());
        }
        return ValueCreator.createMapValue(mapType, initialValueEntries);
    }

    public static void completedChannelsWithPanic(Strand strand, String[] sendWorkerChannelKeys, BError error) {
        if (sendWorkerChannelKeys == null) {
            return;
        }
        for (String channelKey : sendWorkerChannelKeys) {
            WorkerChannel channel = strand.workerChannelMap.get(channelKey);
            channel.panic(error);
        }
    }

    public static void removeCompletedChannels(Strand strand, String workerName, String[] sendWorkerChannelKeys,
                                               String[] receiveWorkerChannels) {
        if (sendWorkerChannelKeys != null) {
            for (String channelKey : sendWorkerChannelKeys) {
                strand.workerChannelMap.remove(workerName, channelKey);
            }
        }
        if (receiveWorkerChannels != null) {
            for (String channelKey : receiveWorkerChannels) {
                strand.workerChannelMap.remove(workerName, channelKey);
            }
        }
    }
}
