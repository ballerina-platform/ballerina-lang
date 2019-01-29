/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.internal.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * Get the parent director of a file or directory.
 *
 * @since 0.971.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "getParentDirectory",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                             structPackage = Constants.PACKAGE_PATH)
        ,
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                            structPackage = Constants.PACKAGE_PATH),
                @ReturnType(type = TypeKind.OBJECT, structType = STRUCT_GENERIC_ERROR,
                            structPackage = BALLERINA_BUILTIN_PKG)
        },
        isPublic = true
)
public class GetParentDirectory extends BlockingNativeCallableUnit {
    
    private static final Logger log = LoggerFactory.getLogger(GetParentDirectory.class);
    
    @Override
    public void execute(Context context) {
        BMap<String, BValue> pathStruct = (BMap<String, BValue>) context.getRefArgument(0);
        Path path = (Path) pathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
    
        try {
            Path parent = path.getParent();
            if (parent != null) {
                BMap<String, BValue> parentPath = BLangConnectorSPIUtil.createObject(context, Constants.PACKAGE_PATH,
                        Constants.PATH_STRUCT, new BString(parent.toString()));
                context.setReturnValues(parentPath);
            } else {
                String msg = "Parent folder cannot be found for: " + path;
                log.error(msg);
                context.setReturnValues(BLangVMErrors.createError(context, msg));
            }
        } catch (Exception ex) {
            String msg = "Error occurred while getting parent directory of: " + path;
            log.error(msg, ex);
            context.setReturnValues(BLangVMErrors.createError(context, msg));
        }
    }
}
