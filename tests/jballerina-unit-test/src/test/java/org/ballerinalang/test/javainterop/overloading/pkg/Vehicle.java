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
package org.ballerinalang.test.javainterop.overloading.pkg;

import io.ballerina.runtime.api.values.BString;

/**
 * A test class.
 */
public class Vehicle {

    private String name;

    public Vehicle(String name) {

        this.name = name;
    }

    public Object getName() {

        return this.name;
    }

    public Object getDescription(String prefix) {

        return prefix + this.name;
    }

    public static String moveTo(BString location) {
        return location.toString();
    }

    public static String moveTo(Long locationNumber) {
        return Long.toString(locationNumber);
    }
}
