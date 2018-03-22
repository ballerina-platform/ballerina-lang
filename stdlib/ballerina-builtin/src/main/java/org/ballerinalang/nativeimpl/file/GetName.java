/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Retrieves the name of the file given by the struct.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "getName",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "File", structPackage = "ballerina.file"),
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetName extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct fileStruct = (BStruct) context.getRefArgument(0);
        Path fileName = Paths.get(fileStruct.getStringField(0)).getFileName();
        context.setReturnValues(new BString(fileName == null ? "" : fileName.toString()));
    }
}
