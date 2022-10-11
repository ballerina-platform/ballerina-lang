/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BResourceFunction} represents a resource function in Ballerina.
 *
 * @since 2.0
 */
public class BResourceFunction extends BAttachedFunction {

    public List<BVarSymbol> pathParams;
    public BVarSymbol restPathParam;
    public List<Name> resourcePath;
    public Name accessor;
    public BTupleType resourcePathType;

    public BResourceFunction(Name funcName, BInvokableSymbol symbol, BInvokableType type,
                             List<Name> resourcePath, Name accessor, List<BVarSymbol> pathParams,
                             BVarSymbol restPathParam, BTupleType resourcePathType, Location pos) {
        super(funcName, symbol, type, pos);
        this.resourcePath = resourcePath;
        this.accessor = accessor;
        this.pathParams = pathParams;
        this.restPathParam = restPathParam;
        this.resourcePathType = resourcePathType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("resource function ").append(accessor).append(" ");
        List<String> resourcePathStrings = new ArrayList<>(resourcePath.size());
        for (int i = 0; i < resourcePath.size(); i++) {
            Name resourcePath = this.resourcePath.get(i);
            if (resourcePath.value.equals("*") || resourcePath.value.equals("$*")) {
                resourcePathStrings.add("[" + resourcePathType.memberTypes.get(i) + "]");
            } else if (resourcePath.value.equals("**") || resourcePath.value.equals("$**")) {
                resourcePathStrings.add("[" + resourcePathType.restType + "...]");
            } else {
                resourcePathStrings.add(resourcePath.value);
            }
        }
        
        sb.append(resourcePathStrings.stream().reduce((a, b) -> a + "/" + b).get());
        
        // Remove path params from `type.paramTypes`, otherwise path prams will appear in function signature
        List<BType> originalParamTypes = type.paramTypes;
        int pathParamCount = this.pathParams.size() + (this.restPathParam == null ? 0 : 1);
        List<BType> paramTypes = new ArrayList<>(originalParamTypes.size() - pathParamCount);
        paramTypes.addAll(originalParamTypes.subList(pathParamCount, originalParamTypes.size()));
        type.paramTypes = paramTypes;
        
        sb.append(type.getTypeSignature());

        type.paramTypes = originalParamTypes;
        return sb.toString();
    }
}
