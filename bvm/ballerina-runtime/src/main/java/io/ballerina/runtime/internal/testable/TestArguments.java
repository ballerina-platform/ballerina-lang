/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.testable;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Strand;

/**
 * Parameters for testable main method.
 *
 * @since 2201.6.0
 */
public class TestArguments {

    private static final int ARGUMENTS_NUMBER = 11;

    private final Class<?>[] argTypes;
    private final Object[] argValues;

    public TestArguments(String... args) {
        argTypes = new Class[ARGUMENTS_NUMBER + 1];
        argValues = new Object[ARGUMENTS_NUMBER + 1];
        argTypes[0] = Strand.class;
        argValues[0] = null;

        assignValues(0, args[0]);
        assignValues(1, args[1]);
        assignValues(2, args[2]);
        assignValues(3, args[3]);
        assignValues(4, args[4]);
        assignValues(5, args[5]);
        assignValues(6, args[6]);
        assignValues(7, args[7]);
        assignValues(8, args[8]);
        assignValues(9, args[9]);
        assignValues(10, args[10]);
    }

    public Object[] getArgValues() {
        return argValues;
    }

    private void assignValues(int position, String argValue) {
        int index = position + 1;
        argTypes[index] = BString.class;
        argValues[index] = StringUtils.fromString(argValue);
    }
}
