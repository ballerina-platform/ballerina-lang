/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.nativeimpl.lang.string;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function ballerina.lang.string:replaceFirst.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.string",
        functionName = "replaceFirst",
        args = {@Argument(name = "string", type = TypeEnum.STRING),
                @Argument(name = "replacePattern", type = TypeEnum.STRING),
                @Argument(name = "replaceWith", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class ReplaceFirst extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String mainString = getArgument(context, 0).stringValue();
        String replacePattern = getArgument(context, 1).stringValue();
        String replaceWith = getArgument(context, 2).stringValue();

        String replacedString = mainString.replaceFirst(replacePattern, replaceWith);
        return getBValues(new BString(replacedString));
    }
}
