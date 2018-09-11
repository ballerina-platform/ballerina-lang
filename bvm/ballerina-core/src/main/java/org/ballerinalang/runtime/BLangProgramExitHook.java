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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.runtime;

import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Ballerina program exit hook which contains the exit function pointer that should be invoked when program is exiting.
 * For each exit hook addition at ballerina program level, an instance of this will be registered with the registry.
 *
 * @since 0.982.0
 */
public class BLangProgramExitHook {

    private BFunctionPointer exitFunctionPointer;

    public BLangProgramExitHook(BFunctionPointer exitFunctionPointer) {
        this.exitFunctionPointer = exitFunctionPointer;
    }

    /**
     * The invoke method would invoke the function pointer registered with the hook which should be invoked when
     * program is exiting.
     *
     * @return the return values from exitHook function invocation
     */
    public BValue[] invoke() {
        final List<BValue> functionArgs = new ArrayList<>();
        exitFunctionPointer.getClosureVars().forEach(closure -> functionArgs.add(closure.value()));
        return BLangFunctions.invokeCallable(exitFunctionPointer.value(), functionArgs.toArray(new BValue[0]));
    }
}
