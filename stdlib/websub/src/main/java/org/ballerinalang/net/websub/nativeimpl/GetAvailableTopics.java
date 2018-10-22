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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.hub.Hub;

import java.util.List;

/**
 * Extern function to retrieve an array of topics which registered in the Hub.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "getAvailableTopics",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WebSubHub", structPackage = "ballerina/websub"),
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetAvailableTopics extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStringArray topicArray = new BStringArray();
        List<String> topics = Hub.getInstance().getTopics();
        int i = 0;
        for (String topic : topics) {
            topicArray.add(i++, topic);
        }
        context.setReturnValues(topicArray);
    }
}
