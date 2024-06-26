/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.runtime.api;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.Runtime;

/**
 * Source class to test the functionality of Ballerina runtime APIs for invoking functions.
 *
 * @since 2201.9.0
 */
public class ModuleStartCallNegative {

    public static void main(String[] args) {
        Module module = new Module("testorg", "function_invocation_negative", "1");
        Runtime balRuntime = Runtime.from(module);
        balRuntime.start();
    }
}
