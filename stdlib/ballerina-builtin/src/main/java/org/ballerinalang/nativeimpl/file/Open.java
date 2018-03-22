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
        orgName = "ballerina", packageName = "file",
        functionName = "open",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "File",
                             structPackage = "ballerina.file"),
        args = {@Argument(name = "file", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.file"), @Argument(name = "accessMode", type = TypeKind.STRING)},
        isPublic = true
)
public class Open extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Open.class);
    
    @Override 
    public void execute(Context context) {
        BStruct struct = (BStruct) context.getRefArgument(0);
        String accessMode = context.getStringArgument(0);
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
                context.setReturnValues();
                return;
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
        context.setReturnValues();
    }

    private void createDirs(File destinationFile) {
        File parent = destinationFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new BallerinaException("Error in writing file");
        }
    }
}
