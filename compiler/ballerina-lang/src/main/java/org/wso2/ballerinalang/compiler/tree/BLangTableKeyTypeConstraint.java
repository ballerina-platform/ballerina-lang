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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TableKeyTypeConstraintNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

/**
 * @since 1.3.0
 */
public class BLangTableKeyTypeConstraint extends BLangNode implements TableKeyTypeConstraintNode {

    public BLangType keyType;

    @Override
    public TypeNode getKeyTypeConstraint() {
        return this.keyType;
    }

    @Override
    public void setKeyTypeConstraint(TypeNode typeNode) {
        this.keyType = (BLangType) typeNode;
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
        return NodeKind.TABLE_KEY_TYPE_CONSTRAINT;
    }

    @Override
    public String toString() {
        return "key<" + this.keyType.toString() + ">";
    }

}
