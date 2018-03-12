/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.grpc.nativeimpl.connection.client.clientsub;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * Created by daneshk on 3/12/18.
 */
@BallerinaFunction(
        functionName = "InitConnector",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ClientEndpoint",
                structPackage = "ballerina.net.grpc"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ClientEndpointConfiguration")},
        isPublic = true
)
public class InitConnector extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        return new BValue[0];
    }
}
