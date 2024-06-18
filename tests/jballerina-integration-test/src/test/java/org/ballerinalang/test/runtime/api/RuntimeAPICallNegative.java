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
import io.ballerina.runtime.api.values.BError;

import java.io.PrintStream;

import static org.ballerinalang.test.runtime.api.RuntimeAPITestUtils.blockAndInvokeMethodAsync;

/**
 * Source class to test the functionality of Ballerina runtime APIs for invoking functions.
 *
 * @since 2201.9.0
 */
public final class RuntimeAPICallNegative {

    private static final PrintStream out = System.out;

    private RuntimeAPICallNegative() {
    }

    public static void main(String[] args) {
        Module module = new Module("testorg", "function_invocation", "1");
        Runtime balRuntime = Runtime.from(module);

        // Test function called before module initialization error for add, start and stop functions
        try {
            blockAndInvokeMethodAsync(balRuntime, "add");
        } catch (BError e) {
            out.println(e.getMessage());
        }
        try {
            balRuntime.start();
        } catch (BError e) {
            out.println(e.getMessage());
        }
        try {
            balRuntime.stop();
        } catch (BError e) {
            out.println(e.getMessage());
        }

        // Test already called error for init, start and stop functions
        balRuntime.init();
        try {
            balRuntime.init();
        } catch (BError e) {
            out.println(e.getMessage());
        }
        balRuntime.start();
        try {
            balRuntime.start();
        } catch (BError e) {
            out.println(e.getMessage());
        }
        balRuntime.stop();
        try {
            balRuntime.stop();
        } catch (BError e) {
            out.println(e.getMessage());
        }

        // Test non-existing ballerina module
        module = new Module("testorg", "non-exist", "1");
        balRuntime = Runtime.from(module);
        try {
            balRuntime.init();
        } catch (BError e) {
            out.println(e.getMessage());
        }
    }
}
