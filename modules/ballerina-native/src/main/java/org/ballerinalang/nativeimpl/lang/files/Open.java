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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * Gets the streams from a local file.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "open",
        args = {@Argument(name = "file", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"), @Argument(name = "accessMode", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Gets streams from a local file") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "file",
        value = "The File that should be opened") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "accessMode",
        value = "The mode the file should be opened in") })
public class Open extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {
        BStruct struct = (BStruct) getArgument(context, 0);
        BString accessMode = (BString) getArgument(context, 1);
        try {
            String path = struct.getValue(0).stringValue();
            File file = new File(path);
            String accessLC = accessMode.stringValue().toLowerCase(Locale.getDefault());
            if (accessLC.contains("r")) {
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                struct.addNativeData("inStream", is);
            }
            if (accessLC.contains("w")) {
                createDirs(file);
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                struct.addNativeData("outStream", os);
            } else if (accessLC.contains("a")) {
                createDirs(file);
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file, true));
                struct.addNativeData("outStream", os);
            }
        } catch (FileNotFoundException e) {
            throw new BallerinaException("Exception occurred since file does not exist", e);
        }
        return VOID_RETURN;
    }

    private void createDirs(File destinationFile) {
        File parent = destinationFile.getParentFile();
        if (parent != null) {
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new BallerinaException("Error in writing file");
                }
            }
        }
    }
}
