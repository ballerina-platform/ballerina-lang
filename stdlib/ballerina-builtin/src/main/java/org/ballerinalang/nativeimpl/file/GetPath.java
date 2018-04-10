/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Creates the file at the path specified in the File struct.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "Path.getPath",
        args = {
                @Argument(name = "path", type = TypeKind.STRUCT, structType = "Path", structPackage = "ballerina.file")
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "Path", structPackage = "ballerina.file")
        },
        isPublic = true
)
public class GetPath extends BlockingNativeCallableUnit {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStruct path = (BStruct) context.getRefArgument(0);
        context.setReturnValues(path);
    }
}
