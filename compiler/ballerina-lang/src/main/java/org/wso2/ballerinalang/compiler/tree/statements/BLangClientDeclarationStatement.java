/*
*  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.ClientDeclarationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.ClientDeclarationStatementNode;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Represent a client declaration statement.
 *
 * @since 2201.3.0
 */
public class BLangClientDeclarationStatement extends BLangStatement implements ClientDeclarationStatementNode {

    public BLangClientDeclaration clientDeclaration;

    @Override
    public void setClientDeclaration(ClientDeclarationNode xmlnsDecl) {
        this.clientDeclaration = (BLangClientDeclaration) xmlnsDecl;
    }

    @Override
    public BLangClientDeclaration getClientDeclaration() {
        return clientDeclaration;
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
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CLIENT_DECL;
    }
    
    @Override
    public String toString() {
        return clientDeclaration.toString();
    }
}
