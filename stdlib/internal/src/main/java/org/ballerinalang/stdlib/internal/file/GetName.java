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

package org.ballerinalang.stdlib.internal.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;

import java.nio.file.Path;

/**
 * Creates the file at the path specified in the File struct.
 *
 * @since 0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "getName",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                             structPackage = Constants.PACKAGE_PATH)
        ,
        returnType = {
                @ReturnType(type = TypeKind.STRING)
        },
        isPublic = true
)
public class GetName extends BlockingNativeCallableUnit {
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BMap<String, BValue> pathStruct = (BMap<String, BValue>) context.getRefArgument(0);
        Path path = (Path) pathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        Path fileName = path.getFileName();
        context.setReturnValues(new BString(fileName == null ? "" : fileName.toString()));
    }
}
