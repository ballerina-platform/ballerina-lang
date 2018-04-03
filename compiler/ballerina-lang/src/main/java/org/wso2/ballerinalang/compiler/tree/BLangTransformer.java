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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TransformerNode;
import org.ballerinalang.model.tree.VariableNode;

import java.util.List;

/**
 * @since 0.94.2
 */
public class BLangTransformer extends BLangInvokableNode implements TransformerNode {

    public BLangVariable source;
    public List<BLangVariable> retParams;

    public VariableNode getSource() {
        return source;
    }

    public void setSource(VariableNode source) {
        this.source = (BLangVariable) source;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TRANSFORMER;
    }
    
    @Override
    public String toString() {
        return "BLangTransformer: " + super.toString();
    }
    
}
