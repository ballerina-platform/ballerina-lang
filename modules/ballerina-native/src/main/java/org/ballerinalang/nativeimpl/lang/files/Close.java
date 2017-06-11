/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
        packageName = "ballerina.lang.files",
        functionName = "close",
        args = {@Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Closes a given file and its stream") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The File struct") })
public class Close extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Close.class);

    @Override
    public BValue[] execute(Context context) {
        BStruct struct = (BStruct) getRefArgument(context, 0);
        BufferedInputStream is = (BufferedInputStream) struct.getNativeData("inStream");
        BufferedOutputStream os = (BufferedOutputStream) struct.getNativeData("outStream");
        closeQuietly(is);
        closeQuietly(os);
        return VOID_RETURN;
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
