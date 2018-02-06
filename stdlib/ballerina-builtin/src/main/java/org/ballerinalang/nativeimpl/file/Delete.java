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
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createAccessDeniedError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createIOError;

/**
 * Deletes a file from a given location.
 */
@BallerinaFunction(
        packageName = "ballerina.file",
        functionName = "delete",
        args = {@Argument(name = "target", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.file")},
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "AccessDeniedError", structPackage = "ballerina.file"),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.file")},
        isPublic = true
)
public class Delete extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Delete.class);

    @Override 
    public BValue[] execute(Context context) {
        BStruct target = (BStruct) getRefArgument(context, 0);
        Path targetFile = Paths.get(target.getStringField(0));

        try {
            Files.walk(targetFile)
                    .sorted(Comparator.reverseOrder()) // To ensure the root directory is the one deleted last
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (SecurityException e) {
            String errMsg = "Failed to delete the file: '" + targetFile.toString() + "'. Permission denied.";
            log.error(errMsg, e);
            return getBValues(createAccessDeniedError(context, errMsg), null);
        } catch (IOException e) {
            String errMsg = "Failed to delete the file: '" + targetFile.toString() + "'. I/O error occurred.";
            log.error(errMsg, e);
            return getBValues(null, createIOError(context, errMsg));
        }
        return getBValues(null, null);
    }
}
