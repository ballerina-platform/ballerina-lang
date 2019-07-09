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
 *
 */

package org.ballerinalang.messaging.artemis.externimpl.consumer;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.concurrent.CountDownLatch;

/**
 * Extern function to start the Artemis consumer.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "start",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.LISTENER_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        )
)
public class Start extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object start(Strand strand, ObjectValue listenerObj) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            listenerObj.addNativeData(ArtemisConstants.COUNTDOWN_LATCH, countDownLatch);
            ObjectValue sessionObj = listenerObj.getObjectValue(ArtemisConstants.SESSION);
            ClientSession session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);
            session.start();
            // It is essential to keep a non-daemon thread running in order to avoid the java program or the
            // Ballerina service from exiting
            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (ActiveMQException e) {
            return ArtemisUtils.getError(e);
        }
        return null;
    }

}
