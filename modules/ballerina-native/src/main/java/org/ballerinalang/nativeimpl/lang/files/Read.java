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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedInputStream;
import java.util.Arrays;

/**
 * Get the blob from a file.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "read",
        args = {@Argument(name = "is", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"),
                @Argument(name = "number", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.BLOB),
                      @ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Read byte data from a file") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "is",
        value = "The File struct") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "number",
        value = "The number of bytes to be read") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "data",
        value = "The blob containing files read") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "numberRead",
        value = "The number of bytes actually read") })
public class Read extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct file = (BStruct) getRefArgument(context, 0);
        int bytesToRead = (int) getIntArgument(context, 0);
        byte[] data;
        int nRead;
        try {
            BufferedInputStream is = (BufferedInputStream) file.getNativeData("inStream");
            if (is == null) {
                throw new BallerinaException("file is not opened in read mode: " + file.getStringField(0));
            }
            data = new byte[bytesToRead];
            nRead = is.read(data, 0, bytesToRead);
        } catch (Throwable e) {
            throw new BallerinaException("failed to read from file: " + e.getMessage(), e);
        }
        return getBValues(new BBlob(Arrays.copyOf(data, nRead)), new BInteger(nRead));
    }
}
