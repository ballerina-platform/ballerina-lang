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

package org.ballerinalang.stdlib.internal.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.internal.Constants;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Initializes a given file location to a path.
 *
 * @since ballerina-0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                             structPackage = Constants.PACKAGE_PATH)
        ,
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
        BMap<String, BValue> path = (BMap<String, BValue>) context.getRefArgument(0);
        path.addNativeData(Constants.PATH_DEFINITION_NAME, getPath(basePath));
    }
}
