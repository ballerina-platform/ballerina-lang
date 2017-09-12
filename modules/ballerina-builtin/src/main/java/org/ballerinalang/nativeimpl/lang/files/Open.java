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
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

    private static final Logger log = LoggerFactory.getLogger(Open.class);
    
    @Override 
    public BValue[] execute(Context context) {
        BStruct struct = (BStruct) getRefArgument(context, 0);
        String accessMode = getStringArgument(context, 0);
        try {
            File file = new File(struct.getStringField(0));
            String accessLC = accessMode.toLowerCase(Locale.getDefault());
            
            if (accessLC.contains("r")) {
                if (!file.exists()) {
                    throw new BallerinaException("file not found: " + file.getPath());
                }
                if (!file.canRead()) {
                    throw new BallerinaException("file is not readable: " + file.getPath());
                }
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
                struct.addNativeData("inStream", is);
            }
            
            boolean write = accessLC.contains("w");
            boolean append = accessLC.contains("a");
            
            // if opened for read only, then return.
            if (!write && !append) {
                return VOID_RETURN;
            }
            
            // if opened for both write and append, then give priority to append
            if (write && append) {
                log.info("found both 'a' and 'w' in access mode string. opening file in append mode");
            }

            if (file.exists() && !file.canWrite()) {
                throw new BallerinaException("file is not writable: " + file.getPath());
            }
            createDirs(file);
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file, append));
            struct.addNativeData("outStream", os);
        } catch (Throwable e) {
            throw new BallerinaException("failed to open file: " + e.getMessage(), e);
        }
        return VOID_RETURN;
    }

    private void createDirs(File destinationFile) {
        File parent = destinationFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new BallerinaException("Error in writing file");
        }
    }
}
