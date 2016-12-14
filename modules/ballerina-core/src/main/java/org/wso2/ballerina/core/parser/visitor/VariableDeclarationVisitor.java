/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.parser.visitor;

import org.wso2.ballerina.core.interpreter.SymbolTable;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;

/**
 * Visitor for variable declaration
 */
public class VariableDeclarationVisitor extends BallerinaBaseVisitor {
    private SymbolTable variableDeclarationSymbolTable;

    public VariableDeclarationVisitor(SymbolTable parentSymbolTable) {
        this.variableDeclarationSymbolTable = new SymbolTable(parentSymbolTable);
    }
    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitVariableDeclaration(BallerinaParser.VariableDeclarationContext ctx) {
        TypeNameVisitor typeNameVisitor = new TypeNameVisitor();
        return new VariableDcl((Type) ctx.typeName().accept(typeNameVisitor),
                new SymbolName(ctx.Identifier().getText()), null);
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.variableDeclarationSymbolTable;
    }
}
