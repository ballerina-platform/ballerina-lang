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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;

import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createAccessDeniedError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createIOError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createTimeStruct;

/**
 * Retrieves the last modified time of the specified file.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        packageName = "ballerina.file",
        functionName = "getModifiedTime",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "File", structPackage = "ballerina.file"),
        returnType = {@ReturnType(type = TypeKind.STRUCT), @ReturnType(type = TypeKind.STRUCT),
                @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class GetModifiedTime extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetModifiedTime.class);

    @Override
    public BValue[] execute(Context context) {
        BStruct fileStruct = (BStruct) getRefArgument(context, 0);
        String path = fileStruct.getStringField(0);

        BStruct lastModifiedStruct;
        try {
            FileTime lastModified = Files.getLastModifiedTime(Paths.get(path));
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastModified.toString());
            lastModifiedStruct = createTimeStruct(context, zonedDateTime, lastModified.toMillis());
        } catch (IOException e) {
            String msg = "Error in reading file: " + path;
            log.error(msg, e);
            return getBValues(null, null, createIOError(context, msg));
        } catch (SecurityException e) {
            String msg = "Read permission denied for file: " + path;
            log.error(msg, e);
            return getBValues(null, createAccessDeniedError(context, msg), null);
        }

        return getBValues(lastModifiedStruct, null, null);
    }
}
