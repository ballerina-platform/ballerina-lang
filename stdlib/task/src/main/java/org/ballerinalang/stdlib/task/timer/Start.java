/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.task.timer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_DELAY;
import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_INTERVAL;
import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_IS_RUNNING_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_ON_ERROR_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_ON_TRIGGER_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_TASK_ID_FIELD;

/**
 * Native function ballerina/task:Timer.start.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "task",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Timer", structPackage = "ballerina/task"),
        isPublic = true
)
public class Start extends BlockingNativeCallableUnit {
    public void execute(Context ctx) {
        BMap<String, BValue> task = (BMap<String, BValue>) ctx.getRefArgument(0);
        boolean isRunning = ((BBoolean) task.get(TIMER_IS_RUNNING_FIELD)).booleanValue();
        if (isRunning) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.TASK_ALREADY_RUNNING);
        }

        FunctionRefCPEntry onTriggerFunctionRefCPEntry = ((BFunctionPointer) task.get(TIMER_ON_TRIGGER_FIELD)).value();
        BValue onErrorFunc = task.get(TIMER_ON_ERROR_FIELD);
        FunctionRefCPEntry onErrorFunctionRefCPEntry =
                onErrorFunc != null ? ((BFunctionPointer) onErrorFunc).value() : null;

        long delay = ((BInteger) task.get(TIMER_DELAY)).intValue();
        long interval = ((BInteger) task.get(TIMER_INTERVAL)).intValue();
        if (delay == -1) {
            delay = interval;
        }

        try {
            Timer timer =
                    new Timer(this, ctx, delay, interval, onTriggerFunctionRefCPEntry, onErrorFunctionRefCPEntry);
            task.put(TIMER_TASK_ID_FIELD, new BString(timer.getId()));
            task.put(TIMER_IS_RUNNING_FIELD, new BBoolean(true));
        } catch (SchedulingException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_TASK_CONFIG);
        }
    }
}
