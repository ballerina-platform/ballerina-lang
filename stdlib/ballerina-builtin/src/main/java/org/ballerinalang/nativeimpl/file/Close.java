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
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;

/**
 * Can be used to close a file object.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "close",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "File",
                             structPackage = "ballerina.file"),
        args = {@Argument(name = "file", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.file")},
        isPublic = true
)
public class Close extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Close.class);

    @Override
    public void execute(Context context) {
        BStruct struct = (BStruct) context.getRefArgument(0);
        BufferedInputStream is = (BufferedInputStream) struct.getNativeData("inStream");
        BufferedOutputStream os = (BufferedOutputStream) struct.getNativeData("outStream");
        closeQuietly(is);
        closeQuietly(os);
        context.setReturnValues();
    }

    private void closeQuietly(Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (IOException e) {
            log.error("Exception during Resource.close()", e);
        }
    }
}
