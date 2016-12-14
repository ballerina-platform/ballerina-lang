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
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Import;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

/**
 * Visitor for compilation unit
 */
public class CompilationUnitVisitor extends BallerinaBaseVisitor {

    BallerinaFile balFile = new BallerinaFile();
    private SymbolTable baseSymbolTable = new SymbolTable(null);


    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
        // Read the package declaration
        Package packageName = (Package) this.visitPackageDeclaration(ctx.packageDeclaration());
        balFile.setPackageName(packageName.getFullQualifiedName());

        // Read the constants
        ConstantVisitor constantVisitor = new ConstantVisitor(baseSymbolTable);
        for (BallerinaParser.ConstantDefinitionContext cdc : ctx.constantDefinition()) {
            VariableDcl constantObject = (VariableDcl) cdc.accept(constantVisitor);
//            balFile.addConstant(constantObject);
            baseSymbolTable.put(constantObject.getSymbolName(),
                    BValueFactory.createBValueFromVariableDeclaration(constantObject));
        }

        for (BallerinaParser.ImportDeclarationContext idc : ctx.importDeclaration()) {
            Import importObject = (Import) this.visitImportDeclaration(idc);
            balFile.addImport(importObject);
        }

        // Read the services
        ServiceVisitor serviceVisitor = new ServiceVisitor(baseSymbolTable);
        for (BallerinaParser.ServiceDefinitionContext sdc : ctx.serviceDefinition()) {
            Service serviceObject = (Service) sdc.accept(serviceVisitor);
            balFile.addService(serviceObject);
        }

        // Read the functions
        FunctionVisitor functionVisitor = new FunctionVisitor(baseSymbolTable);
        for (BallerinaParser.FunctionDefinitionContext fdc : ctx.functionDefinition()) {
            Function functionObject = (Function) fdc.accept(functionVisitor);
            balFile.addFunction(functionObject);
        }

        // Read the connectors
        ConnectorVisitor connectorVisitor = new ConnectorVisitor(baseSymbolTable);
        for (BallerinaParser.ConnectorDefinitionContext cdc : ctx.connectorDefinition()) {
            Connector connectorObject = (Connector) cdc.accept(connectorVisitor);
//            balFile.addConnector(connectorObject);
        }

        return balFile;
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
    public Object visitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
        String packageName = ctx.packageName().getText();
        return new Package(packageName);
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
    public Object visitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
        String importPackageName = ctx.packageName().getText();
        if (ctx.Identifier() != null) {
            String importAsName = ctx.Identifier().getText();
            return new Import(importAsName, importPackageName);
        } else {
            return new Import(importPackageName);
        }
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
    public Object visitPackageName(BallerinaParser.PackageNameContext ctx) {
        String packageName = ctx.getText();
        return packageName;
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.baseSymbolTable;
    }
}
