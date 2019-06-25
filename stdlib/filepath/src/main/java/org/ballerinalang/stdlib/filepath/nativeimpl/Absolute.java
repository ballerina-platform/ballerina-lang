/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.filepath.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.filepath.Constants;
import org.ballerinalang.stdlib.filepath.Utils;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;

/**
 * The native class to get absolute value of the path.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "absolute",
        isPublic = true
)
public class Absolute extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static Object absolute(Strand strand, String inputPath) {
        try {
            String absolutePath = FileSystems.getDefault().getPath(inputPath).toAbsolutePath().toString();
            return  absolutePath;
        } catch (InvalidPathException ex) {
            return Utils.getPathError("INVALID_PATH", ex);
        }
    }

}
