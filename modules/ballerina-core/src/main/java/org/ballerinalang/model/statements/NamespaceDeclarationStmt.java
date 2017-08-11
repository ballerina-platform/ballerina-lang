/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.statements;

import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;

/**
 * {@link NamespaceDeclarationStmt} represents a {@link NamespaceDeclaration} statement in ballerina.
 *
 * @since 0.89
 */
public class NamespaceDeclarationStmt extends AbstractStatement {

    NamespaceDeclaration namespaceDclr;

    public NamespaceDeclarationStmt(NodeLocation location, NamespaceDeclaration namespaceDclr) {
        super(location);
        this.namespaceDclr = namespaceDclr;
    }

    /**
     * Get the namespace declaration associated with this statement.
     *
     * @return namespace declaration associated with this statement
     */
    public NamespaceDeclaration getNamespaceDclr() {
        return namespaceDclr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public StatementKind getKind() {
        return StatementKind.NAMESPACE_DECLARATION;
    }
}
