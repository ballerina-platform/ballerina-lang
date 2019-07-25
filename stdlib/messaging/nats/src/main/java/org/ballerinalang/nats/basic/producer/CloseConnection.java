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

package org.ballerinalang.nats.basic.producer;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Extern function to close logical connection in producer.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "closeConnection",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Producer", structPackage = "ballerina/nats"),
        isPublic = true
)
public class CloseConnection {

    public static void closeConnection(Strand strand, ObjectValue producerObject) {
        Object connection = producerObject.get("connection");
        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            ((AtomicInteger) connectionObject.getNativeData(Constants.CONNECTED_CLIENTS)).decrementAndGet();
        }
    }
}
