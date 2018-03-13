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
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * Native function ballerina.lang.test:stopService.
 * Stops a given ballerina service.
 *
 * @since 0.9.4
 */
@BallerinaFunction(packageName = "ballerina.test",
                   functionName = "stopService", args = {
        @Argument(name = "serviceName", type = TypeKind.STRING)}, returnType = {
        @ReturnType(type = TypeKind.STRING)}, isPublic = true)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value",
                                               value = "Stops the service specified in the 'serviceName' argument") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "serviceName",
                                               value = "Name of the service to be stopped") })
public class StopService extends BlockingNativeCallableUnit {

    /**
     * Stops the service specified in the 'serviceName' argument.
     */
    @Override
    public void execute(Context ctx) {
        String serviceName = ctx.getStringArgument(0);

        for (ProgramFile programFile : TesterinaRegistry.getInstance().getProgramFiles()) {
            // 1) First, we get the Service for the given serviceName from the original ProgramFile
            ServiceInfo matchingService = programFile.getEntryPackage().getServiceInfo(serviceName);
            if (matchingService != null) {
                BLangFunctions.invokeVMUtilFunction(matchingService.getPackageInfo().getStopFunctionInfo());
                break;
            }
        }
    }

}
