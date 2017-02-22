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
package org.ballerinalang.util.parser.antlr4;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.util.parser.BallerinaListener;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationContext;

import java.nio.file.Path;
import java.util.List;

/**
 * Build the Ballerina language model using the listener events from antlr4 parser.
 *
 * @see BLangModelBuilder
 * @since 0.8.0
 */
public class BLangAntlr4Listener implements BallerinaListener {
    protected String fileName;
    protected String packageDirPath;
    protected static final String PUBLIC = "public";
    protected BLangModelBuilder modelBuilder;
    protected String currentPkgName;

    // Types related attributes
    protected String typeName;
    // private String schemaID;

    protected boolean isSimpleType;
    // private boolean isSchemaIDType;
    // private boolean isFullSchemaType;
    // private boolean isSchemaURLType;

    protected boolean isArrayType;

    // Variable to keep whether worker creation has been started. This is used at BLangAntlr4Listener class
    // to create parameter when there is a named parameter
    protected boolean isWorkerStarted = false;

    public BLangAntlr4Listener(BLangModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public BLangAntlr4Listener(BLangModelBuilder modelBuilder, Path sourceFilePath) {
        this.modelBuilder = modelBuilder;
        this.fileName = sourceFilePath.getFileName().toString();

        if (sourceFilePath.getNameCount() >= 2) {
            this.packageDirPath = sourceFilePath.subpath(0, sourceFilePath.getNameCount() - 1).toString();
        } else {
            this.packageDirPath = null;
        }
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
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addPackageDcl(ctx.packageName().getText());
    }

    @Override
    public void enterImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
    }

    @Override
    public void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String pkgPath = ctx.packageName().getText();
        String asPkgName = (ctx.Identifier() != null) ? ctx.Identifier().getText() : null;
        modelBuilder.addImportPackage(getCurrentLocation(ctx), pkgPath, asPkgName);
    }

    @Override
    public void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startServiceDef(getCurrentLocation(ctx));
    }

    @Override
    public void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception == null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                NodeLocation serviceLocation = new NodeLocation(fileName, lineNo);

                modelBuilder.createService(serviceLocation, identifier.getText());
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
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startResourceDef();
    }

    @Override
    public void exitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) {
        if (ctx.exception == null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                NodeLocation resourceLocation = new NodeLocation(fileName, lineNo);

                modelBuilder.addResource(resourceLocation, identifier.getText());
            }
        }
    }

    @Override
    public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {

    }

    @Override
    public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {

    }

    @Override
    public void enterNativeFunction(BallerinaParser.NativeFunctionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startFunctionDef();
    }

    @Override
    public void exitNativeFunction(BallerinaParser.NativeFunctionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        
        List<AnnotationContext> annotations = ctx.annotation();
        if (annotations != null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier(0);
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                NodeLocation functionLocation = new NodeLocation(fileName, lineNo);

                modelBuilder.addFunction(functionLocation, identifier.getText(), true, true);
            }
        }
    }

    @Override
    public void enterFunction(BallerinaParser.FunctionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startFunctionDef();
        }
    }

    @Override
    public void exitFunction(BallerinaParser.FunctionContext ctx) {
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
                        NodeLocation functionLocation = new NodeLocation(fileName, lineNo);

                        modelBuilder.addFunction(functionLocation, identifier.getText(), isPublic, false);
                    }
                }
            }
        }
    }

    @Override
    public void enterFunctionBody(BallerinaParser.FunctionBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitBody(getCurrentLocation(ctx));
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

    }

    @Override
    public void exitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {

    }

    @Override
    public void enterNativeConnector(BallerinaParser.NativeConnectorContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startConnectorDef(getCurrentLocation(ctx));
    }

    @Override
    public void exitNativeConnector(BallerinaParser.NativeConnectorContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        TerminalNode identifier = ctx.Identifier();
        if (identifier != null) {
            String fileName = identifier.getSymbol().getInputStream().getSourceName();
            int lineNo = identifier.getSymbol().getLine();
            NodeLocation connectorLocation = new NodeLocation(fileName, lineNo);

            modelBuilder.createConnector(connectorLocation, identifier.getText(), true);
        }
    }

    @Override
    public void enterNativeConnectorBody(BallerinaParser.NativeConnectorBodyContext ctx) {

    }

    @Override
    public void exitNativeConnectorBody(BallerinaParser.NativeConnectorBodyContext ctx) {

    }

    @Override
    public void enterConnector(BallerinaParser.ConnectorContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startConnectorDef(getCurrentLocation(ctx));
    }

    @Override
    public void exitConnector(BallerinaParser.ConnectorContext ctx) {
        if (ctx.exception == null) {
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                NodeLocation connectorLocation = new NodeLocation(fileName, lineNo);

                modelBuilder.createConnector(connectorLocation, identifier.getText(), false);
            }
        }
    }

    @Override
    public void enterConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
//        if (ctx.exception == null) {
//            modelBuilder.registerConnectorType(ctx.getParent().getChild(1).getText());
//        }
    }

    @Override
    public void exitConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
    }

    @Override
    public void enterNativeAction(BallerinaParser.NativeActionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startActionDef();
    }

    @Override
    public void exitNativeAction(BallerinaParser.NativeActionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        // Set the location info needed to generate the stack trace
        TerminalNode identifier = ctx.Identifier(0);
        if (identifier != null) {
            String fileName = identifier.getSymbol().getInputStream().getSourceName();
            int lineNo = identifier.getSymbol().getLine();
            NodeLocation actionLocation = new NodeLocation(fileName, lineNo);

            modelBuilder.addAction(actionLocation, identifier.getText(), true);
        }
    }

    @Override
    public void enterAction(BallerinaParser.ActionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startActionDef();
        }
    }

    @Override
    public void exitAction(BallerinaParser.ActionContext ctx) {
        if (ctx.exception == null) {
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier(0);
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                NodeLocation actionLocation = new NodeLocation(fileName, lineNo);

                modelBuilder.addAction(actionLocation, identifier.getText(), false);
            }
        }
    }

    @Override
    public void enterStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startStructDef(getCurrentLocation(ctx));
    }

    @Override
    public void exitStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addStructDef(getCurrentLocation(ctx), ctx.Identifier().getText());
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
            modelBuilder.addStructField(getCurrentLocation(node), node.getText());
        }
    }

    @Override
    public void enterTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx) {

    }

    @Override
    public void exitTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx) {

    }

    @Override
    public void enterNativeTypeMapper(BallerinaParser.NativeTypeMapperContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startTypeMapperDef();
    }

    @Override
    public void exitNativeTypeMapper(BallerinaParser.NativeTypeMapperContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        // Create the return type of the type convertor
        modelBuilder.createReturnTypes(getCurrentLocation(ctx));
        boolean isPublic = true;
        // Set the location info needed to generate the stack trace
        TerminalNode identifier = ctx.Identifier();
        if (identifier != null) {
            String fileName = identifier.getSymbol().getInputStream().getSourceName();
            int lineNo = identifier.getSymbol().getLine();
            NodeLocation typeconvertorLocation = new NodeLocation(fileName, lineNo);
            modelBuilder.addTypeMapper(ctx.typeMapperInput().typeMapperType().getText(),
                ctx.typeMapperType().getText(), identifier.getText(), typeconvertorLocation, isPublic, true);
        }
    }

    @Override
    public void enterTypeMapper(BallerinaParser.TypeMapperContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startTypeMapperDef();
        }
    }

    @Override
    public void exitTypeMapper(BallerinaParser.TypeMapperContext ctx) {
        if (ctx.exception == null) {
            // Create the return type of the type mapper
            modelBuilder.createReturnTypes(getCurrentLocation(ctx));
            boolean isPublic = true;
            // Set the location info needed to generate the stack trace
            TerminalNode identifier = ctx.Identifier();
            if (identifier != null) {
                String fileName = identifier.getSymbol().getInputStream().getSourceName();
                int lineNo = identifier.getSymbol().getLine();
                NodeLocation typeconvertorLocation = new NodeLocation(fileName, lineNo);
                modelBuilder.addTypeMapper(ctx.typeMapperInput().typeMapperType().getText(),
                    ctx.typeMapperType().getText(), identifier.getText(), typeconvertorLocation, isPublic, false);
            }
        }
    }

    /**
     * Enter a parse tree produced by {@link BallerinaParser#typeMapperInput}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterTypeMapperInput(BallerinaParser.TypeMapperInputContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startTypeMapperInput();
        }
    }

    /**
     * Exit a parse tree produced by {@link BallerinaParser#typeMapperInput}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitTypeMapperInput(BallerinaParser.TypeMapperInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addParam(ctx.Identifier().getText(), getCurrentLocation(ctx));
        modelBuilder.endTypeMapperInput();
    }

    @Override
    public void enterTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitBody(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx) {
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

        boolean isPublic = false;
        String tokenStr = ctx.getChild(0).getText();
        if (PUBLIC.equals(tokenStr)) {
            isPublic = true;
        }

        createBasicLiteral(ctx.literalValue());
        modelBuilder.addConstantDef(getCurrentLocation(ctx), ctx.Identifier().getText(), isPublic);
    }

    @Override
    public void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (ctx.exception == null) {
            isWorkerStarted = true;
            modelBuilder.startWorkerUnit();
            modelBuilder.startCallableUnitBody(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            //modelBuilder.createSymbolName(ctx.Identifier().getText());
            modelBuilder.endCallableUnitBody();
            modelBuilder.createWorker(ctx.Identifier().getText(), getCurrentLocation(ctx));
            isWorkerStarted = false;
        }

    }

//    @Override
//    public void enterWorkerInputParameter(BallerinaParser.WorkerInputParameterContext ctx) {
//
//    }
//
//    @Override
//    public void exitWorkerInputParameter(BallerinaParser.WorkerInputParameterContext ctx) {
//        if (ctx.exception == null) {
//            modelBuilder.createParam(ctx.Identifier().getText(), getCurrentLocation(ctx));
//        }
//    }

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
        // If worker is started, then this is an input parameter definition
        if (isWorkerStarted) {
            modelBuilder.addParam(ctx.Identifier().getText(), getCurrentLocation(ctx));
        } else {
            modelBuilder.createNamedReturnParam(getCurrentLocation(ctx), ctx.Identifier().getText());
        }
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
        if (ctx.exception != null) {
            return;
        }

        currentPkgName = ctx.packageName().getText();
    }

    @Override
    public void enterTypeMapperType(BallerinaParser.TypeMapperTypeContext ctx) {
    }

    @Override
    public void exitTypeMapperType(BallerinaParser.TypeMapperTypeContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (isSimpleType) {
            modelBuilder.addSimpleTypeName(getCurrentLocation(ctx), typeName, currentPkgName, isArrayType);
            typeName = null;
            currentPkgName = null;
            isArrayType = false;
            isSimpleType = false;
        }

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
        if (ctx.exception != null) {
            return;
        }

        typeName = ctx.getText();
        isSimpleType = true;
    }

    @Override
    public void enterSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx) {
    }

    @Override
    public void exitSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        typeName = ctx.Identifier().getText();
        isSimpleType = true;
        isArrayType = true;
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
        if (ctx.exception != null) {
            return;
        }

        if (isSimpleType) {
            modelBuilder.addSimpleTypeName(getCurrentLocation(ctx), typeName, currentPkgName, isArrayType);
            typeName = null;
            currentPkgName = null;
            isArrayType = false;
            isSimpleType = false;
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
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addParam(ctx.Identifier().getText(), getCurrentLocation(ctx));
    }

    @Override
    public void enterPackageName(BallerinaParser.PackageNameContext ctx) {
    }

    @Override
    public void exitPackageName(BallerinaParser.PackageNameContext ctx) {
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
    public void enterVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {

    }

    @Override
    public void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String varName = ctx.Identifier().getText();
        boolean exprAvailable = ctx.expression() != null;
        modelBuilder.addVariableDefinitionStmt(getCurrentLocation(ctx), varName, exprAvailable);
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
            modelBuilder.startIfElseStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.addIfElseStmt();
        }
    }

    @Override
    public void enterIfClause(BallerinaParser.IfClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startIfClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitIfClause(BallerinaParser.IfClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.addIfClause();
        }
    }

    @Override
    public void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startElseIfClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addElseIfClause();
    }

    @Override
    public void enterElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startElseClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.addElseClause();
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
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startWhileStmt(getCurrentLocation(ctx));
    }

    @Override
    public void exitWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createWhileStmt(getCurrentLocation(ctx));
    }

    @Override
    public void enterBreakStatement(BallerinaParser.BreakStatementContext ctx) {
    }

    @Override
    public void exitBreakStatement(BallerinaParser.BreakStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createBreakStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startForkJoinStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endForkJoinStmt();
        }
    }

    @Override
    public void enterJoinClause(BallerinaParser.JoinClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startJoinClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitJoinClause(BallerinaParser.JoinClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endJoinClause(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    public void enterAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) {

    }

    @Override
    public void exitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createAnyJoinCondition("any", ctx.IntegerLiteral().getText(), getCurrentLocation(ctx));
            for (TerminalNode t : ctx.Identifier()) {
                modelBuilder.createJoinWorkers(t.getText());
            }
        }
   }

    @Override
    public void enterAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) {

    }

    @Override
    public void exitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createAllJoinCondition("all");
            for (TerminalNode t : ctx.Identifier()) {
                modelBuilder.createJoinWorkers(t.getText());
            }
        }

    }

    @Override
    public void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startTimeoutClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endTimeoutClause(ctx.Identifier().getText(), getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startTryCatchStmt(getCurrentLocation(ctx));
    }

    @Override
    public void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.addTryCatchStmt();
    }

    @Override
    public void enterCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startCatchClause(getCurrentLocation(ctx));
    }

    @Override
    public void exitCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String key = ctx.Identifier().getText();
        modelBuilder.addCatchClause(getCurrentLocation(ctx), key);
    }

    @Override
    public void enterThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
    }

    @Override
    public void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.createThrowStmt(getCurrentLocation(ctx));
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
        if (ctx.exception == null) {
            modelBuilder.createWorkerInvocationStmt(ctx.Identifier(0).getText(), ctx.Identifier(1).getText(),
                    getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
    }

    @Override
    public void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createWorkerReplyStmt(ctx.Identifier(0).getText(), ctx.Identifier(1).getText(),
                    getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterCommentStatement(BallerinaParser.CommentStatementContext ctx) {
    }

    @Override
    public void exitCommentStatement(BallerinaParser.CommentStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.addCommentStmt(getCurrentLocation(ctx), ctx.getText());
        }
    }

    @Override
    public void enterActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
    }

    @Override
    public void exitActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createActionInvocationStmt(getCurrentLocation(ctx), ctx.actionInvocation().Identifier().getText());
    }

    @Override
    public void enterSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx) {
    }

    @Override
    public void exitSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx) {
        if (ctx.exception == null) {
            String varName = ctx.getText();
            modelBuilder.createVarRefExpr(getCurrentLocation(ctx), varName);
        }
    }

    @Override
    public void enterMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx) {
    }

    @Override
    public void exitMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx) {
        if (ctx.exception == null && ctx.Identifier() != null) {
            String mapArrayVarName = ctx.Identifier().getText();
            modelBuilder.createMapArrayVarRefExpr(getCurrentLocation(ctx), mapArrayVarName);
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
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createFunctionInvocationStmt(getCurrentLocation(ctx));
    }

    @Override
    public void enterFunctionName(BallerinaParser.FunctionNameContext ctx) {
    }

    @Override
    public void exitFunctionName(BallerinaParser.FunctionNameContext ctx) {

    }

    @Override
    public void enterActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
    }

    @Override
    public void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx) {

    }

    @Override
    public void enterCallableUnitName(BallerinaParser.CallableUnitNameContext ctx) {

    }

    @Override
    public void exitCallableUnitName(BallerinaParser.CallableUnitNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String pkgName = (ctx.packageName() != null) ? ctx.packageName().getText() : null;
        modelBuilder.addCallableUnitName(pkgName, ctx.Identifier().getText());
    }

    @Override
    public void enterBacktickString(BallerinaParser.BacktickStringContext ctx) {
    }

    @Override
    public void exitBacktickString(BallerinaParser.BacktickStringContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.createBacktickExpr(getCurrentLocation(ctx), ctx.BacktickStringLiteral().getText());
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
    public void enterTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
    }

    @Override
    public void exitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
    }

    @Override
    public void enterFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
    }

    @Override
    public void exitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addFunctionInvocationExpr(getCurrentLocation(ctx));
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
    public void enterRefTypeInitExpression(BallerinaParser.RefTypeInitExpressionContext ctx) {
    }

    @Override
    public void exitRefTypeInitExpression(BallerinaParser.RefTypeInitExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createRefTypeInitExpr(getCurrentLocation(ctx));
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
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.addActionInvocationExpr(getCurrentLocation(ctx), ctx.actionInvocation().Identifier().getText());
    }

    @Override
    public void enterTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
    }

    @Override
    public void exitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createTypeCastExpr(getCurrentLocation(ctx));
    }

    @Override
    public void enterArrayInitExpression(BallerinaParser.ArrayInitExpressionContext ctx) {
    }

    @Override
    public void exitArrayInitExpression(BallerinaParser.ArrayInitExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = false;
        if (ctx.expressionList() != null) {
            argsAvailable = true;
        }

        modelBuilder.createArrayInitExpr(getCurrentLocation(ctx), argsAvailable);
    }

    @Override
    public void enterBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {

    }

    @Override
    public void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
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
    public void enterBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx) {

    }

    @Override
    public void exitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx) {
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
            modelBuilder.createUnaryExpr(getCurrentLocation(ctx), op);
        }
    }

    @Override
    public void enterConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx) {

    }

    @Override
    public void exitConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createConnectorInitExpr(getCurrentLocation(ctx));
    }

    @Override
    public void enterBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx) {

    }

    @Override
    public void exitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
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
    public void enterMapStructInitKeyValueList(BallerinaParser.MapStructInitKeyValueListContext ctx) {

    }

    @Override
    public void exitMapStructInitKeyValueList(BallerinaParser.MapStructInitKeyValueListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.endMapStructInitKeyValueList(ctx.mapStructInitKeyValue().size());
    }

    @Override
    public void enterMapStructInitKeyValue(BallerinaParser.MapStructInitKeyValueContext ctx) {

    }

    @Override
    public void exitMapStructInitKeyValue(BallerinaParser.MapStructInitKeyValueContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.createMapStructInitKeyValue(getCurrentLocation(ctx));
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

    protected void createBinaryExpr(ParserRuleContext ctx) {
        if (ctx.exception == null && ctx.getChild(1) != null) {
            String opStr = ctx.getChild(1).getText();
            modelBuilder.createBinaryExpr(getCurrentLocation(ctx), opStr);
        }
    }

    protected void createBasicLiteral(BallerinaParser.LiteralValueContext ctx) {
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

    protected NodeLocation getCurrentLocation(ParserRuleContext ctx) {
        String fileName = ctx.getStart().getInputStream().getSourceName();
        int lineNo = ctx.getStart().getLine();
        return new NodeLocation(fileName, lineNo);
    }

    protected NodeLocation getCurrentLocation(TerminalNode node) {
        String fileName = node.getSymbol().getInputStream().getSourceName();
        int lineNo = node.getSymbol().getLine();
        return new NodeLocation(fileName, lineNo);
    }

    protected int getNoOfArgumentsInList(ParserRuleContext ctx) {
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
