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
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

/**
 * Visitor for connector
 */
public class ConnectorVisitor extends BallerinaBaseVisitor {

    private SymbolTable connectorSymbolTable;

    public ConnectorVisitor(SymbolTable parentSymbolTable) {
        this.connectorSymbolTable = new SymbolTable(parentSymbolTable);
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
    public Object visitConnectorDeclaration(BallerinaParser.ConnectorDeclarationContext ctx) {
        ConnectorDcl connection = new ConnectorDcl(ctx.qualifiedReference().get(0).Identifier().getText(),
                new SymbolName(ctx.Identifier().getText()));
        // TODO: 12/11/16 : Need to read the argument list and set to the connection object
//        if(ctx.expressionList() != null) {
//            for(BallerinaParser.ExpressionContext ec : ctx.expressionList().expression()) {
//
//            }
//        }
        return connection;
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
    public Object visitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        SymbolName connectorName = new SymbolName(ctx.Identifier().getText());
        Connector connectorObject = new Connector(connectorName, null, null, null, null);

        // Read the annotations
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        for (BallerinaParser.AnnotationContext annotationContext : ctx.annotation()) {
            connectorObject.addAnnotation((Annotation) annotationContext.accept(annotationVisitor));
        }

        // Read the input parameters
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        BallerinaParser.ParameterListContext parameterListContext = ctx.parameterList();
        if (parameterListContext != null) { //parameter list is optional
            for (BallerinaParser.ParameterContext praCtx : parameterListContext.parameter()) {
                connectorObject.addArgument((Parameter) praCtx.accept(parameterVisitor));
            }
        }

        // Read the connector declarations
        for (BallerinaParser.ConnectorDeclarationContext cdc : ctx.connectorBody().connectorDeclaration()) {
            connectorObject.addConnection((ConnectorDcl) this.visitConnectorDeclaration(cdc));
        }

        // Read the variable declarations
        VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor(connectorSymbolTable);
        for (BallerinaParser.VariableDeclarationContext variableDeclarationContext :
                ctx.connectorBody().variableDeclaration()) {
            VariableDcl variableDcl = (VariableDcl) variableDeclarationContext.accept(variableDeclarationVisitor);
            connectorObject.addVariable(variableDcl);
            connectorSymbolTable.put(variableDcl.getName(),
                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
        }

        // Read the action definitions
        ActionVisitor actionVisitor = new ActionVisitor(connectorSymbolTable);
        for (BallerinaParser.ActionDefinitionContext adc : ctx.connectorBody().actionDefinition()) {
            connectorObject.addAction((Action) adc.accept(actionVisitor));
        }

        return connectorObject;
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
    public Object visitConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
        return super.visitConnectorBody(ctx);
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.connectorSymbolTable;
    }
}
