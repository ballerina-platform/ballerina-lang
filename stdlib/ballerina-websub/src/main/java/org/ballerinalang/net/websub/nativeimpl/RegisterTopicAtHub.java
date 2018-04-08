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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.hub.Hub;

/**
 * Native function to register a topic in the Ballerina Hub, to accept subscription requests against.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "registerTopicAtHub",
        args = {@Argument(name = "topic", type = TypeKind.STRING),
                @Argument(name = "secret", type = TypeKind.STRING),
                @Argument(name = "loadingOnStartUp", type = TypeKind.BOOLEAN)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class RegisterTopicAtHub extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String topic = context.getStringArgument(0);
        String secret = context.getStringArgument(1);
        Boolean loadingOnStartUp = context.getBooleanArgument(0);
        context.setReturnValues(new BString(Hub.getInstance().registerTopic(topic, secret, loadingOnStartUp)));
    }

}
