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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for command-line options.
 *
 * @since 0.94
 */
public class CompilerOptions {

    private static final CompilerContext.Key<CompilerOptions> OPTIONS_KEY =
            new CompilerContext.Key<>();

    private Map<CompilerOptionName, String> optionMap;

    public static CompilerOptions getInstance(CompilerContext context) {
        CompilerOptions options = context.get(OPTIONS_KEY);
        if (options == null) {
            options = new CompilerOptions(context);
        }
        return options;
    }

    private CompilerOptions(CompilerContext context) {
        context.put(OPTIONS_KEY, this);
        optionMap = new HashMap<>();
    }

    public String get(CompilerOptionName optionName) {
        return optionMap.get(optionName);
    }

    public boolean isSet(CompilerOptionName optionName) {
        return optionMap.containsKey(optionName);
    }

    public void put(CompilerOptionName optionName, String value) {
        optionMap.put(optionName, value);
    }

    public CompilerPhase getCompilerPhase() {
        String phaseName = get(CompilerOptionName.COMPILER_PHASE);
        if (phaseName == null || phaseName.isEmpty()) {
            return CompilerPhase.CODE_GEN; //TODO may need to remove this as this is not generic return value
        }

        return CompilerPhase.fromValue(phaseName);
    }
}
