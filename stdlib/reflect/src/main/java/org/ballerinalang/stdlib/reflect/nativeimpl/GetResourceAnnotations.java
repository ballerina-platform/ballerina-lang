/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Get Resource's Annotations.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "reflect",
        functionName = "getResourceAnnotations"
)
public class GetResourceAnnotations extends AbstractAnnotationReader {

    @Override
    public void execute(Context context) {
        BType serviceType = context.getRefArgument(0).getType();
        String key = serviceType.getName() + DOT + context.getStringArgument(0);
        context.setReturnValues(getAnnotationValue(context, serviceType.getPackagePath(), key));
    }

    public static ArrayValue getResourceAnnotations(Strand strand, ObjectValue service, String resourceName) {
        String key = service.getType().getName() + DOT + resourceName;
        return getAnnotationValue(service.getType(), key);
    }
}
