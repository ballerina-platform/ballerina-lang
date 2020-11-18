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
package org.wso2.ballerinalang.compiler.tree.bindingpatterns;

import org.ballerinalang.model.symbols.VariableSymbol;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.bindingpattern.CaptureBindingPattern;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since Swan Lake
 */
public class BLangCaptureBindingPattern extends BLangBindingPattern implements CaptureBindingPattern {

    BLangIdentifier identifier;
    public BVarSymbol symbol;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CAPTURE_BINDING_PATTERN;
    }

    @Override
    public IdentifierNode getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(IdentifierNode identifier) {
        this.identifier = (BLangIdentifier) identifier;
    }

    @Override
    public VariableSymbol getSymbol() {
        return symbol;
    }
}
