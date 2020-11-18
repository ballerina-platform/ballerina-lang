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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.InvokableSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 0.94
 */
public class BInvokableSymbol extends BVarSymbol implements InvokableSymbol {

    public List<BVarSymbol> params;
    public BVarSymbol restParam;
    public BType retType;
    public Map<Integer, TaintRecord> taintTable;
    public List<BLangAnnotationAttachment> annAttachments;
    public Map<String, BType> paramDefaultValTypes;

    // This field is only applicable for functions at the moment.
    public BVarSymbol receiverSymbol;
    public boolean bodyExist;

    // Only applicable for workers within fork statements.
    public String enclForkName;
    public String source;
    public String strandName = null;
    public SchedulerPolicy schedulerPolicy = SchedulerPolicy.PARENT;


    public Set<BVarSymbol> dependentGlobalVars;

    public BInvokableSymbol(int tag,
                            int flags,
                            Name name,
                            PackageID pkgID,
                            BType type,
                            BSymbol owner,
                            DiagnosticPos pos,
                            SymbolOrigin origin) {
        super(flags, name, pkgID, type, owner, pos, origin);
        this.tag = tag;
        this.params = new ArrayList<>();
        this.annAttachments = new ArrayList<>();
        this.dependentGlobalVars = new HashSet<>();
        this.paramDefaultValTypes = new HashMap<>();
    }

    @Override
    public List<BVarSymbol> getParameters() {
        return params;
    }

    public BInvokableType getType() {
        if (type instanceof BInvokableType) {
            return (BInvokableType) type;
        }
        // Should never come here, this is to please the spotbugs
        throw new BLangCompilerException("Invokable symbol with non invokable type : " + type);
    }


    @Override
    public BType getReturnType() {
        return retType;
    }
}
