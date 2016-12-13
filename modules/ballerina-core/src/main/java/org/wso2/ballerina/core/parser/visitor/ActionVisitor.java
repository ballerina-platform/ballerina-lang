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
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.utils.BValueFactory;

/**
 * Visitor for actions
 */
public class ActionVisitor extends BallerinaBaseVisitor {

    private SymbolTable actionSymbolTable;

    public ActionVisitor(SymbolTable parentSymbolTable) {
        this.actionSymbolTable = new SymbolTable(parentSymbolTable);
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
    public Object visitActionDefinition(BallerinaParser.ActionDefinitionContext ctx) {
        SymbolName actionName = new SymbolName(ctx.Identifier(0).getText());
        Action actionObject = new Action(actionName, null, null, null, null, null, null, null);

        // Read the annotations
        AnnotationVisitor annotationVisitor = new AnnotationVisitor();
        for (BallerinaParser.AnnotationContext annotationContext : ctx.annotation()) {
            actionObject.addAnnotation((Annotation) annotationContext.accept(annotationVisitor));
        }

        // Read the input parameters
        ParameterVisitor parameterVisitor = new ParameterVisitor();
        BallerinaParser.ParameterListContext parameterListContext = ctx.parameterList();
        if (parameterListContext != null) { //parameter list is optional
            for (BallerinaParser.ParameterContext praCtx : parameterListContext.parameter()) {
                actionObject.addArgument((Parameter) praCtx.accept(parameterVisitor));
            }
        }

        // Read the return types
//        List<Type> typeList = new ArrayList<>();
//        TypeNameVisitor typeNameVisitor = new TypeNameVisitor();
//        BallerinaParser.ReturnTypeListContext returnTypeListContext = ctx.returnTypeList();
//        if (returnTypeListContext != null) { //return type list is optional
//            for (BallerinaParser.TypeNameContext typeNameContext : returnTypeListContext.typeNameList().typeName()) {
//                typeList.add((Type) typeNameContext.accept(typeNameVisitor));
//            }
//        }
//
//        Type[] types = typeList.toArray(new Type[typeList.size()]);

        // Read the variable declarations
        VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor(actionSymbolTable);
        for (BallerinaParser.VariableDeclarationContext variableDeclarationContext :
                ctx.functionBody().variableDeclaration()) {
            VariableDcl variableDcl = (VariableDcl) variableDeclarationContext.accept(variableDeclarationVisitor);
            actionObject.addVariable(variableDcl);
            actionSymbolTable.put(variableDcl.getSymbolName(),
                    BValueFactory.createBValueFromVariableDeclaration(variableDcl));
        }

        // Read the statements
        StatementVisitor statementVisitor = new StatementVisitor(actionSymbolTable);
        for (int i = 0; i < ctx.functionBody().statement().size(); i++) {
            actionObject.addStatement((Statement) (ctx.functionBody().
                    statement(i).getChild(0).accept(statementVisitor)));
        }
        return actionObject;
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return super.getSymbolTable();
    }
}
