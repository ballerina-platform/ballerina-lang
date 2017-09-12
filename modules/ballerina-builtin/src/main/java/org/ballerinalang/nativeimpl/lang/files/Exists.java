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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Can be used to check whether a file exists.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "exists",
        args = {@Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files")},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Checks whether a file exists") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The File struct of witch existence is to be checked") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "isExists",
        value = "Boolean representing whether a file exists") })
public class Exists extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct struct = (BStruct) getRefArgument(context, 0);
        Path filePath = Paths.get(struct.getStringField(0));

        return getBValues(new BBoolean(Files.exists(filePath)));
    }
}
