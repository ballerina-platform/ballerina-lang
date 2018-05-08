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

package org.ballerinalang.nativeimpl.internal;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.file.utils.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * Creates a new directory.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "createDirectory",
        args = {
                @Argument(name = "path", type = TypeKind.STRUCT, structType = "Path", structPackage = "ballerina.file")
        },
        returnType = {
                @ReturnType(type = TypeKind.BOOLEAN),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.file")
        },
        isPublic = true
)
public class CreateDirectory extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(CreateDirectory.class);
    /**
     * Index which holds the reference to the path.
     */
    private static final int INDEX = 0;

    @Override
    public void execute(Context context) {
        BStruct pathStruct = (BStruct) context.getRefArgument(INDEX);
        Path path = (Path) pathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        File dir = path.toFile();
        try {
            if (dir.mkdirs()) {
                context.setReturnValues(new BBoolean(true));
            } else {
                context.setReturnValues(BLangVMErrors.createError(context,
                        "Permission denied to create the requested directory structure: " + path));
            }
        } catch (SecurityException e) {
            log.error("Could not create directory structure: " + path, e);
            context.setReturnValues(BLangVMErrors.createError(context,
                    "Could not create the requested directory structure: " + path));
        }
    }
}
