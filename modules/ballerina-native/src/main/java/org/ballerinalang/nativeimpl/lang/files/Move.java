/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.lang.files;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Moves a file from a given location to another.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "move",
        args = {@Argument(name = "source", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"),
                @Argument(name = "destination", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function moves a file from a given location to another") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File that should be moved") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the File should moved to") })
public class Move extends AbstractNativeFunction {

    @Override 
    public BValue[] execute(Context context) {
        BStruct source = (BStruct) getRefArgument(context, 0);
        BStruct destination = (BStruct) getRefArgument(context, 1);

        Path sourcePath = Paths.get(source.getStringField(0));
        if (!Files.exists(sourcePath)) {
            throw new BallerinaException("failed to move file: file not found: " + sourcePath);
        }
        Path destinationPath = Paths.get(destination.getStringField(0));
        Path parent = destinationPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new BallerinaException("failed to move file: cannot create directory: " + parent);
            }
        }
        try {
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING,
                       StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new BallerinaException("failed to move file: " + sourcePath);
        }
        return VOID_RETURN;
    }
}
