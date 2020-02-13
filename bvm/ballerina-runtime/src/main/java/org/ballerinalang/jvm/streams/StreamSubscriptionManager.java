/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm.streams;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.CloneUtils;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.StreamValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * The {@link StreamSubscriptionManager} manages the streams subscriptions. It is responsible for registering
 * subscriptions for streams and sending events to correct stream through the subscription.
 *
 * @since 0.995.0
 */
@Deprecated
public class StreamSubscriptionManager implements Observer {

    private Map<String, List<StreamSubscription>> processors = new HashMap<>();

    private static StreamSubscriptionManager streamSubscriptionManager = new StreamSubscriptionManager();

    private StreamSubscriptionManager() {

    }

    public static StreamSubscriptionManager getInstance() {
        return streamSubscriptionManager;
    }

    public void registerMessageProcessor(StreamValue stream, FPValue<Object[], Object> functionPointer) {
        synchronized (this) {
            processors.computeIfAbsent(stream.streamId, key -> new ArrayList<>())
                    .add(new DefaultStreamSubscription(stream, functionPointer, this));
        }
    }

    public void sendMessage(StreamValue stream, Strand strand, Object value) {
        List<StreamSubscription> msgProcessors = processors.get(stream.streamId);
        if (msgProcessors != null) {
            msgProcessors.forEach(processor -> processor.send(strand, CloneUtils.cloneValue(value)));
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(o instanceof StreamSubscription)) {
            throw new BallerinaException("Invalid subscription. Expected a subscription to a stream");
        }
        StreamSubscription msgProcessor = (StreamSubscription) o;
        StreamValue stream = msgProcessor.getStream();
        if (!(arg instanceof Object[])) {
            throw new BallerinaException("Invalid data parameters received to stream: " + stream.getStreamId());
        } else {
            msgProcessor.execute((Object[]) arg);
        }
    }
}
