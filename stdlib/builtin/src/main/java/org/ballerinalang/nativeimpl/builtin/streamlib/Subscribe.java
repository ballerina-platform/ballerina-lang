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
package org.ballerinalang.nativeimpl.builtin.streamlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * {@code Subscribe} is the function to subscribe to data from a stream.
 *
 * @since 0.964.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "builtin",
        functionName = "stream.subscribe",
        args = {
                @Argument(name = "s", type = TypeKind.STREAM),
                @Argument(name = "func", type = TypeKind.ANY)
        },
        isPublic = true)
public class Subscribe extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStream stream = (BStream) context.getRefArgument(0);
        BFunctionPointer functionPointer = (BFunctionPointer) context.getRefArgument(1);
        stream.subscribe(functionPointer);
    }
}
