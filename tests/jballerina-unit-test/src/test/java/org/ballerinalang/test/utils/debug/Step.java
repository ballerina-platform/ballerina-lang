/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.utils.debug;

/**
 * @since 0.94
 */
public enum Step {
    STEP_IN("1"),
    STEP_OVER("2"),
    STEP_OUT("3"),
    RESUME("5"),
    EXIT("0");

    private String value;

    Step(String value) {
        this.value = value;
    }

    public static Step fromValue(String value) {
        switch (value) {
            case "1":
                return STEP_IN;
            case "2":
                return STEP_OVER;
            case "3":
                return STEP_OUT;
            case "5":
                return RESUME;
            case "0":
                return EXIT;
            default:
                throw new IllegalArgumentException("invalid compiler phase: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
    }
