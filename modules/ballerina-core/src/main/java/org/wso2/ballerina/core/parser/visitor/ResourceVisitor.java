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
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

/**
 * Visitor for resource
 */
public class ResourceVisitor extends BallerinaBaseVisitor {

    private SymbolTable resourceSymbolTable;

    public ResourceVisitor(SymbolTable parentSymbolTable) {
        this.resourceSymbolTable = new SymbolTable(parentSymbolTable);
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
    public Object visitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) {
        Resource resourceObject = new Resource(ctx.Identifier().getText());

        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        for (BallerinaParser.AnnotationContext annotationContext : ctx.annotation()) {
            resourceObject.addAnnotation((Annotation) annotationContext.accept(annotationVisitor));
        }

        VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor(resourceSymbolTable);
        for (BallerinaParser.VariableDeclarationContext variableDeclarationContext :
                ctx.functionBody().variableDeclaration()) {
            VariableDcl variableDcl = (VariableDcl) variableDeclarationContext.accept(variableDeclarationVisitor);
            resourceObject.addVariable(variableDcl);
            resourceSymbolTable.put(variableDcl.getIdentifier(),
                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
        }

        StatementVisitor statementVisitor = new StatementVisitor(resourceSymbolTable);
        for (int i = 0; i < ctx.functionBody().statement().size(); i++) {
            resourceObject.addStatement((Statement) (ctx.functionBody().statement(i).
                    accept(statementVisitor)));
        }

        return resourceObject;
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.resourceSymbolTable;
    }
}
