/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.jms.nativeimpl.endpoint.session;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;

import javax.jms.Connection;
import javax.jms.Session;

/**
 * Connection init function for JMS connection endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Session",
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class InitEndpoint {

    public static void initEndpoint(Strand strand, ObjectValue sessionObj, ObjectValue connectionObj) {
        MapValue sessionConfig = sessionObj.getMapValue(JmsConstants.SESSION_CONFIG);
        Connection connection = (Connection) connectionObj.getNativeData(JmsConstants.JMS_CONNECTION);

        Session session = JmsUtils.createSession(connection, sessionConfig);
        sessionObj.addNativeData(JmsConstants.JMS_SESSION, session);
    }

    private InitEndpoint() {
    }
}
