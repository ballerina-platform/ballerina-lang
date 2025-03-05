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
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BNever;
import io.ballerina.runtime.api.values.BObject;

import java.io.PrintStream;

/**
 * Source class to test the functionality of Ballerina runtime APIs for invoking functions.
 *
 * @since 2201.9.0
 */
public final class RuntimeAPICall {

    private RuntimeAPICall() {
    }

    private static final PrintStream out = System.out;

    public static void main(String[] args) {
        Module module = new Module("testorg", "function_invocation", "1");
        Runtime balRuntime = Runtime.from(module);
        balRuntime.init();
        balRuntime.start();
        Object result = balRuntime.callFunction(module, "add", null, 5L, 7L);
        out.println(result);

        BObject person = ValueCreator.createObjectValue(module, "Person", 1001, StringUtils.fromString("John Doe"));
        result = balRuntime.callMethod(person, "getNameWithTitle", null, StringUtils.fromString("Dr. "));
        out.println(result);
        result = balRuntime.callMethod(person, "getInfo", null, StringUtils.fromString("Dr."), BNever.getValue());
        out.println(result);
        result = balRuntime.callMethod(person, "getInfo", null, StringUtils.fromString("Dr."),
                StringUtils.fromString("bus"));
        out.println(result);
        result = balRuntime.callMethod(person, "getInfo", null, StringUtils.fromString("Dr."),
                StringUtils.fromString("bus"), StringUtils.fromString("Has a cat"));
        out.println(result);
        result = balRuntime.callMethod(person, "getInfo", null, StringUtils.fromString("Dr."),
                StringUtils.fromString("bus"), StringUtils.fromString("Has a cat"),
                StringUtils.fromString("Likes physics"));
        out.println(result);
        try {
            result = balRuntime.callMethod(person, "getInfo", null);
            out.println(result);
        } catch (Exception e) {
            out.println(e.getMessage());
        }
        balRuntime.stop();

        module = new Module("testorg", "function_invocation.moduleA", "1");
        balRuntime = Runtime.from(module);
        balRuntime.init();
        balRuntime.start();
        result = balRuntime.callFunction(module, "getPerson", null, 1001L, StringUtils.fromString("John"),
                StringUtils.fromString("100m"));
        out.println(result);
        balRuntime.stop();
    }
}
