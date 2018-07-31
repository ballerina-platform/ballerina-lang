/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the state such as the current node, enclosing package, function etc, during bir generation.
 *
 * @since 0.980.0
 */
public class BIRGenEnv {

    public BIRNode node;

    public BIRPackage enclPkg;

    public BIRFunction enclFunc;
    private int currentBBId = -1;
    private int currentLocalVarId = -1;
    private Map<BSymbol, BIRVariableDcl> symbolVarMap;

    public BIRBasicBlock enclBB;

    private BIRGenEnv() {
    }

    public static BIRGenEnv packageEnv(BIRPackage birPkg) {
        BIRGenEnv env = new BIRGenEnv();
        env.enclPkg = birPkg;
        return env;
    }

    public static BIRGenEnv funcEnv(BIRGenEnv enclEnv, BIRFunction birFunc) {
        BIRGenEnv env = duplicate(enclEnv);
        env.symbolVarMap = new HashMap<>();
        env.enclFunc = birFunc;
        return env;
    }

    public static BIRGenEnv bbEnv(BIRGenEnv enclEnv, BIRBasicBlock bb) {
        BIRGenEnv env = duplicate(enclEnv);
        env.enclBB = bb;
        return env;
    }

    public Name nextBBId(Names names) {
        currentBBId++;
        return names.merge(Names.BIR_BASIC_BLOCK_PREFIX, names.fromString(Integer.toString(currentBBId)));
    }

    public Name nextLocalVarId(Names names) {
        currentLocalVarId++;
        return names.merge(Names.BIR_LOCAL_VAR_PREFIX, names.fromString(Integer.toString(currentLocalVarId)));
    }

    public void addSymbolVarMapping(BSymbol varSymbol, BIRVariableDcl variableDcl) {
        this.symbolVarMap.put(varSymbol, variableDcl);
    }

    public BIRVariableDcl getVariableDcl(BSymbol varSymbol) {
        return this.symbolVarMap.get(varSymbol);
    }


    // private methods

    private static BIRGenEnv duplicate(BIRGenEnv fromEnv) {
        BIRGenEnv toEnv = new BIRGenEnv();
        toEnv.node = fromEnv.node;
        toEnv.enclPkg = fromEnv.enclPkg;
        toEnv.enclFunc = fromEnv.enclFunc;
        toEnv.enclBB = fromEnv.enclBB;
        return toEnv;
    }
}
