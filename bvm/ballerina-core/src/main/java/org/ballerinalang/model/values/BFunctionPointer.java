/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code {@link BFunctionPointer}} represents a function pointer reference value in Ballerina.
 *
 * @since 0.90
 */
public class BFunctionPointer implements BRefType<FunctionRefCPEntry> {

    FunctionRefCPEntry funcRefCPEntry;

    //container which keeps the closure variables values
    private List<BClosure> closureVars = new ArrayList<>();

    //map which keeps tracks of additional index count needed for closure vars
    private Map<Integer, Integer> additionalIndexes = new HashMap<>();

    public BFunctionPointer(FunctionRefCPEntry funcRefCPEntryIndex) {
        this.funcRefCPEntry = funcRefCPEntryIndex;
    }

    @Override
    public FunctionRefCPEntry value() {
        return funcRefCPEntry;
    }

    public List<BClosure> getClosureVars() {
        return closureVars;
    }

    public void addClosureVar(BClosure closure, int tag) {
        if (closureVars.contains(closure)) {
            return;
        }
        closureVars.add(closure);
        additionalIndexes.merge(tag, 1, Integer::sum);
    }

    public Integer getAdditionalIndexCount(int type) {
        return additionalIndexes.getOrDefault(type, 0);
    }

    @Override
    public String stringValue() {
        return BLangConstants.STRING_EMPTY_VALUE;
    }

    @Override
    public BType getType() {
        return new BFunctionType();
    }

    @Override
    public BValue copy() {
        return new BFunctionPointer(funcRefCPEntry);
    }
}
