/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazyLoadingGlobalVarCollector {

    // Map use to store global var name vs instructions
    private final Map<String, List<BIRInstruction>> lazyLoadingInsMap;

    // Map use to store global var name vs call terminator
    private final Map<String, BIRTerminator.Call> lazyLoadingCallMap;

    public LazyLoadingGlobalVarCollector() {
        this.lazyLoadingInsMap = new HashMap<>();
        this.lazyLoadingCallMap = new HashMap<>();
    }

    public void add(String varName, List<BIRInstruction> instructions) {
        lazyLoadingInsMap.put(varName, instructions);
    }

    public void add(String varName, BIRTerminator.Call call) {
        lazyLoadingCallMap.put(varName, call);
    }

    public Map<String, List<BIRInstruction>> getLazyLoadingMap() {
        return lazyLoadingInsMap;
    }

    public Map<String, BIRTerminator.Call> getLazyLoadingCallMap() {
        return lazyLoadingCallMap;
    }
}
