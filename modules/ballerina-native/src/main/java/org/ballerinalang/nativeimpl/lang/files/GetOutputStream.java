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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFile;
import org.ballerinalang.model.values.BOutputStream;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Get an output stream from file.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "getOutputStream",
        args = {@Argument(name = "file", type = TypeEnum.FILE),
                @Argument(name = "append", type = TypeEnum.BOOLEAN)},
        returnType = {@ReturnType(type = TypeEnum.OUTPUTSTREAM)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Gets the outputStream from file") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The BFile reference") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "append",
        value = "Append the content to the file") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "os",
        value = "The output stream of the file") })
public class GetOutputStream extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BOutputStream result;
        BFile file = (BFile) getArgument(context, 0);
        BBoolean append = (BBoolean) getArgument(context, 1);
        try {
            FileSystemManager fsm = VFS.getManager();
            FileObject fileObject = fsm.resolveFile(file.stringValue());
            result = new BOutputStream(fileObject.getContent().getOutputStream(append.booleanValue()));
        }  catch (FileSystemException e) {
            throw new BallerinaException("Error occurred while getting output stream", e);
        }
        return getBValues(result);
    }
}
