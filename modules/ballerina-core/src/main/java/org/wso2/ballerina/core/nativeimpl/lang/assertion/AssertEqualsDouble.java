/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.nativeimpl.lang.assertion;

import org.wso2.ballerina.core.exception.AssertionFailedException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Native function ballerina.lang.assertion:assertEquals
 */
@BallerinaFunction(
        packageName = "ballerina.lang.assertion",
        functionName = "assertEquals",
        args = {@Argument(name = "actual", type = TypeEnum.DOUBLE),
                @Argument(name = "expected", type = TypeEnum.DOUBLE),
                @Argument(name = "message", type = TypeEnum.STRING) },
        isPublic = true
)
public class AssertEqualsDouble extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BDouble actual = ((BDouble) getArgument(context, 0));
        BDouble expected = ((BDouble) getArgument(context, 1));
        String message = getArgument(context, 2).stringValue();

        if (Double.compare(actual.doubleValue(), expected.doubleValue()) != 0) {
            if (("").equals(message)) {
                message = "Double not equal: expected: " + expected.doubleValue() + " and actual: " +
                        actual.doubleValue();
            }
            throw new AssertionFailedException(message);

        } else {
            return VOID_RETURN;
        }

    }
}
