    /*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.testerina.natives.test;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.util.Utils;
import org.ballerinalang.util.program.BLangFunctions;

import java.nio.file.Paths;

    /**
 * Native function ballerina.test:stopServiceSkeleton.
 * Stop a service skeleton and cleanup the directories that got created.
 *
 * @since 0.97.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "test", functionName = "stopServiceSkeleton", args =
        {@Argument(name = "packageName", type = TypeKind.STRING)}, isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value", value = "Stop a " +
        "service skeleton and cleanup created directories of a given ballerina package.")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "packageName", value = "Name of the " +
        "package")})
public class StopServiceSkeleton extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        String packageName = ctx.getStringArgument(0);

        TesterinaRegistry.getInstance().getSkeletonProgramFiles().forEach(skeletonProgramFile -> {
            if (skeletonProgramFile.getEntryPkgName().equals(packageName)) {
                // stop the service
                BLangFunctions.invokeVMUtilFunction(skeletonProgramFile.getEntryPackage().getStopFunctionInfo());
                // Clean up the package DIR
                Utils.cleanUpDir(Paths.get(System.getProperty(TesterinaConstants.BALLERINA_SOURCE_ROOT),
                    TesterinaConstants.TESTERINA_TEMP_DIR, packageName));
            }
        });
    }
}
