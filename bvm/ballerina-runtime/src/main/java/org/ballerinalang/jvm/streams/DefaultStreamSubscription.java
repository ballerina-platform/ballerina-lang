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

import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.StreamValue;

/**
 * The {@link DefaultStreamSubscription} represents a stream subscription in Ballerina.
 *
 * @since 0.995.0
 */
public class DefaultStreamSubscription extends StreamSubscription {

    private StreamValue stream;
    private FPValue<Object[], Object> functionPointer;

    DefaultStreamSubscription(StreamValue stream, FPValue<Object[], Object> functionPointer,
                              StreamSubscriptionManager streamSubscriptionManager) {
        super(streamSubscriptionManager);
        this.stream = stream;
        this.functionPointer = functionPointer;
    }

    public void execute(Object[] fpParams) {
        //Cannot use scheduler, as the order of events should be preserved
        functionPointer.call(fpParams);
    }

    public StreamValue getStream() {
        return stream;
    }
}
