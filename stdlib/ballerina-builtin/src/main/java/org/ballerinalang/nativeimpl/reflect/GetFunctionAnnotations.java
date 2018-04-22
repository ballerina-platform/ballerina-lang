/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.nativeimpl.reflect;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Get Function's Annotations.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "reflect",
        functionName = "getFunctionAnnotations"
)
public class GetFunctionAnnotations extends AbstractAnnotationReader {

    @Override
    public void execute(Context context) {
        BValue bValue = context.getRefArgument(0);
        if (!(bValue instanceof BFunctionPointer)) {
            context.setReturnValues((BValue) null);
        }
        BFunctionPointer fp = (BFunctionPointer) bValue;
        context.setReturnValues(getAnnotationValue(context, fp.value().getPackagePath(), fp.value().getFunctionName()));
    }
}
