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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.XMLProcessingInstructionLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangXMLProcInsLiteral extends BLangExpression implements XMLProcessingInstructionLiteralNode {

    public BLangLiteral target;
    public List<BLangExpression> dataFragments;
    public BLangExpression dataConcatExpr;
    
    public BLangXMLProcInsLiteral() {
        dataFragments = new ArrayList<BLangExpression>();
    }
    
    @Override
    public LiteralNode getTarget() {
        return target;
    }
    
    @Override
    public void setTarget(LiteralNode target) {
        this.target = (BLangLiteral) target;
    }

    @Override
    public List<BLangExpression> getDataTextFragments() {
        return dataFragments;
    }

    @Override
    public void addDataTextFragment(ExpressionNode textFragment) {
        this.dataFragments.add((BLangExpression) textFragment);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XML_PI_LITERAL;
    }

    @Override
    public String toString() {
        return "BLangXMLProcInsLiteral: " + dataFragments;
    }
}
