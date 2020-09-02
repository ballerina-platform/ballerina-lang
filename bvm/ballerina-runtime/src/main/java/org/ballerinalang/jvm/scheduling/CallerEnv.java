/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.scheduling;

/**
 * Class represents the environment information of function call.
 *
 * @since 2.0.0
 */
public class CallerEnv {

    /**
     * Module of the function call.
     */
    private final Module module;

    /**
     * Position of the function call.
     */
    private final DiagnosticPos diagnosticPos;

    public CallerEnv(Module module, DiagnosticPos diagnosticPos) {
        this.module = module;
        this.diagnosticPos = diagnosticPos;
    }

    /**
     * Gets the module that function call was initiated.
     *
     * @return Function call module.
     */
    public Module getModule() {
        return module;
    }

    /**
     * Gets the position that function call was initiated.
     *
     * @return Function call position info.
     */
    public DiagnosticPos getDiagnosticPos() {
        return diagnosticPos;
    }
}
