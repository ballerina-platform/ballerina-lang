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

 import io.ballerina.runtime.api.TypeTags;
 import io.ballerina.runtime.api.creators.ErrorCreator;
 import io.ballerina.runtime.api.creators.ValueCreator;
 import io.ballerina.runtime.api.types.MapType;
 import io.ballerina.runtime.api.types.RecordType;
 import io.ballerina.runtime.api.types.Type;
 import io.ballerina.runtime.api.utils.StringUtils;
 import io.ballerina.runtime.api.values.BError;
 import io.ballerina.runtime.api.values.BMap;
 import io.ballerina.runtime.api.values.BMapInitialValueEntry;
 import io.ballerina.runtime.api.values.BString;
 import io.ballerina.runtime.internal.values.ErrorValue;

 import java.util.Map;
 import java.util.concurrent.CompletableFuture;


 /**
  * This represents a worker channel that is created for each worker to worker interaction.
  *
  * @since 2201.11.0
  */

 public final class WorkerUtils {

     /*
      * Used to codegen worker async send.
      */
     @SuppressWarnings("unused")
     public static void asyncSend(WorkerChannelMap workerChannelMap, String channelKey, Object result) {
         WorkerChannel channel = workerChannelMap.get(channelKey);
         channel.write(result);
     }

     /*
      * Used to codegen worker sync send.
      */
     @SuppressWarnings("unused")
     public static Object syncSend(Strand strand, WorkerChannelMap workerChannelMap, String channelKey, Object result) {
         WorkerChannel channel = workerChannelMap.get(channelKey);
         channel.write(result);
         Object waitResult = AsyncUtils.handleWait(strand, channel.getReceiveFuture());
         if (waitResult instanceof BError error) {
             return error;
         }
         return null;

     }

     public static Object flush(Strand strand, WorkerChannelMap workerChannelMap, String[] workerChannelKeys) {
         CompletableFuture<?>[] futures = new CompletableFuture[workerChannelKeys.length];
         WorkerChannel[] channels = new WorkerChannel[workerChannelKeys.length];
         for (int i = 0; i < workerChannelKeys.length; i++) {
             WorkerChannel channel = workerChannelMap.get(workerChannelKeys[i]);
             futures[i] = channel.getReceiveFuture();
             channels[i] = channel;
         }
         if (strand.isIsolated) {
             AsyncUtils.waitForAllFutureResult(futures);
         } else {
             AsyncUtils.handleNonIsolatedStrand(strand, () -> {
                 AsyncUtils.waitForAllFutureResult(futures);
                 return null;
             });
         }

         for (WorkerChannel channel : channels) {
             Object result = channel.getReceiveFuture().resultNow();
             if (result instanceof ErrorValue errorValue) {
                 return errorValue;
             }
         }
         return null;
     }

     public static Object receive(Strand strand, WorkerChannelMap workerChannelMap, String channelKey) {
         WorkerChannel channel = workerChannelMap.get(channelKey);
         if (strand.isIsolated) {
             return channel.read();
         }
         return AsyncUtils.handleNonIsolatedStrand(strand, channel::read);
     }

     /*
      * Used to codegen worker alternative receive action.
      */
     @SuppressWarnings("unused")
     public static Object alternateReceive(Strand strand, WorkerChannelMap workerChannelMap,
                                           String[] workerChannelKeys) {
         WorkerChannel[] channels = new WorkerChannel[workerChannelKeys.length];
         int count = 0;
         for (String workerChanelKey : workerChannelKeys) {
             channels[count++] = workerChannelMap.get(workerChanelKey);
         }
         CompletableFuture<?>[] futures = new CompletableFuture[channels.length];
         for (int i = 0; i < channels.length; i++) {
             futures[i] = channels[i].getResultFuture();
         }
         return getAlternativeReceiveResult(strand, futures, channels);
     }

     /*
      * Used to codegen worker multiple receive action.
      */
     @SuppressWarnings({"unused", "unchecked"})
     public static BMap<BString, Object> multipleReceive(Strand strand, WorkerChannelMap workerChannelMap,
                                                         Map<String, String> channelFieldNameMap, Type targetType) {
         WorkerChannel[] channels = new WorkerChannel[channelFieldNameMap.size()];
         int count = 0;
         for (Map.Entry<String, String> entry : channelFieldNameMap.entrySet()) {
             channels[count++] = workerChannelMap.get(entry.getValue());
         }
         CompletableFuture<?>[] futures = new CompletableFuture[channels.length];
         for (int i = 0; i < channels.length; i++) {
             futures[i] = channels[i].getResultFuture();
         }
         if (strand.isIsolated) {
             AsyncUtils.waitForAllFutureResult(futures);
             return getMultipleReceiveResult(workerChannelMap, channelFieldNameMap, targetType, channels);
         }
         return (BMap<BString, Object>) AsyncUtils.handleNonIsolatedStrand(strand,
                 () -> {
                     AsyncUtils.waitForAllFutureResult(futures);
                     return getMultipleReceiveResult(workerChannelMap, channelFieldNameMap, targetType, channels);
                 });
     }

     /*
      * Used to codegen adding worker channels.
      */
     @SuppressWarnings("unused")
     public static void addWorkerChannels(WorkerChannelMap workerChannelMap, String[] workerChannelKeys) {
         workerChannelMap.addChannelKeys(workerChannelKeys);
     }

     /*
      * Used to codegen handle worker return.
      */
     @SuppressWarnings("unused")
     public static void completedWorkerChannels(WorkerChannelMap workerChannelMap,
                                                Object returnValue, String[] sendWorkerChannelKeys,
                                                String[] receiveWorkerChannelKeys) {
         if (sendWorkerChannelKeys == null && receiveWorkerChannelKeys == null) {
             return;
         }

         if (sendWorkerChannelKeys != null) {
             for (String channelKey : sendWorkerChannelKeys) {
                 workerChannelMap.completeSendWorkerChannels(channelKey, returnValue);
             }
         }
         if (receiveWorkerChannelKeys != null) {
             for (String channelKey : receiveWorkerChannelKeys) {
                 workerChannelMap.completeReceiveWorkerChannels(channelKey, returnValue);
             }
         }
     }

     /*
      * Used to codegen handle worker panic.
      */
     @SuppressWarnings("unused")
     public static void completeWorkerChannelsWithPanic(WorkerChannelMap workerChannelMap, Throwable throwable,
                                                        String[] sendWorkerChannelKeys,
                                                        String[] receiveWorkerChannelKeys) {
         if (sendWorkerChannelKeys == null && receiveWorkerChannelKeys == null) {
             return;
         }
         BError error;
         if (throwable instanceof BError bError) {
             error = bError;
         } else {
             error = ErrorCreator.createError(throwable);
         }
         if (sendWorkerChannelKeys != null) {
             for (String channelKey : sendWorkerChannelKeys) {
                 workerChannelMap.panicSendWorkerChannels(channelKey, error);
             }
         }
         if (receiveWorkerChannelKeys != null) {
             for (String channelKey : receiveWorkerChannelKeys) {
                 workerChannelMap.panicReceiveWorkerChannels(channelKey, error);
             }
         }
     }

     private static Object getAlternativeReceiveResult(Strand strand, CompletableFuture<?>[] completableFutures,
                                                       WorkerChannel[] channels) {
         Object result = AsyncUtils.handleWaitAny(strand, completableFutures);
         for (WorkerChannel channel : channels) {
             channel.read();
         }
         return result;
     }

     private static BMap<BString, Object> getMultipleReceiveResult(WorkerChannelMap workerChannelMap,
                                                                   Map<String, String> channelFieldNameMap,
                                                                   Type targetType, WorkerChannel[] channels) {
         int count = 0;
         BMapInitialValueEntry[] initialValueEntries = new BMapInitialValueEntry[channels.length];
         for (Map.Entry<String, String> entry : channelFieldNameMap.entrySet()) {
             WorkerChannel channel = workerChannelMap.get(entry.getValue());
             initialValueEntries[count++] = ValueCreator.createKeyFieldEntry(StringUtils.fromString(entry.getKey()),
                     channel.read());
         }
         if (targetType.getTag() == TypeTags.RECORD_TYPE_TAG) {
             return ValueCreator.createMapValue((RecordType) targetType, initialValueEntries);
         }
         return ValueCreator.createMapValue((MapType) targetType, initialValueEntries);
     }

    private WorkerUtils() {}
}

