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
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for function
 */
public class FunctionVisitor extends BallerinaBaseVisitor {

    private SymbolTable functionSymbolTable;

    public FunctionVisitor() {
        this.functionSymbolTable = new SymbolTable(null);
    }

    public FunctionVisitor(SymbolTable parentSymbolTable) {
        this.functionSymbolTable = new SymbolTable(parentSymbolTable);
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
    public Object visitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {

        Identifier functionName = new Identifier(ctx.Identifier(0).getText());
        boolean isPublicFunction = !ctx.getTokens(31)
                .isEmpty(); //since function body cannot have public keyword inside.

        List<Annotation> annotationList = new ArrayList<>();
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        for (BallerinaParser.AnnotationContext annotationContext : ctx.annotation()) {
            annotationList.add((Annotation) annotationContext.accept(annotationVisitor));
        }

        Annotation[] annotations = annotationList.toArray(new Annotation[annotationList.size()]);

        List<Parameter> parameterList = new ArrayList<>();
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        BallerinaParser.ParameterListContext parameterListContext = ctx.parameterList();
        if (parameterListContext != null) { //parameter list is optional
            for (BallerinaParser.ParameterContext praCtx : parameterListContext.parameter()) {
                parameterList.add((Parameter) praCtx.accept(parameterVisitor));
            }
        }

        Parameter[] parameters = parameterList.toArray(new Parameter[parameterList.size()]);

        List<Type> typeList = new ArrayList<>();
        TypeNameVisitor typeNameVisitor = new TypeNameVisitor();
        BallerinaParser.ReturnTypeListContext returnTypeListContext = ctx.returnTypeList();
        if (returnTypeListContext != null) { //return type list is optional
            for (BallerinaParser.TypeNameContext typeNameContext : returnTypeListContext.typeNameList().typeName()) {
                typeList.add((Type) typeNameContext.accept(typeNameVisitor));
            }
        }

        Type[] types = typeList.toArray(new Type[typeList.size()]);

        List<VariableDcl> variableDclList = new ArrayList<>();
        VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor();
        for (BallerinaParser.VariableDeclarationContext variableDeclarationContext :
                ctx.functionBody().variableDeclaration()) {
            VariableDcl variableDcl = (VariableDcl) variableDeclarationContext.accept(variableDeclarationVisitor);
            variableDclList.add(variableDcl);
            functionSymbolTable.put(variableDcl.getIdentifier(),
                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
        }

        VariableDcl[] variableDcls = variableDclList.toArray(new VariableDcl[variableDclList.size()]);

        Statement[] statementArray = new Statement[ctx.functionBody().statement().size()];
        StatementVisitor statementVisitor = new StatementVisitor();
        for (int i = 0; i < ctx.functionBody().statement().size(); i++) {
            statementArray[i] = (Statement) (ctx.functionBody().statement(i).getChild(0).accept(statementVisitor));
        }
        Function functionObject = new BallerinaFunction(
                functionName,
                isPublicFunction,
                annotations,
                parameters,
                types,
                null,
                variableDcls,
                null,
                new BlockStmt(statementArray));
        return functionObject;
    }

    public SymbolTable getFunctionSymbolTable() {
        return functionSymbolTable;
    }
}
