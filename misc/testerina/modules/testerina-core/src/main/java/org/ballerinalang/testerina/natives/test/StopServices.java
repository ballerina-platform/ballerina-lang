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
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.ballerinalang.util.LaunchListener;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.ServiceLoader;

/**
 * Native function ballerina.test:stopServices.
 * Stops all the services in a ballerina module.
 *
 * @since 0.94.1
 */
@BallerinaFunction(orgName = "ballerina", packageName = "test", functionName = "stopServices", args = {@Argument(name
        = "moduleName", type = TypeKind.STRING)}, isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value", value = "Stops all the " +
        "" + "services defined in the module specified in the 'moduleName' argument")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "moduleName", value = "Name of the "
        + "module")})
public class StopServices extends BlockingNativeCallableUnit {

    /**
     * Stops all the services defined in the module specified in the 'moduleName' argument.
     */
    @Override
    public void execute(Context ctx) {
        String moduleName = ctx.getStringArgument(0);

        for (ProgramFile programFile : TesterinaRegistry.getInstance().getProgramFiles()) {
            PackageInfo servicesPackage = programFile.getEntryPackage();
            if (servicesPackage == null ||
                !servicesPackage.getPkgPath().equals(TesterinaUtils.getFullModuleName(moduleName))) {
                continue;
            }
            BLangFunctions.invokeVMUtilFunction(servicesPackage.getStopFunctionInfo());
            ServiceLoader<LaunchListener> listeners = ServiceLoader.load(LaunchListener.class);
            listeners.forEach(listener -> listener.afterRunProgram(true));
            break;
        }
    }
}
