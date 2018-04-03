/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.util.Utils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * Native function ballerina.test:startServices.
 * Starts all the services in a ballerina package.
 *
 * @since 0.94.1
 */
@BallerinaFunction(orgName = "ballerina", packageName = "test", functionName = "startServices", args = {@Argument
        (name = "packageName", type = TypeKind.STRING)}, returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value", value = "Starts all " +
        "the" + " services defined in the package specified in the 'packageName' argument")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "packageName", value = "Name of the "
        + "package")})
public class StartServices extends BlockingNativeCallableUnit {

    /**
     * Starts all the services defined in the package specified in the 'packageName' argument.
     */
    @Override
    public void execute(Context ctx) {
        String packageName = ctx.getStringArgument(0);

        for (ProgramFile programFile : TesterinaRegistry.getInstance().getProgramFiles()) {
            // Get the service package
            PackageInfo servicesPackage = programFile.getEntryPackage();
            if (servicesPackage == null || !servicesPackage.getPkgPath().equals(packageName)) {
                continue;
            }

            Utils.startService(programFile);
            ctx.setReturnValues(new BBoolean(true));
            return;
        }

        // package could not be found
        ctx.setReturnValues(new BBoolean(false));
    }

}
