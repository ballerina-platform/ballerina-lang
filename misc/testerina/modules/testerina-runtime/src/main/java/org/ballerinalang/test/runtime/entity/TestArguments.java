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

    private static final int ARGUMENTS_NUMBER = 10;

    private final Class<?>[] argTypes;
    private final Object[] argValues;

    public TestArguments(String targetPath, String packageName, String moduleName, String report, String coverage,
                         String groups, String disableGroups,  String tests, String rerunFailed, String listGroups) {

        argTypes = new Class[ARGUMENTS_NUMBER + 1];
        argValues = new Object[ARGUMENTS_NUMBER + 1];

        argTypes[0] = Strand.class;
        argValues[0] = null;

        assignValues(0, targetPath);
        assignValues(1, packageName);
        assignValues(2, moduleName);
        assignValues(3, report);
        assignValues(4, coverage);
        assignValues(5, groups);
        assignValues(6, disableGroups);
        assignValues(7, tests);
        assignValues(8, rerunFailed);
        assignValues(9, listGroups);
    }

    public Class<?>[] getArgTypes() {

        return argTypes;
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
