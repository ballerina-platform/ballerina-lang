/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.reflect.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Get a resource's annotations.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "reflect",
        functionName = "getResourceAnnotationsExternal"
)
public class GetResourceAnnotationsExternal extends BlockingNativeCallableUnit {

    public static Object getResourceAnnotationsExternal(Strand strand, ObjectValue service, String resourceName,
                                                        String annot) {
        AttachedFunction[] functions = service.getType().getAttachedFunctions();

        for (AttachedFunction function : functions) {
            if (function.funcName.equals(resourceName)) {
                return function.getAnnotation(annot);
            }
        }
        return null;
    }

    @Override
    public void execute(Context context) {

    }
}
