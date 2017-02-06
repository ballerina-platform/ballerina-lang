/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.parser.antlr4;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaListener;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParser.AnnotationContext;

import java.util.List;

/**
 * Build the Ballerina language model using the listener events from antlr4 parser.
 *
 * @see BLangModelBuilder
 * @since 0.8.0
 */
public class BLangAntlr4Listener implements BallerinaListener {

    private BLangModelBuilder modelBuilder;
    private int childPosition;
    private static final String PUBLIC = "public";

    public BLangAntlr4Listener(BLangModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
        this.childPosition = 0;
    }

    @Override
    public void enterCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
    }

    @Override
    public void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
    }

    @Override
    public void enterPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
    }

    @Override
    public void exitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createPackageDcl();
        }
    }

    @Override
    public void enterImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
    }

    @Override
    public void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
        if (ctx.exception == null) {
            String pkgName = (ctx.Identifier() != null) ? ctx.Identifier().getText() : null;
            modelBuilder.addImportPackage(pkgName, getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitGroup();
        }
    }

    @Override
    public void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception == null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                Position serviceLocation = new Position(fileName, lineNo);

                modelBuilder.createService(identifier.getText(), serviceLocation, childPosition);
                childPosition++;
            }
        }
    }

    @Override
    public void enterServiceBody(BallerinaParser.ServiceBodyContext ctx) {
    }

    @Override
    public void exitServiceBody(BallerinaParser.ServiceBodyContext ctx) {
    }

    @Override
    public void enterServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx) {
    }

    @Override
    public void exitServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx) {
    }

    @Override
    public void enterResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnit();
        }
    }

    @Override
    public void exitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) {
        if (ctx.exception == null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                Position resourceLocation = new Position(fileName, lineNo);

                modelBuilder.createResource(identifier.getText(), resourceLocation);
            }
        }
    }

    @Override
    public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnit();
        }
    }

    @Override
    public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (ctx.exception == null) {
            boolean isPublic = false;
            List<AnnotationContext> annotations = ctx.annotation();
            if (annotations != null) {
                ParseTree child = ctx.getChild(annotations.size());
                if (child != null) {
                    String tokenStr = child.getText();
                    if (PUBLIC.equals(tokenStr)) {
                        isPublic = true;
                    }

                    // Set the location info needed to generate the stack trace
                    TerminalNode identifier = ctx.Identifier(0);
                    if (identifier != null) {
                        String fileName = identifier.getSymbol().getInputStream().getSourceName();
                        int lineNo = identifier.getSymbol().getLine();
                        Position functionLocation = new Position(fileName, lineNo);

                        modelBuilder.createFunction(identifier.getText(), isPublic, functionLocation, childPosition);
                        childPosition++;
                    }
                }
            }
        }
    }

    @Override
    public void enterFunctionBody(BallerinaParser.FunctionBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitBody();
        }
    }

    @Override
    public void exitFunctionBody(BallerinaParser.FunctionBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endCallableUnitBody();
        }
    }

    @Override
    public void enterConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitGroup();
        }
    }

    @Override
    public void exitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        if (ctx.exception == null) {
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                Position connectorLocation = new Position(fileName, lineNo);

                modelBuilder.createConnector(identifier.getText(), connectorLocation, childPosition);
                childPosition++;
            }
        }
    }

    @Override
    public void enterConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.registerConnectorType(ctx.getParent().getChild(1).getText());
        }
    }

    @Override
    public void exitConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
    }

    @Override
    public void enterActionDefinition(BallerinaParser.ActionDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnit();
        }
    }

    @Override
    public void exitActionDefinition(BallerinaParser.ActionDefinitionContext ctx) {
        if (ctx.exception == null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier(0);
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                Position actionLocation = new Position(fileName, lineNo);

                modelBuilder.createAction(identifier.getText(), actionLocation);
            }
        }
    }

    @Override
    public void enterConnectorDeclaration(BallerinaParser.ConnectorDeclarationContext ctx) {
    }

    @Override
    public void exitConnectorDeclaration(BallerinaParser.ConnectorDeclarationContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createConnectorDcl(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startStruct();
        }
    }

    @Override
    public void exitStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception == null) {
            boolean isPublic = false;
            // TODO: get the child in the (anotation.length) instead of 0, once the annotation support is added.
            String tokenStr = ctx.getChild(0).getText();
            if (PUBLIC.equals(tokenStr)) {
                isPublic = true;
            }
            modelBuilder.createStructDefinition(ctx.Identifier().getText(), isPublic, getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterStructDefinitionBody(BallerinaParser.StructDefinitionBodyContext ctx) {
    }

    @Override
    public void exitStructDefinitionBody(BallerinaParser.StructDefinitionBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        
        List<TerminalNode> fieldList = ctx.Identifier();
        // Each field is added separately rather than sending the whole list at once, coz
        // in future, fields will have annotations, and there will be a new event for each field.
        for (TerminalNode node : fieldList) {
            modelBuilder.createStructField(node.getText(), getCurrentLocation(node));
        }
    }

    @Override
    public void enterTypeConvertorDefinition(BallerinaParser.TypeConvertorDefinitionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnit();
        }
    }

    @Override
    public void exitTypeConvertorDefinition(BallerinaParser.TypeConvertorDefinitionContext ctx) {
        if (ctx.exception == null) {
            // Create the return type of the type convertor
            modelBuilder.createReturnTypes(getCurrentLocation(ctx));
            boolean isPublic = true;
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                Position functionLocation = new Position(fileName, lineNo);
                String typeConverterName = "_" + ctx.typeConvertorInput().typeConvertorType().getText() + "->" + "_" +
                        ctx.typeConvertorType().getText();
                modelBuilder.createTypeConverter(typeConverterName, isPublic, functionLocation, childPosition);
                childPosition++;
            }
        }
    }

    /**
     * Enter a parse tree produced by {@link BallerinaParser#typeConvertorInput}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterTypeConvertorInput(BallerinaParser.TypeConvertorInputContext ctx) {
    }

    /**
     * Exit a parse tree produced by {@link BallerinaParser#typeConvertorInput}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitTypeConvertorInput(BallerinaParser.TypeConvertorInputContext ctx) {
        if (ctx.exception == null) {
            // Create the input parameter for type convertor
            modelBuilder.createParam(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterTypeConvertorBody(BallerinaParser.TypeConvertorBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitBody();
        }
    }

    @Override
    public void exitTypeConvertorBody(BallerinaParser.TypeConvertorBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endCallableUnitBody();
        }
    }

    @Override
    public void enterConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx) {
    }

    @Override
    public void exitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        createBasicLiteral(ctx.literalValue());
        if (ctx.Identifier() != null) {
            modelBuilder.createConstant(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterVariableDeclaration(BallerinaParser.VariableDeclarationContext ctx) {
    }

    @Override
    public void exitVariableDeclaration(BallerinaParser.VariableDeclarationContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            modelBuilder.createVariableDcl(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }


    @Override
    public void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
    }

    @Override
    public void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
    }

    @Override
    public void enterReturnParameters(BallerinaParser.ReturnParametersContext ctx) {

    }

    @Override
    public void exitReturnParameters(BallerinaParser.ReturnParametersContext ctx) {

    }

    @Override
    public void enterNamedParameterList(BallerinaParser.NamedParameterListContext ctx) {

    }

    @Override
    public void exitNamedParameterList(BallerinaParser.NamedParameterListContext ctx) {

    }

    @Override
    public void enterNamedParameter(BallerinaParser.NamedParameterContext ctx) {

    }

    @Override
    public void exitNamedParameter(BallerinaParser.NamedParameterContext ctx) {
        // Value of the ctx.exception is not null, if there are any parser level issues.
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createNamedReturnParams(ctx.Identifier().getText(), getCurrentLocation(ctx));
    }

    @Override
    public void enterReturnTypeList(BallerinaParser.ReturnTypeListContext ctx) {

    }

    @Override
    public void exitReturnTypeList(BallerinaParser.ReturnTypeListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createReturnTypes(getCurrentLocation(ctx));
    }

    @Override
    public void enterQualifiedTypeName(BallerinaParser.QualifiedTypeNameContext ctx) {
    }

    @Override
    public void exitQualifiedTypeName(BallerinaParser.QualifiedTypeNameContext ctx) {
    }

    @Override
    public void enterTypeConvertorType(BallerinaParser.TypeConvertorTypeContext ctx) {
    }

    @Override
    public void exitTypeConvertorType(BallerinaParser.TypeConvertorTypeContext ctx) {

    }

    @Override
    public void enterUnqualifiedTypeName(BallerinaParser.UnqualifiedTypeNameContext ctx) {
    }

    @Override
    public void exitUnqualifiedTypeName(BallerinaParser.UnqualifiedTypeNameContext ctx) {
    }

    @Override
    public void enterSimpleType(BallerinaParser.SimpleTypeContext ctx) {
    }

    @Override
    public void exitSimpleType(BallerinaParser.SimpleTypeContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createType(ctx.getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx) {
    }

    @Override
    public void exitSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            modelBuilder.createArrayType(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterSimpleTypeIterate(BallerinaParser.SimpleTypeIterateContext ctx) {
    }

    @Override
    public void exitSimpleTypeIterate(BallerinaParser.SimpleTypeIterateContext ctx) {
    }

    @Override
    public void enterWithFullSchemaType(BallerinaParser.WithFullSchemaTypeContext ctx) {
    }

    @Override
    public void exitWithFullSchemaType(BallerinaParser.WithFullSchemaTypeContext ctx) {
    }

    @Override
    public void enterWithFullSchemaTypeArray(BallerinaParser.WithFullSchemaTypeArrayContext ctx) {
    }

    @Override
    public void exitWithFullSchemaTypeArray(BallerinaParser.WithFullSchemaTypeArrayContext ctx) {
    }

    @Override
    public void enterWithFullSchemaTypeIterate(BallerinaParser.WithFullSchemaTypeIterateContext ctx) {
    }

    @Override
    public void exitWithFullSchemaTypeIterate(BallerinaParser.WithFullSchemaTypeIterateContext ctx) {
    }

    @Override
    public void enterWithScheamURLType(BallerinaParser.WithScheamURLTypeContext ctx) {
    }

    @Override
    public void exitWithScheamURLType(BallerinaParser.WithScheamURLTypeContext ctx) {
    }

    @Override
    public void enterWithSchemaURLTypeArray(BallerinaParser.WithSchemaURLTypeArrayContext ctx) {
    }

    @Override
    public void exitWithSchemaURLTypeArray(BallerinaParser.WithSchemaURLTypeArrayContext ctx) {
    }

    @Override
    public void enterWithSchemaURLTypeIterate(BallerinaParser.WithSchemaURLTypeIterateContext ctx) {
    }

    @Override
    public void exitWithSchemaURLTypeIterate(BallerinaParser.WithSchemaURLTypeIterateContext ctx) {
    }

    @Override
    public void enterWithSchemaIdType(BallerinaParser.WithSchemaIdTypeContext ctx) {
    }

    @Override
    public void exitWithSchemaIdType(BallerinaParser.WithSchemaIdTypeContext ctx) {
    }

    @Override
    public void enterWithScheamIdTypeArray(BallerinaParser.WithScheamIdTypeArrayContext ctx) {
    }

    @Override
    public void exitWithScheamIdTypeArray(BallerinaParser.WithScheamIdTypeArrayContext ctx) {
    }

    @Override
    public void enterWithScheamIdTypeIterate(BallerinaParser.WithScheamIdTypeIterateContext ctx) {
    }

    @Override
    public void exitWithScheamIdTypeIterate(BallerinaParser.WithScheamIdTypeIterateContext ctx) {
    }

    @Override
    public void enterTypeName(BallerinaParser.TypeNameContext ctx) {
    }

    @Override
    public void exitTypeName(BallerinaParser.TypeNameContext ctx) {
    }

    @Override
    public void enterQualifiedReference(BallerinaParser.QualifiedReferenceContext ctx) {
    }

    @Override
    public void exitQualifiedReference(BallerinaParser.QualifiedReferenceContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            modelBuilder.createSymbolName(ctx.Identifier().getText());
        }
    }

    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) {
        modelBuilder.startParamList();
    }

    @Override
    public void exitParameterList(BallerinaParser.ParameterListContext ctx) {
        modelBuilder.endParamList();
    }

    @Override
    public void enterParameter(BallerinaParser.ParameterContext ctx) {
    }

    @Override
    public void exitParameter(BallerinaParser.ParameterContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            modelBuilder.createParam(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterPackageName(BallerinaParser.PackageNameContext ctx) {
    }

    @Override
    public void exitPackageName(BallerinaParser.PackageNameContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createPackageName(ctx.getText());
        }
    }

    @Override
    public void enterLiteralValue(BallerinaParser.LiteralValueContext ctx) {
    }

    @Override
    public void exitLiteralValue(BallerinaParser.LiteralValueContext ctx) {
//        createBasicLiteral(ctx);
    }

    @Override
    public void enterAnnotation(BallerinaParser.AnnotationContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startAnnotation();
        }
    }

    @Override
    public void exitAnnotation(BallerinaParser.AnnotationContext ctx) {
        if (ctx.exception == null && ctx.annotationName() != null) {
            boolean valueAvailable = false;
            String annotationName = ctx.annotationName().getText();

            int childCount = ctx.getChildCount();
            if (childCount > 2) {
                valueAvailable = true;
            }
            modelBuilder.endAnnotation(annotationName, valueAvailable, getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterAnnotationName(BallerinaParser.AnnotationNameContext ctx) {
    }

    @Override
    public void exitAnnotationName(BallerinaParser.AnnotationNameContext ctx) {
    }

    @Override
    public void enterElementValuePairs(BallerinaParser.ElementValuePairsContext ctx) {
    }

    @Override
    public void exitElementValuePairs(BallerinaParser.ElementValuePairsContext ctx) {
    }

    @Override
    public void enterElementValuePair(BallerinaParser.ElementValuePairContext ctx) {
    }

    @Override
    public void exitElementValuePair(BallerinaParser.ElementValuePairContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            String key = ctx.Identifier().getText();
            modelBuilder.createAnnotationKeyValue(key);
        }
    }

    @Override
    public void enterElementValue(BallerinaParser.ElementValueContext ctx) {
    }

    @Override
    public void exitElementValue(BallerinaParser.ElementValueContext ctx) {
    }

    @Override
    public void enterElementValueArrayInitializer(BallerinaParser.ElementValueArrayInitializerContext ctx) {
    }

    @Override
    public void exitElementValueArrayInitializer(BallerinaParser.ElementValueArrayInitializerContext ctx) {
    }

    @Override
    public void enterStatement(BallerinaParser.StatementContext ctx) {
    }

    @Override
    public void exitStatement(BallerinaParser.StatementContext ctx) {
    }

    @Override
    public void enterAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
    }

    @Override
    public void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createAssignmentStmt(getCurrentLocation(ctx));
    }

    @Override
    public void enterVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startVarRefList();
    }

    @Override
    public void exitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        int noOfArguments = getNoOfArgumentsInList(ctx);
        modelBuilder.endVarRefList(noOfArguments);
    }

    @Override
    public void enterIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startIfElseStmt();
        }
    }

    @Override
    public void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endIfElseStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startElseIfClause();
        }
    }

    @Override
    public void exitElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endElseIfClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startElseClause();
        }
    }

    @Override
    public void exitElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endElseClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterIterateStatement(BallerinaParser.IterateStatementContext ctx) {
    }

    @Override
    public void exitIterateStatement(BallerinaParser.IterateStatementContext ctx) {
    }

    @Override
    public void enterWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startWhileStmt();
        }
    }

    @Override
    public void exitWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endWhileStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterBreakStatement(BallerinaParser.BreakStatementContext ctx) {
    }

    @Override
    public void exitBreakStatement(BallerinaParser.BreakStatementContext ctx) {
    }

    @Override
    public void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
    }

    @Override
    public void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
    }

    @Override
    public void enterJoinClause(BallerinaParser.JoinClauseContext ctx) {
    }

    @Override
    public void exitJoinClause(BallerinaParser.JoinClauseContext ctx) {
    }

    @Override
    public void enterJoinConditions(BallerinaParser.JoinConditionsContext ctx) {
    }

    @Override
    public void exitJoinConditions(BallerinaParser.JoinConditionsContext ctx) {
    }

    @Override
    public void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
    }

    @Override
    public void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
    }

    @Override
    public void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
    }

    @Override
    public void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
    }

    @Override
    public void enterCatchClause(BallerinaParser.CatchClauseContext ctx) {
    }

    @Override
    public void exitCatchClause(BallerinaParser.CatchClauseContext ctx) {
    }

    @Override
    public void enterThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
    }

    @Override
    public void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
    }

    @Override
    public void enterReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
    }

    @Override
    public void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createReturnStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterReplyStatement(BallerinaParser.ReplyStatementContext ctx) {
    }

    @Override
    public void exitReplyStatement(BallerinaParser.ReplyStatementContext ctx) {
        // Here the expression is only a message reference
        //modelBuilder.createVarRefExpr();
        if (ctx.exception == null) {
            modelBuilder.createReplyStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) {
    }

    @Override
    public void exitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) {
    }

    @Override
    public void enterTriggerWorker(BallerinaParser.TriggerWorkerContext ctx) {
    }

    @Override
    public void exitTriggerWorker(BallerinaParser.TriggerWorkerContext ctx) {
    }

    @Override
    public void enterWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
    }

    @Override
    public void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
    }

    @Override
    public void enterCommentStatement(BallerinaParser.CommentStatementContext ctx) {
    }

    @Override
    public void exitCommentStatement(BallerinaParser.CommentStatementContext ctx) {
    }

    @Override
    public void enterActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
    }

    @Override
    public void exitActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createActionInvocationStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx) {
    }

    @Override
    public void exitSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx) {
        if (ctx.exception == null) {
            String varName = ctx.getText();
            modelBuilder.createVarRefExpr(varName, getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx) {
    }

    @Override
    public void exitMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            String mapArrayVarName = ctx.Identifier().getText();
            modelBuilder.createMapArrayVarRefExpr(mapArrayVarName, getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterStructFieldIdentifier(BallerinaParser.StructFieldIdentifierContext ctx) {
    }

    @Override
    public void exitStructFieldIdentifier(BallerinaParser.StructFieldIdentifierContext ctx) {
        if (ctx.exception != null || ctx.getChild(0) == null) {
            return;
        }
        modelBuilder.createStructFieldRefExpr(getCurrentLocation(ctx));
    }

    @Override
    public void enterArgumentList(BallerinaParser.ArgumentListContext ctx) {
    }

    @Override
    public void exitArgumentList(BallerinaParser.ArgumentListContext ctx) {
        if (ctx.exception == null && ctx.expressionList() == null) { //handles empty argument functions
            modelBuilder.startExprList();
        }
    }

    @Override
    public void enterExpressionList(BallerinaParser.ExpressionListContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startExprList();
        }
    }

    @Override
    public void exitExpressionList(BallerinaParser.ExpressionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        int noOfArguments = getNoOfArgumentsInList(ctx);
        modelBuilder.endExprList(noOfArguments);
    }

    @Override
    public void enterFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx) {
    }

    @Override
    public void exitFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createFunctionInvocationStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterFunctionName(BallerinaParser.FunctionNameContext ctx) {
    }

    @Override
    public void exitFunctionName(BallerinaParser.FunctionNameContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createSymbolName(ctx.Identifier().getText());
        }
    }

    @Override
    public void enterActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
    }

    @Override
    public void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
        // Corresponding production
        // actionInvocation
        //      :   packageName ':' Identifier '.' Identifier
        // Here first identifier is the connector name and the second identifier in the action name.
        if (ctx.exception == null) {
            modelBuilder.createSymbolName(ctx.Identifier(0).getText(), ctx.Identifier(1).getText());
        }
    }

    @Override
    public void enterBacktickString(BallerinaParser.BacktickStringContext ctx) {
    }

    @Override
    public void exitBacktickString(BallerinaParser.BacktickStringContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createBacktickExpr(ctx.BacktickStringLiteral().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
    }

    @Override
    public void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }

    }

    @Override
    public void enterBinaryGTExpression(BallerinaParser.BinaryGTExpressionContext ctx) {
    }

    @Override
    public void exitBinaryGTExpression(BallerinaParser.BinaryGTExpressionContext ctx) {
        createBinaryExpr(ctx);
    }

    @Override
    public void enterTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
    }

    @Override
    public void exitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
    }

    @Override
    public void enterBinaryLEExpression(BallerinaParser.BinaryLEExpressionContext ctx) {
    }

    @Override
    public void exitBinaryLEExpression(BallerinaParser.BinaryLEExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
    }

    @Override
    public void exitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createFunctionInvocationExpr(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterBinaryGEExpression(BallerinaParser.BinaryGEExpressionContext ctx) {
    }

    @Override
    public void exitBinaryGEExpression(BallerinaParser.BinaryGEExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx) {
    }

    @Override
    public void exitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBracedExpression(BallerinaParser.BracedExpressionContext ctx) {
    }

    @Override
    public void exitBracedExpression(BallerinaParser.BracedExpressionContext ctx) {
    }

    @Override
    public void enterVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) {
    }

    @Override
    public void exitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) {
//        modelBuilder.createVarRefExpr();
    }

    @Override
    public void enterActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx) {
    }

    @Override
    public void exitActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createActionInvocationExpr(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
    }

    @Override
    public void exitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createTypeCastExpr(ctx.typeName().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterStructInitializeExpression(BallerinaParser.StructInitializeExpressionContext ctx) {
    }

    @Override
    public void exitStructInitializeExpression(BallerinaParser.StructInitializeExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        boolean exprListAvailable = ctx.expressionList() != null;
        modelBuilder.createInstanceCreaterExpr(ctx.Identifier().getText(), exprListAvailable, getCurrentLocation(ctx));
    }

    @Override
    public void enterBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx) {
    }

    @Override
    public void exitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBinaryNotEqualExpression(BallerinaParser.BinaryNotEqualExpressionContext ctx) {
    }

    @Override
    public void exitBinaryNotEqualExpression(BallerinaParser.BinaryNotEqualExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterArrayInitializerExpression(BallerinaParser.ArrayInitializerExpressionContext ctx) {

    }

    @Override
    public void exitArrayInitializerExpression(BallerinaParser.ArrayInitializerExpressionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createArrayInitExpr(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterBinaryDivisionExpression(BallerinaParser.BinaryDivisionExpressionContext ctx) {
    }

    @Override
    public void exitBinaryDivisionExpression(BallerinaParser.BinaryDivisionExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBinaryModExpression(BallerinaParser.BinaryModExpressionContext ctx) {
    }

    @Override
    public void exitBinaryModExpression(BallerinaParser.BinaryModExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBinarySubExpression(BallerinaParser.BinarySubExpressionContext ctx) {
    }

    @Override
    public void exitBinarySubExpression(BallerinaParser.BinarySubExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBinaryMultiplicationExpression(BallerinaParser.BinaryMultiplicationExpressionContext ctx) {
    }

    @Override
    public void exitBinaryMultiplicationExpression(BallerinaParser.BinaryMultiplicationExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterLiteralExpression(BallerinaParser.LiteralExpressionContext ctx) {
    }

    @Override
    public void exitLiteralExpression(BallerinaParser.LiteralExpressionContext ctx) {
        if (ctx.exception == null) {
            createBasicLiteral(ctx.literalValue());
        }
    }

    @Override
    public void enterUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
    }

    @Override
    public void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
        if (ctx.exception == null) {
            String op = ctx.getChild(0).getText();
            modelBuilder.createUnaryExpr(op, getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterBinaryLTExpression(BallerinaParser.BinaryLTExpressionContext ctx) {
    }

    @Override
    public void exitBinaryLTExpression(BallerinaParser.BinaryLTExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterMapInitializerExpression(BallerinaParser.MapInitializerExpressionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startMapInitKeyValue();
        }
    }

    @Override
    public void exitMapInitializerExpression(BallerinaParser.MapInitializerExpressionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createMapInitExpr(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx) {
    }

    @Override
    public void exitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBinaryAddExpression(BallerinaParser.BinaryAddExpressionContext ctx) {
    }

    @Override
    public void exitBinaryAddExpression(BallerinaParser.BinaryAddExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    /**
     * Enter a parse tree produced by {@link BallerinaParser#mapInitKeyValueList}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterMapInitKeyValueList(BallerinaParser.MapInitKeyValueListContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startMapInitKeyValue();
        }
    }

    /**
     * Exit a parse tree produced by {@link BallerinaParser#mapInitKeyValueList}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitMapInitKeyValueList(BallerinaParser.MapInitKeyValueListContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endMapInitKeyValue(ctx.mapInitKeyValue().size());
        }
    }

    @Override
    public void enterMapInitKeyValue(BallerinaParser.MapInitKeyValueContext ctx) {
    }

    @Override
    public void exitMapInitKeyValue(BallerinaParser.MapInitKeyValueContext ctx) {
        if (ctx.exception == null) {
            // Remove the double quotes
            String key = ctx.QuotedStringLiteral().toString().substring(1,
                    ctx.QuotedStringLiteral().toString().length() - 1);
            modelBuilder.createMapInitKeyValue(key, getCurrentLocation(ctx));
        }
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
    }

    private void createBinaryExpr(ParserRuleContext ctx) {
        if (ctx.exception == null && ctx.getChild(1) != null) {
            String opStr = ctx.getChild(1).getText();
            modelBuilder.createBinaryExpr(opStr, getCurrentLocation(ctx));
        }
    }

    private void createBasicLiteral(BallerinaParser.LiteralValueContext ctx) {
        if (ctx.exception == null) {
            TerminalNode terminalNode = ctx.IntegerLiteral();
            if (terminalNode != null) {
                if (terminalNode.getText().endsWith("l") || terminalNode.getText().endsWith("L")) {
                    //dropping the last character L
                    String longValue = terminalNode.getText().substring(0, terminalNode.getText().length() - 1);
                    modelBuilder.createLongLiteral(longValue, getCurrentLocation(ctx));
                } else {
                    modelBuilder.createIntegerLiteral(terminalNode.getText(), getCurrentLocation(ctx));
                }
            }

            terminalNode = ctx.FloatingPointLiteral();
            if (terminalNode != null) {
                if (terminalNode.getText().endsWith("d") || terminalNode.getText().endsWith("D")) {
                    //dropping the last character D
                    String doubleValue = terminalNode.getText().substring(0, terminalNode.getText().length() - 1);
                    modelBuilder.createDoubleLiteral(doubleValue, getCurrentLocation(ctx));
                } else {
                    modelBuilder.createFloatLiteral(terminalNode.getText(), getCurrentLocation(ctx));
                }
            }

            terminalNode = ctx.QuotedStringLiteral();
            if (terminalNode != null) {
                String stringLiteral = terminalNode.getText();
                stringLiteral = stringLiteral.substring(1, stringLiteral.length() - 1);
                stringLiteral = StringEscapeUtils.unescapeJava(stringLiteral);
                modelBuilder.createStringLiteral(stringLiteral, getCurrentLocation(ctx));
            }

            terminalNode = ctx.BooleanLiteral();
            if (terminalNode != null) {
                modelBuilder.createBooleanLiteral(terminalNode.getText(), getCurrentLocation(ctx));
            }

            terminalNode = ctx.NullLiteral();
            if (terminalNode != null) {
                modelBuilder.createNullLiteral(terminalNode.getText(), getCurrentLocation(ctx));
            }
        }
    }

    private Position getCurrentLocation(ParserRuleContext ctx) {
        String fileName = ctx.getStart().getInputStream().getSourceName();
        int lineNo = ctx.getStart().getLine();
        return new Position(fileName, lineNo);
    }
    
    private Position getCurrentLocation(TerminalNode node) {
        String fileName = node.getSymbol().getInputStream().getSourceName();
        int lineNo = node.getSymbol().getLine();
        return new Position(fileName, lineNo);
    }

    private int getNoOfArgumentsInList(ParserRuleContext ctx) {
        // Here is the production for the argument list
        // argumentList
        //    :   '(' expressionList ')'
        //    ;
        //
        // expressionList
        //    :   expression (',' expression)*
        //    ;

        // Now we need to calculate the number of arguments in a function or an action.
        // We can do the by getting the children of expressionList from the ctx
        // The following count includes the token for the ","  as well.
        int childCountExprList = ctx.getChildCount();

        // Therefore we need to subtract the number of ","
        // e.g. (a, b)          => childCount = 3, noOfArguments = 2;
        //      (a, b, c)       => childCount = 5, noOfArguments = 3;
        //      (a, b, c, d)    => childCount = 7, noOfArguments = 4;
        // Here childCount is always an odd number.
        // noOfArguments = childCount mod 2 + 1
        return childCountExprList / 2 + 1;
    }
}
