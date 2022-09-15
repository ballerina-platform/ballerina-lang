// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.test.runtime.entity;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Strand;

/**
 * Parameters for the runtime Testerina main function.
 *
 * @since 2201.3.0
 */
public class TestArguments {

    private static final int ARGUMENTS_NUMBER = 9;

    private final Class<?>[] argTypes;
    private final Object[] argValues;

    public TestArguments(String targetPath, String moduleName, String report, String coverage, String groups,
                         String disableGroups,  String tests, String rerunFailed, String listGroups) {

        argTypes = new Class[2 * ARGUMENTS_NUMBER + 1];
        argValues = new Object[2 * ARGUMENTS_NUMBER + 1];

        argTypes[0] = Strand.class;
        argValues[0] = null;

        assignValues(0, targetPath);
        assignValues(1, moduleName);
        assignValues(2, report);
        assignValues(3, coverage);
        assignValues(4, groups);
        assignValues(5, disableGroups);
        assignValues(6, tests);
        assignValues(7, rerunFailed);
        assignValues(8, listGroups);
    }

    public Class<?>[] getArgTypes() {

        return argTypes;
    }

    public Object[] getArgValues() {

        return argValues;
    }

    private void assignValues(int position, String argValue) {
        int index = 2 * position + 1;

        argTypes[index] = BString.class;
        argValues[index] = StringUtils.fromString(argValue);

        argTypes[index + 1] = boolean.class;
        argValues[index + 1] = true;
    }
}
