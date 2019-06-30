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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves file path.
 *
 * @since 0.971.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "resolve",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                             structPackage = Constants.PACKAGE_PATH)
        ,
        args = {
                @Argument(name = "paths", type = TypeKind.ARRAY, elementType = TypeKind.STRING)
        },
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                            structPackage = Constants.PACKAGE_PATH)
        },
        isPublic = true
)
public class Resolve extends BlockingNativeCallableUnit {
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BMap<String, BValue> pathStruct = (BMap<String, BValue>) context.getRefArgument(0);
        Path path = (Path) pathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
    
        BValueArray pathsToAppend = (BValueArray) context.getNullableRefArgument(1);
        
        Path newPath = Paths.get(path.toString());
        for (int i = 0; i < pathsToAppend.size(); i++) {
            newPath = newPath.resolve(pathsToAppend.getString(i));
        }
    
        BMap<String, BValue> parentPath = BLangConnectorSPIUtil.createObject(context, Constants.PACKAGE_PATH, Constants
                .PATH_STRUCT, new BString(newPath.toString()));
        context.setReturnValues(parentPath);
    }

    public static ObjectValue resolve(Strand strand, ObjectValue self, ArrayValue paths) {
        Path path = (Path) self.getNativeData(Constants.PATH_DEFINITION_NAME);

        Path newPath = Paths.get(path.toString());
        for (int i = 0; i < paths.size(); i++) {
            newPath = newPath.resolve(paths.getString(i));
        }

        ObjectValue parentPathStruct = BallerinaValues.createObjectValue(Constants.PACKAGE_PATH, Constants
                .PATH_STRUCT, "");
        parentPathStruct.call(strand, Constants.INIT_FUNCTION_NAME, newPath.toString(), true);
        return parentPathStruct;
    }
}
