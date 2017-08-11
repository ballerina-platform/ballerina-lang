/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

import java.io.File;

/**
 * Deletes a file from a given location.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "delete",
        args = {@Argument(name = "target", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function deletes a file from a given location") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "target",
        value = "File that should be deleted") })
public class Delete extends AbstractNativeFunction {

    @Override 
    public BValue[] execute(Context context) {

        BStruct target = (BStruct) getRefArgument(context, 0);
        File targetFile = new File(target.getStringField(0));
        if (!targetFile.exists()) {
            throw new BallerinaException("failed to delete file: file not found: " + targetFile.getPath());
        }
        if (!delete(targetFile)) {
            throw new BallerinaException("failed to delete file: " + targetFile.getPath());
        }
        return VOID_RETURN;
    }

    private boolean delete(File targetFile) {

        String[] entries = targetFile.list();
        if (entries != null && entries.length != 0) {
            for (String s : entries) {
                File currentFile = new File(targetFile.getPath(), s);
                if (currentFile.isDirectory()) {
                    delete(currentFile);
                } else if (!currentFile.delete()) {
                    return false;
                }
            }
        }
        return targetFile.delete();
    }

}
