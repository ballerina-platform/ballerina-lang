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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.file.utils.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * List out the content in directory.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "list",
        args = {@Argument(name = "path", type = TypeKind.STRUCT, structType = "Path",
                structPackage = "ballerina.file")},
        returnType = {
                @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRUCT),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.file")
        },
        isPublic = true
)
public class List extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(List.class);

    @Override
    public void execute(Context context) {
        BStruct pathStruct = (BStruct) context.getRefArgument(0);
        Path path = (Path) pathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        final BRefValueArray filesList = new BRefValueArray(new BArrayType(pathStruct.getType()));
        try {
            Files.list(path).forEach(p -> {
                BStruct filePaths = BLangConnectorSPIUtil.createBStruct(context, Constants.FILE_PACKAGE,
                        Constants.PATH_STRUCT);
                filePaths.addNativeData(Constants.PATH_DEFINITION_NAME, p);
                long index = filesList.size();
                filesList.add((index), filePaths);
            });
            context.setReturnValues(filesList);
        } catch (IOException | SecurityException e) {
            String msg;
            if (e instanceof IOException) {
                msg = "Error occurred while opening directory: " + path;
            } else {
                msg = "Permission denied. Could not open directory: " + path;
            }
            log.error(msg, e);
            context.setReturnValues(BLangVMErrors.createError(context, msg));
        }
    }
}
