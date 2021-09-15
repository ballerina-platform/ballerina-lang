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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TableKeySpecifierNode;
import org.ballerinalang.model.tree.TableKeyTypeConstraintNode;
import org.ballerinalang.model.tree.types.TableTypeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;

/**
 * {@code BLangTableTypeNode} represents a table type node in Ballerina.
 *
 * @since 1.3.0
 */
public class BLangTableTypeNode extends BLangType implements TableTypeNode {
    // BLangNodes
    public BLangType type;
    public BLangType constraint;
    public BLangTableKeySpecifier tableKeySpecifier;
    public BLangTableKeyTypeConstraint tableKeyTypeConstraint;

    // Parser Flags and Data
    public boolean isTypeInlineDefined;

    // Semantic Data
    public BTableType tableType;

    @Override
    public TypeNode getConstraint() {
        return this.constraint;
    }

    @Override
    public void setConstraint(TypeNode typeNode) {
        this.constraint = (BLangType) typeNode;
    }

    @Override
    public TableKeySpecifierNode getTableKeySpecifier() {
        return this.tableKeySpecifier;
    }

    @Override
    public void setTableKeySpecifier(TableKeySpecifierNode tableKeySpecifierNode) {
        this.tableKeySpecifier = (BLangTableKeySpecifier) tableKeySpecifierNode;
    }

    @Override
    public TableKeyTypeConstraintNode getTableKeyTypeConstraint() {
        return this.tableKeyTypeConstraint;
    }

    @Override
    public void setTableKeyTypeConstraint(TableKeyTypeConstraintNode tableKeyTypeConstraint) {
        this.tableKeyTypeConstraint = (BLangTableKeyTypeConstraint) tableKeyTypeConstraint;
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
    public NodeKind getKind() {
        return NodeKind.TABLE_TYPE;
    }

    @Override
    public String toString() {
        return ("table<" + this.constraint.toString() + "> " +
                ((this.tableKeySpecifier != null) ? this.tableKeySpecifier.toString() :
                        ((this.tableKeyTypeConstraint != null) ? this.tableKeyTypeConstraint.toString() : ""))).trim();
    }
}
