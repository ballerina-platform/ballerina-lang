/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerina.nativeimpl.lang.exceptions;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BException;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Native functions for ballerina.lang.exceptions to Set the category of an exception.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.exceptions",
        functionName = "setCategory",
        args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION),
                @Argument(name = "category", type = TypeEnum.STRING)},
        isPublic = true
)
public class SetCategory extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BException exception = (BException) getArgument(context, 0);
        String category = getArgument(context, 1).stringValue();
        exception.value().setCategory(category);
        return VOID_RETURN;
    }
}
