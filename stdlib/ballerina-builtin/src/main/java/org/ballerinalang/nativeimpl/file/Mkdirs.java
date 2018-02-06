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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createAccessDeniedError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createIOError;

/**
 * This function creates the directory structure denoted by the given File struct.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        packageName = "ballerina.file",
        functionName = "mkdirs",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "File", structPackage = "ballerina.file"),
        returnType = {@ReturnType(type = TypeKind.BOOLEAN),
                @ReturnType(type = TypeKind.STRUCT, structType = "AccessDeniedError", structPackage = "ballerina.file"),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.file")},
        isPublic = true
)
public class Mkdirs extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Mkdirs.class);

    @Override
    public BValue[] execute(Context context) {
        BStruct fileStruct = (BStruct) getRefArgument(context, 0);
        Path dir = Paths.get(fileStruct.getStringField(0));

        try {
            Files.createDirectories(dir);
        } catch (SecurityException e) {
            String errMsg = "Failed to create file tree: '" + dir.toString() + "'. Permission denied.";
            log.error(errMsg, e);
            return getBValues(createAccessDeniedError(context, errMsg), null);
        } catch (FileAlreadyExistsException e) {
            String errMsg = "File already exists: '" + dir.toString() + "'. Existing file replacing disabled.";
            log.error(errMsg, e);
            return getBValues(null, createIOError(context, errMsg));
        } catch (IOException e) {
            String errMsg = "Failed to create file tree: '" + dir.toString() + "'. I/O error occurred.";
            log.error(errMsg, e);
            return getBValues(null, createIOError(context, errMsg));
        }

        return new BValue[]{null};
    }
}
