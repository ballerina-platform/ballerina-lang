/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.ballerinalang.nativeimpl.file.utils.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initializes a given file location to a path.
 *
 * @since ballerina-0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "file",
        functionName = "Path.init",
        args = {
                @Argument(name = "path", type = TypeKind.STRUCT, structType = "Path", structPackage = "ballerina.file"),
                @Argument(name = "link", type = TypeKind.STRING)
        },
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {
    /**
     * Retrieves the path from the given location.
     *
     * @param path the values of the path.
     * @return reference to the path location.
     */
    private Path getPath(String path) {
        return Paths.get(path);
    }

    @Override
    public void execute(Context context) {
        String basePath = context.getStringArgument(0);
        BStruct path = (BStruct) context.getRefArgument(0);
        path.addNativeData(Constants.PATH_DEFINITION_NAME, getPath(basePath));
    }
}
