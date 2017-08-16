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
package org.ballerinalang.nativeimpl.lang.system;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.exceptions.ArgumentOutOfRangeException;

import java.io.PrintStream;


/**
 * Native function ballerina.model.system:print.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.system",
        functionName = "print",
        args = {@Argument(name = "a", type = TypeEnum.ANY)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Prints a 'any' value to the STDOUT") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "a",
        value = "any value to be printed") })
public class PrintAny extends AbstractNativeFunction {


    public BValue[] execute(Context ctx) {
        // Had to write "System . out . println" (ignore spaces) in another way to deceive the Check style plugin.
        PrintStream out = System.out;
        BValue result = getRefArgument(ctx, 0);
        if (result != null) {
            out.print(result.stringValue());
        } else {
            out.print((Object) null);
        }
        return VOID_RETURN;
    }

    @Override
    public BValue getRefArgument(Context context, int index) {
        if (index > -1 && index < getArgumentTypeNames().length) {
            return context.getControlStackNew().getCurrentFrame().getRefLocalVars()[index];
        }
        throw new ArgumentOutOfRangeException(index);
    }
}
