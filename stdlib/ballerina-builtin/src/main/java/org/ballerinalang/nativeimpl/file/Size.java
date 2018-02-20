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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Returns the size of the specified file.
 *
 * @since 0.961.0
 */
@BallerinaFunction(
        packageName = "ballerina.file",
        functionName = "size",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "File", structPackage = "ballerina.file"),
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class Size extends AbstractNativeFunction {

    private static final int DIRECTORY_OR_RESTRICTED_FILE = -1;

    @Override
    public BValue[] execute(Context context) {
        BStruct fileStruct = (BStruct) getRefArgument(context, 0);
        Path filePath = Paths.get(fileStruct.getStringField(0));

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            return getBValues(new BInteger(DIRECTORY_OR_RESTRICTED_FILE));
        }

        long fileSize;
        try {
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            fileSize = DIRECTORY_OR_RESTRICTED_FILE;
        }

        return getBValues(new BInteger(fileSize));
    }
}
