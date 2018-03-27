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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;

/**
 * Moves a file from a given location to another.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "move",
        args = {@Argument(name = "source", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.file"),
                @Argument(name = "destination", type = TypeKind.STRUCT, structType = "File",
                        structPackage = "ballerina.file")},
        isPublic = true
)
public class Move extends BlockingNativeCallableUnit {

    @Override 
    public void execute(Context context) {

        BStruct source = (BStruct) context.getRefArgument(0);
        BStruct destination = (BStruct) context.getRefArgument(1);

        File sourceFile = new File(source.getStringField(0));
        if (!sourceFile.exists()) {
            throw new BallerinaException("failed to move file: file not found: " + sourceFile.getPath());
        }
        File destinationFile = new File(destination.getStringField(0));
        File parent = destinationFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new BallerinaException("failed to move file: cannot create directory: " + parent.getPath());
        }
        if (!sourceFile.renameTo(destinationFile)) {
            throw new BallerinaException("failed to move file: " + sourceFile.getPath());
        }
        context.setReturnValues();
    }
}
