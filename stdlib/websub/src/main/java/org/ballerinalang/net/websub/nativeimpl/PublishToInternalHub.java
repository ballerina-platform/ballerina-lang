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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.WebSubUtils;
import org.ballerinalang.net.websub.hub.Hub;

/**
 * Extern function to publish against a topic in the default Ballerina Hub's underlying broker.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "publishToInternalHub",
        args = {@Argument(name = "topic", type = TypeKind.STRING),
                @Argument(name = "content", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.OBJECT)},
        isPublic = true
)
public class PublishToInternalHub extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object publishToInternalHub(Strand strand, String topic, MapValue<String, Object> content) {
        try {
            Hub.getInstance().publish(topic, content);
        } catch (BallerinaWebSubException e) {
            return WebSubUtils.createError(e.getMessage());
        }
        return null;
    }
}
