/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.basic.consumer;

import io.nats.client.Dispatcher;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.nats.Constants.CONNECTED_CLIENTS;
import static org.ballerinalang.nats.Constants.DISPATCHER_LIST;

/**
 * Initialize NATS subscriber using the connection.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Init {

    public static void init(Strand strand, ObjectValue listenerObject, ObjectValue connectionObject) {
        // This is to add listener to the connected client list in connection object.
        ((AtomicInteger) connectionObject.getNativeData(CONNECTED_CLIENTS)).incrementAndGet();
        // Initialize dispatcher list to use in service register and listener close.
        List<Dispatcher> dispatcherList = Collections.synchronizedList(new ArrayList<>());
        listenerObject.addNativeData(DISPATCHER_LIST, dispatcherList);
    }
}
