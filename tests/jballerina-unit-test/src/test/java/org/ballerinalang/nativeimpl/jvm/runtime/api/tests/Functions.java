/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.ValueCreator;

/**
 * This class contains a set of utility methods required for runtime api functions testing.
 *
 * @since 2201.2.0
 */
public class Functions {

    private static final Module functionModule = new Module("testorg", "runtime_api_types.functions", "1");

    public static int getFunctionParameterCountByName(BString functionName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(functionModule, false));
        return valueCreator.getFunctionType(functionName.getValue()).getParameters().length;
    }
}
