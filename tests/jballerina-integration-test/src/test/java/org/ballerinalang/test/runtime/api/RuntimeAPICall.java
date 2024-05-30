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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

import java.io.PrintStream;

/**
 * Source class to test the functionality of Ballerina runtime APIs for invoking functions.
 *
 * @since 2201.9.0
 */
public class RuntimeAPICall {

    private static final PrintStream out = System.out;

    public static void main(String[] args) {
        Module module = new Module("testorg", "function_invocation", "1");
        Runtime balRuntime = Runtime.from(module);
        balRuntime.init();
        balRuntime.start();
        balRuntime.invokeMethodAsync("add",
                new Callback() {
                    @Override
                    public void notifySuccess(Object result) {
                        out.println(result);
                    }

                    @Override
                    public void notifyFailure(BError error) {
                        out.println("Error: " + error);
                    }
                }, 5L, 7L);

        BObject person = ValueCreator.createObjectValue(module, "Person", 1001,
                StringUtils.fromString("John Doe"));
        balRuntime.invokeMethodAsyncSequentially(person, "getNameWithTitle", null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                out.println(result);
            }

            @Override
            public void notifyFailure(BError error) {
                out.println("Error: " + error);
            }
        }, null, PredefinedTypes.TYPE_STRING, StringUtils.fromString("Dr. "), false);

        balRuntime.stop();

        balRuntime = Runtime.from(new Module("testorg", "function_invocation.moduleA", "1"));
        balRuntime.init();
        balRuntime.start();
        balRuntime.invokeMethodAsync("getPerson",
                new Callback() {
                    @Override
                    public void notifySuccess(Object result) {
                        out.println(result);
                    }

                    @Override
                    public void notifyFailure(BError error) {
                        out.println("Error: " + error);
                    }
                }, 1001L, StringUtils.fromString("John"), StringUtils.fromString("100m"));
        balRuntime.stop();
    }
}
