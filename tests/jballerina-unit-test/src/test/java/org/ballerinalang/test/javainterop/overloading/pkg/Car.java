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

/**
 * A test class.
 */
public class Car extends Vehicle {

    private final String model;

    public Car(String name, String model) {

        super(name);
        this.model = model;
    }

    @Override
    public String getName() {

        return super.getName() + " : " + this.model;
    }

    @Override
    public String getDescription(String prefix) {

        return prefix + this.model;
    }

    public long getSeatCount() {
        return 4;
    }

    public static String getMaxSpeed(String model) {
        if (model.equals("BMW")) {
            return "200MPH";
        }
        return "160MPH";
    }

    public Object[] getCategorization(int num, String category) {
        return new Object[] {num, category};
    }

    public Object getBatteryType(long prefix) {
        return prefix + " EVL-HP-LiPo powered";
    }

    public static String getMillage(long val) {
        return val + "MPG";
    }
}
