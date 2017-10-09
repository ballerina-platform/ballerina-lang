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
package org.ballerinalang.nativeimpl.task;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.PrintStream;


/**
 * Native function ballerina.model.task:stopTask.
 */
@BallerinaFunction(
    packageName = "ballerina.task",
    functionName = "stopTask",
    args = {@Argument(name = "taskID", type = TypeKind.INT)},
    returnType = {@ReturnType(type = TypeKind.ANY)},
    isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Stops the scheduled task service") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "taskID",
        value = "The identifier of the task") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "any)",
        value = "The error which is occurred while stopping the task") })
public class StopTask extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BString error = new BString("");
        PrintStream out = System.out;
        out.println("In Stop Task Class!!!!!");
        long taskId = getIntArgument(ctx, 0);
        TaskScheduler.stopTask(ctx, (int) taskId);
        out.println("StopTask:: " + taskId);
        return getBValues(error);
    }
}
