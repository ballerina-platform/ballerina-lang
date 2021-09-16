/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.TableKeySpecifierNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.TableConstructorExprNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Table constructor expression.
 *
 * @since 1.3.0
 */
public class BLangTableConstructorExpr extends BLangExpression implements TableConstructorExprNode {

    // BLangNodes
    public BLangTableKeySpecifier tableKeySpecifier;
    public List<BLangRecordLiteral> recordLiteralList = new ArrayList<>();

    @Override
    public void setTableKeySpecifier(TableKeySpecifierNode tableKeySpecifierNode) {
        this.tableKeySpecifier = (BLangTableKeySpecifier) tableKeySpecifierNode;
    }

    @Override
    public TableKeySpecifierNode getTableKeySpecifier() {
        return this.tableKeySpecifier;
    }

    @Override
    public void addRecordLiteral(RecordLiteralNode recordLiteralNode) {
        this.recordLiteralList.add((BLangRecordLiteral) recordLiteralNode);
    }

    @Override
    public List<BLangRecordLiteral> getRecordLiteralList() {
        return this.recordLiteralList;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TABLE_CONSTRUCTOR_EXPR;
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
    public <T, R> R apply(BLangNodeModifier<T, R> modifier, T props) {
        return modifier.modify(this, props);
    }

    @Override
    public String toString() {
        //TODO pending Table Impl
        return " ";
    }
}
