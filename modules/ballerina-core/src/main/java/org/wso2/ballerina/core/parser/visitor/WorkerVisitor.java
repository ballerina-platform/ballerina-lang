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
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * visitor for worker
 */
public class WorkerVisitor extends BallerinaBaseVisitor {

    private SymbolTable workerSymbolTable;

    public WorkerVisitor(SymbolTable parentSymbolTable) {
        this.workerSymbolTable = new SymbolTable(parentSymbolTable);
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
    public Object visitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        List<VariableDcl> variableDclList = new ArrayList<>();
        VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor(workerSymbolTable);
        for (BallerinaParser.VariableDeclarationContext varDclCtx : ctx.variableDeclaration()) {
            VariableDcl variableDcl = (VariableDcl) varDclCtx.accept(variableDeclarationVisitor);
            variableDclList.add(variableDcl);
            workerSymbolTable.put(variableDcl.getSymbolName(),
                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
        }

        List<Statement> statementList = new ArrayList<>();
        StatementVisitor statementVisitor = new StatementVisitor(workerSymbolTable);
        for (BallerinaParser.StatementContext statementContext : ctx.statement()) {
            //todo check getChild(0)
            statementList.add((Statement) statementContext.accept(statementVisitor));
        }

        return new Worker(variableDclList, statementList);
    }
    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return workerSymbolTable;
    }
}
