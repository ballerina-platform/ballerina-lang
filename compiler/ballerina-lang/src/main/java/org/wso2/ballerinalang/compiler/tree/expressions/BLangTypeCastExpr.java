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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.TypeCastNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.programfile.Instruction.RegIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangTypeCastExpr extends BLangExpression implements TypeCastNode, MultiReturnExpr {

    public BLangExpression expr;
    public BLangType typeNode;
    public List<BType> types = new ArrayList<>(0);
    public BOperatorSymbol castSymbol;
    private RegIndex[] regIndexes;

    public ExpressionNode getExpression() {
        return expr;
    }

    public void setExpression(ExpressionNode expr) {
        this.expr = (BLangExpression) expr;
    }

    public BLangType getTypeNode() {
        return typeNode;
    }

    public void setTypeNode(TypeNode typeNode) {
        this.typeNode = (BLangType) typeNode;
    }

    public boolean isMultiReturnExpr() {
        // Unsafe casts are multi return expressions
        return castSymbol == null || !((BCastOperatorSymbol) castSymbol).safe;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TYPE_CAST_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + String.valueOf(type) + ") " + String.valueOf(expr);
    }

    @Override
    public List<BType> getTypes() {
        return types;
    }

    @Override
    public void setTypes(List<BType> types) {
        this.types = types;
    }

    public RegIndex[] getRegIndexes() {
        return regIndexes;
    }

    public void setRegIndexes(RegIndex[] regIndexes) {
        this.regIndexes = regIndexes;
        this.regIndex = regIndexes != null && regIndexes.length > 0 ? regIndexes[0] : null;
    }
}
