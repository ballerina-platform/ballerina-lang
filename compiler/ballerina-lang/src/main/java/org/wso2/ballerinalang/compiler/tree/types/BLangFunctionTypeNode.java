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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.FunctionTypeNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FunctionTypeNode}.
 *
 * @since 0.94
 */
public class BLangFunctionTypeNode extends BLangType implements FunctionTypeNode {

    public List<BLangType> paramTypeNodes = new ArrayList<>();
    public BLangType returnTypeNode;

    public boolean returnsKeywordExists = false;

    @Override
    public List<BLangType> getParamTypeNode() {
        return this.paramTypeNodes;
    }

    @Override
    public BLangType getReturnTypeNode() {
        return this.returnTypeNode;
    }

    @Override
    public boolean isReturnKeywordExists() {
        return this.returnsKeywordExists;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.FUNCTION_TYPE;
    }

    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        br.append("function(");
        if (paramTypeNodes.size() > 0) {
            br.append(getParamNames(paramTypeNodes));
        }
        if (returnTypeNode != null) {
            br.append(returnsKeywordExists ? ")returns(" : ")(");
            br.append(returnTypeNode);
        }
        br.append(")");
        return br.toString();
    }

    private String getParamNames(List<BLangType> paramTypes) {
        return paramTypes.stream().map(paramType -> {
            if (paramType.getKind() == NodeKind.USER_DEFINED_TYPE) {
                return ((UserDefinedTypeNode) paramType).getTypeName().getValue();
            } else {
                return paramType.toString();
            }
        }).collect(Collectors.joining(", "));
    }
}
