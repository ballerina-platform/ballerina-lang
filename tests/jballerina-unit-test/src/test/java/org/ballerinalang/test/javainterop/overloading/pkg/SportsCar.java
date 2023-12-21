/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.javainterop.overloading.pkg;

public class SportsCar extends Car {

    private final long seatCount;

    public SportsCar(String name, String model, long seatCount) {

        super(name, model);
        this.seatCount = seatCount;
    }

    @Override
    public String getDescription(String prefix) {
        return prefix + " seat count: " + seatCount;
    }

    public long getSeatCount() {
        return seatCount;
    }

    public String getDescription(long[] numProps) {
        return getName() + "| no of seats: " + seatCount + "| max speed: " + numProps[0] + "| mileage: " + numProps[1];
    }

    public static long getSeatCount(String model) {
        if (model.equals("Nissan-GTR")) {
            return 4;
        }
        return 2;
    }

    public static String getMaxSpeed(String model) {
        if (model.equals("Nissan-GTR")) {
            return "300MPH";
        }
        return "250MPH";
    }

    public char[] getCategorization(char category, char type) {
        return new char[] {category, type};
    }

    public String[] getCategorization(String category, String type) {
        return new String[] {category, type};
    }

    public String getBatteryType(String prefix) {
        return prefix + " EV-HY3-Lithium-ion power";
    }

    public static String getPrice(String model) {
        if (model.equals("Nissan-GTR")) {
            return "USD 100,000";
        }
        return "USD 50,000";
    }

    public static Object getPrice(Object type) {
        if (type.equals("BMW-S6")) {
            return " 120,000";
        }
        return " 60,000";
    }
}
