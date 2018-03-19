/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.nativeimpl.builtin.streamletlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.bre.bvm.StreamingRuntimeManager;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStreamlet;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * {@code Start} is the function to start the streamlet runtime.
 *
 * @since 0.965.0
 */
@BallerinaFunction(packageName = "ballerina.builtin",
        functionName = "startStreamlet",
        args = {
                @Argument(name = "streamletRef", type = TypeKind.STREAMLET),
                @Argument(name = "inStreamRef", type = TypeKind.ARRAY),
                @Argument(name = "inTableRef", type = TypeKind.ARRAY),
                @Argument(name = "outStreamRef", type = TypeKind.ARRAY),
                @Argument(name = "outTableRefs", type = TypeKind.ARRAY)
        },
        returnType = {@ReturnType(type = TypeKind.STREAMLET)},
        isPublic = true)
public class StartStreamlet extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStreamlet streamlet = (BStreamlet) context.getRefArgument(0);
        StreamingRuntimeManager.getInstance().createSiddhiAppRuntime(streamlet);
        StreamingRuntimeManager.getInstance().registerSubscriberForTopics(streamlet.getStreamSpecificInputHandlerMap(),
                streamlet.getStreamIdsAsString());
        context.setReturnValues(streamlet);
    }
}
