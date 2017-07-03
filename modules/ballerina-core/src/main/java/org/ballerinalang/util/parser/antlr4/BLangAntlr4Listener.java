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

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.util.parser.BallerinaListener;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.BallerinaParser.ActionDefinitionContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationAttachmentContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationAttributeArrayContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationAttributeContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationAttributeListContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationAttributeValueContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationBodyContext;
import org.ballerinalang.util.parser.BallerinaParser.AnnotationDefinitionContext;
import org.ballerinalang.util.parser.BallerinaParser.ArrayLiteralContext;
import org.ballerinalang.util.parser.BallerinaParser.ArrayLiteralExpressionContext;
import org.ballerinalang.util.parser.BallerinaParser.AttachmentPointContext;
import org.ballerinalang.util.parser.BallerinaParser.BuiltInReferenceTypeNameContext;
import org.ballerinalang.util.parser.BallerinaParser.BuiltInReferenceTypeTypeExpressionContext;
import org.ballerinalang.util.parser.BallerinaParser.CallableUnitBodyContext;
import org.ballerinalang.util.parser.BallerinaParser.CallableUnitSignatureContext;
import org.ballerinalang.util.parser.BallerinaParser.ContinueStatementContext;
import org.ballerinalang.util.parser.BallerinaParser.DefinitionContext;
import org.ballerinalang.util.parser.BallerinaParser.FieldDefinitionContext;
import org.ballerinalang.util.parser.BallerinaParser.MapStructKeyValueContext;
import org.ballerinalang.util.parser.BallerinaParser.MapStructLiteralContext;
import org.ballerinalang.util.parser.BallerinaParser.MapStructLiteralExpressionContext;
import org.ballerinalang.util.parser.BallerinaParser.NameReferenceContext;
import org.ballerinalang.util.parser.BallerinaParser.ReferenceTypeNameContext;
import org.ballerinalang.util.parser.BallerinaParser.SimpleLiteralContext;
import org.ballerinalang.util.parser.BallerinaParser.SimpleLiteralExpressionContext;
import org.ballerinalang.util.parser.BallerinaParser.StructBodyContext;
import org.ballerinalang.util.parser.BallerinaParser.TypeConversionExpressionContext;
import org.ballerinalang.util.parser.BallerinaParser.TypeMapperSignatureContext;
import org.ballerinalang.util.parser.BallerinaParser.ValueTypeNameContext;
import org.ballerinalang.util.parser.BallerinaParser.ValueTypeTypeExpressionContext;
import org.ballerinalang.util.parser.BallerinaParser.XmlLocalNameContext;
import org.ballerinalang.util.parser.BallerinaParser.XmlNamespaceNameContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Build the Ballerina language model using the listener events from antlr4 parser.
 *
 * @see BLangModelBuilder
 * @since 0.8.0
 */
public class BLangAntlr4Listener implements BallerinaListener {
    protected static final String B_KEYWORD_PUBLIC = "public";
    protected static final String B_KEYWORD_NATIVE = "native";
    protected static final String ATTACHMENT_POINTS = "attachmentPoints";
    protected static final String B_KEYWORD_ACTION = "action";
    public static final String NAME_REF = "nameRef";
    public static final String TYPE_NAME = "typeName";

    protected String fileName;
    protected String packageDirPath;
    protected String currentPkgName;
    protected BLangModelBuilder modelBuilder;

    // Types related attributes
    protected String typeName;
    // private String schemaID;

    protected boolean processingReturnParams = false;
    protected Stack<SimpleTypeName> typeNameStack = new Stack<>();
    protected Stack<BLangModelBuilder.NameReference> nameReferenceStack = new Stack<>();

    // Variable to keep whether worker creation has been started. This is used at BLangAntlr4Listener class
    // to create parameter when there is a named parameter
    protected boolean isWorkerStarted = false;
    protected boolean isTypeMapperStarted = false;

    // token stream is required for listener to access hidden whiteSpace
    // such as whitespace/newlines while building model for composer use
    private CommonTokenStream tokenStream;

    // flag to indicate whether additional information
    // from source needs to be captured, eg: whitespace
    private boolean isVerboseMode = false;

    public BLangAntlr4Listener(BLangModelBuilder modelBuilder, Path sourceFilePath) {
        this.modelBuilder = modelBuilder;
        this.fileName = sourceFilePath.getFileName().toString();

        if (sourceFilePath.getNameCount() >= 2) {
            this.packageDirPath = sourceFilePath.subpath(0, sourceFilePath.getNameCount() - 1).toString();
        } else {
            this.packageDirPath = null;
        }
    }

    public BLangAntlr4Listener(boolean isVerboseMode, CommonTokenStream tokenStream,
                               BLangModelBuilder modelBuilder, Path sourceFilePath) {
        this(modelBuilder, sourceFilePath);
        this.isVerboseMode = isVerboseMode;
        this.tokenStream = tokenStream;
    }

    @Override
    public void enterCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
    }

    @Override
    public void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
        if (isVerboseMode) {
            // getting whitespace from file start to first token
            String whiteSpace = WhiteSpaceUtil.getFileStartingWhiteSpace(tokenStream);
            modelBuilder.addBFileWhiteSpaceRegion(WhiteSpaceRegions.BFILE_START, whiteSpace);
        }
    }

    @Override
    public void enterPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
    }

    @Override
    public void exitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        if (isVerboseMode) {
            // getting whitespace related to package declaration node
            WhiteSpaceDescriptor wsDescriptor = WhiteSpaceUtil.getPackageDeclarationWS(tokenStream, ctx);
            if (wsDescriptor != null) {
                // file start whitespace & package declaration related whitespace is stored in BFile
                wsDescriptor.getWhiteSpaceRegions().forEach(modelBuilder::addBFileWhiteSpaceRegion);
            }
        }
        modelBuilder.addPackageDcl(getCurrentLocation(ctx), ctx.packageName().getText());
    }

    @Override
    public void enterPackageName(BallerinaParser.PackageNameContext ctx) {
    }

    @Override
    public void exitPackageName(BallerinaParser.PackageNameContext ctx) {
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
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getImportDeclarationWS(tokenStream, ctx);
        }
        modelBuilder.addImportPackage(getCurrentLocation(ctx), whiteSpaceDescriptor, pkgPath, asPkgName);
    }

    @Override
    public void enterDefinition(DefinitionContext ctx) {

    }

    @Override
    public void exitDefinition(DefinitionContext ctx) {

    }

    @Override
    public void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startServiceDef();
    }

    @Override
    public void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String protocolPkgName = ctx.Identifier(0).getText();
        String serviceName = ctx.Identifier(1).getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getServiceDefinitionWS(tokenStream, ctx);
        }
        modelBuilder.createService(getCurrentLocation(ctx), whiteSpaceDescriptor, serviceName, protocolPkgName);
    }

    @Override
    public void enterServiceBody(BallerinaParser.ServiceBodyContext ctx) {

    }

    @Override
    public void exitServiceBody(BallerinaParser.ServiceBodyContext ctx) {
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
        if (ctx.exception != null) {
            return;
        }

        String resourceName = ctx.Identifier().getText();
        int annotationCount = ctx.annotationAttachment() != null ? ctx.annotationAttachment().size() : 0;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getResourceDefinitionWS(tokenStream, ctx);
        }
        modelBuilder.addResource(getCurrentLocation(ctx), whiteSpaceDescriptor, resourceName, annotationCount);

    }

    @Override
    public void enterCallableUnitBody(CallableUnitBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startCallableUnitBody();
    }

    @Override
    public void exitCallableUnitBody(CallableUnitBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.endCallableUnitBody(getCurrentLocation(ctx));
    }

    @Override
    public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startFunctionDef();
    }

    @Override
    public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isNative = B_KEYWORD_NATIVE.equals(ctx.getChild(0).getText());
        String functionName = ctx.callableUnitSignature().Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getFunctionDefWS(tokenStream, ctx);
        }
        modelBuilder.addFunction(getCurrentLocation(ctx), whiteSpaceDescriptor, functionName, isNative);
    }

    @Override
    public void enterCallableUnitSignature(CallableUnitSignatureContext ctx) {

    }

    @Override
    public void exitCallableUnitSignature(CallableUnitSignatureContext ctx) {

    }

    @Override
    public void enterConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startConnectorDef();
    }

    @Override
    public void exitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String connectorName = ctx.Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getConnectorDefWS(tokenStream, ctx);
        }
        modelBuilder.createConnector(getCurrentLocation(ctx), whiteSpaceDescriptor, connectorName);
    }

    @Override
    public void enterConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
    }

    @Override
    public void exitConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
    }

    @Override
    public void enterActionDefinition(ActionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startActionDef();
    }

    @Override
    public void exitActionDefinition(ActionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isNative = false;
        for (int position = 1; position < ctx.getChildCount(); position++) {
            if (ctx.getChild(position).getText().equals(B_KEYWORD_ACTION)
                    && ctx.getChild(position - 1).getText().equals(B_KEYWORD_NATIVE)) {
                isNative = true;
                break;
            }
        }
        String actionName = ctx.callableUnitSignature().Identifier().getText();
        int annotationCount = ctx.annotationAttachment() != null ? ctx.annotationAttachment().size() : 0;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getActionDefWS(tokenStream, ctx);
        }
        modelBuilder.addAction(getCurrentLocation(ctx), whiteSpaceDescriptor, actionName, isNative, annotationCount);
    }

    @Override
    public void enterStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startStructDef();
    }

    @Override
    public void exitStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String structName = ctx.Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getStructDefWS(tokenStream, ctx);
        }
        modelBuilder.addStructDef(getCurrentLocation(ctx), whiteSpaceDescriptor, structName);
    }

    @Override
    public void enterStructBody(StructBodyContext ctx) {

    }

    @Override
    public void exitStructBody(StructBodyContext ctx) {

    }

    @Override
    public void enterAnnotationDefinition(AnnotationDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = new WhiteSpaceDescriptor();
            whiteSpaceDescriptor.addChildDescriptor(ATTACHMENT_POINTS, new WhiteSpaceDescriptor());
        }
        modelBuilder.startAnnotationDef(whiteSpaceDescriptor);
    }

    @Override
    public void exitAnnotationDefinition(AnnotationDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAnnotationDefWS(tokenStream, ctx);
        }
        modelBuilder.addAnnotationtDef(getCurrentLocation(ctx), whiteSpaceDescriptor, ctx.Identifier().getText());
    }

    @Override
    public void enterGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx) {

    }

    @Override
    public void exitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        SimpleTypeName typeName = typeNameStack.pop();
        String varName = ctx.Identifier().getText();
        boolean exprAvailable = ctx.expression() != null;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getGlobalVariableDefWS(tokenStream, ctx);
        }
        modelBuilder.addGlobalVarDef(getCurrentLocation(ctx), whiteSpaceDescriptor, typeName, varName, exprAvailable);
    }

    @Override
    public void enterAttachmentPoint(AttachmentPointContext ctx) {

    }

    @Override
    public void exitAttachmentPoint(AttachmentPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAttachmentPointWS(tokenStream, ctx);
        }
        modelBuilder.addAnnotationtAttachmentPoint(getCurrentLocation(ctx), whiteSpaceDescriptor, ctx.getText());
    }

    @Override
    public void enterAnnotationBody(AnnotationBodyContext ctx) {

    }

    @Override
    public void exitAnnotationBody(AnnotationBodyContext ctx) {

    }

    @Override
    public void enterTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startTypeMapperDef();
    }

    @Override
    public void exitTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isNative = B_KEYWORD_NATIVE.equals(ctx.getChild(0).getText());
        String typeMapperName = ctx.typeMapperSignature().Identifier().getText();
        SimpleTypeName returnTypeName = typeNameStack.pop();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getTypeMapperDef(tokenStream, ctx);
        }
        modelBuilder.addTypeMapper(getCurrentLocation(ctx), whiteSpaceDescriptor,
                typeMapperName, returnTypeName, isNative);
    }

    @Override
    public void enterTypeMapperSignature(TypeMapperSignatureContext ctx) {

    }

    @Override
    public void exitTypeMapperSignature(TypeMapperSignatureContext ctx) {

    }

    @Override
    public void enterTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCallableUnitBody();
        }
    }

    @Override
    public void exitTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.endCallableUnitBody(getCurrentLocation(ctx));
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

        SimpleTypeName typeName = typeNameStack.pop();
        String constName = ctx.Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getConstantDefWS(tokenStream, ctx);
        }

        modelBuilder.addConstantDef(getCurrentLocation(ctx), whiteSpaceDescriptor, typeName, constName);
    }

    @Override
    public void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (ctx.exception == null) {
            isWorkerStarted = true;
            modelBuilder.startWorkerUnit();
            modelBuilder.startCallableUnitBody();
        }
    }

    @Override
    public void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (ctx.exception == null && ctx.workerDefinition().Identifier() != null) {
            modelBuilder.endCallableUnitBody(getCurrentLocation(ctx));

            String workerName = ctx.workerDefinition().Identifier().getText();
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getWorkerDeclarationWS(tokenStream, ctx);
            }
            modelBuilder.createWorker(getCurrentLocation(ctx), whiteSpaceDescriptor, workerName);
            isWorkerStarted = false;
        }
    }

    /**
     * Enter a parse tree produced by {@link BallerinaParser#workerDefinition}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx) {

    }

    /**
     * Exit a parse tree produced by {@link BallerinaParser#workerDefinition}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx) {
        if (ctx.exception == null) {
            String workerName = ctx.Identifier().getText();
            modelBuilder.createWorkerDefinition(getCurrentLocation(ctx), workerName);
        }
    }

    @Override
    public void enterTypeName(BallerinaParser.TypeNameContext ctx) {
    }

    @Override
    public void exitTypeName(BallerinaParser.TypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.typeName() != null) {
            // This is an array type
            SimpleTypeName typeName = typeNameStack.peek();
            typeName.setArrayType((ctx.getChildCount() - 1) / 2);
            return;
        }

        if (ctx.referenceTypeName() != null || ctx.valueTypeName() != null) {
            return;
        }

        // This is 'any' type
        SimpleTypeName typeName = new SimpleTypeName(ctx.getChild(0).getText());
        typeName.setNodeLocation(getCurrentLocation(ctx));
        if (isVerboseMode) {
            WhiteSpaceDescriptor ws = WhiteSpaceUtil.getTypeNameWS(tokenStream, ctx);
            typeName.setWhiteSpaceDescriptor(ws);
        }
        typeNameStack.push(typeName);
    }

    @Override
    public void enterReferenceTypeName(ReferenceTypeNameContext ctx) {

    }

    @Override
    public void exitReferenceTypeName(ReferenceTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.nameReference() != null) {
            BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
            modelBuilder.validateAndSetPackagePath(getCurrentLocation(ctx), nameReference);

            SimpleTypeName typeName = new SimpleTypeName(nameReference.getName(),
                    nameReference.getPackageName(), nameReference.getPackagePath());
            typeName.setNodeLocation(getCurrentLocation(ctx));

            if (isVerboseMode) {
                WhiteSpaceDescriptor ws = WhiteSpaceUtil.getRefTypeNameWS(tokenStream, ctx);
                typeName.setWhiteSpaceDescriptor(ws);
            }

            typeNameStack.push(typeName);
        }
    }

    @Override
    public void enterValueTypeName(ValueTypeNameContext ctx) {

    }

    @Override
    public void exitValueTypeName(ValueTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String valueTypeName = ctx.getChild(0).getText();
        SimpleTypeName simpleTypeName = new SimpleTypeName(valueTypeName);
        simpleTypeName.setNodeLocation(getCurrentLocation(ctx));
        if (isVerboseMode) {
            WhiteSpaceDescriptor ws = WhiteSpaceUtil.getValueTypeNameWS(tokenStream, ctx);
            simpleTypeName.setWhiteSpaceDescriptor(ws);
        }
        typeNameStack.push(simpleTypeName);
    }

    @Override
    public void enterBuiltInReferenceTypeName(BuiltInReferenceTypeNameContext ctx) {

    }

    @Override
    public void exitBuiltInReferenceTypeName(BuiltInReferenceTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String builtInRefTypeName = ctx.getChild(0).getText();
        SimpleTypeName simpleTypeName = new SimpleTypeName(builtInRefTypeName);
        simpleTypeName.setNodeLocation(getCurrentLocation(ctx));
        if (isVerboseMode) {
            WhiteSpaceDescriptor ws = WhiteSpaceUtil.getBuiltInRefTypeNameWS(tokenStream, ctx);
            simpleTypeName.setWhiteSpaceDescriptor(ws);
        }
        typeNameStack.push(simpleTypeName);
    }

    @Override
    public void enterXmlNamespaceName(XmlNamespaceNameContext ctx) {

    }

    @Override
    public void exitXmlNamespaceName(XmlNamespaceNameContext ctx) {

    }

    @Override
    public void enterXmlLocalName(XmlLocalNameContext ctx) {

    }

    @Override
    public void exitXmlLocalName(XmlLocalNameContext ctx) {

    }

    @Override
    public void enterAnnotationAttachment(AnnotationAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startAnnotationAttachment();
    }

    @Override
    public void exitAnnotationAttachment(AnnotationAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        int attribuesAvailable = ctx.annotationAttributeList() == null ? 0 :
                ctx.annotationAttributeList().annotationAttribute().size();

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAnnotationAttachmentWS(tokenStream, ctx);
        }

        NodeLocation currentLocation = getCurrentLocation(ctx);
        BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
        modelBuilder.validateAndSetPackagePath(currentLocation, nameReference);
        modelBuilder.addAnnotationAttachment(currentLocation, whiteSpaceDescriptor,
                nameReference, attribuesAvailable);
    }

    @Override
    public void enterAnnotationAttributeList(AnnotationAttributeListContext ctx) {

    }

    @Override
    public void exitAnnotationAttributeList(AnnotationAttributeListContext ctx) {

    }

    @Override
    public void enterAnnotationAttribute(AnnotationAttributeContext ctx) {

    }

    @Override
    public void exitAnnotationAttribute(AnnotationAttributeContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String key = ctx.Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAnnotationAttributeWS(tokenStream, ctx);
        }
        modelBuilder.createAnnotationKeyValue(getCurrentLocation(ctx), whiteSpaceDescriptor, key);
    }

    @Override
    public void enterAnnotationAttributeValue(AnnotationAttributeValueContext ctx) {

    }

    @Override
    public void exitAnnotationAttributeValue(AnnotationAttributeValueContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        ParseTree childContext = ctx.getChild(0);
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAnnotationAttributeValueWS(tokenStream, ctx);
        }
        if (childContext instanceof SimpleLiteralContext) {
            modelBuilder.createLiteralTypeAttributeValue(getCurrentLocation(ctx), whiteSpaceDescriptor);
        } else if (childContext instanceof AnnotationAttachmentContext) {
            modelBuilder.createAnnotationTypeAttributeValue(getCurrentLocation(ctx), whiteSpaceDescriptor);
        } else if (childContext instanceof AnnotationAttributeArrayContext) {
            modelBuilder.createArrayTypeAttributeValue(getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterAnnotationAttributeArray(AnnotationAttributeArrayContext ctx) {

    }

    @Override
    public void exitAnnotationAttributeArray(AnnotationAttributeArrayContext ctx) {

    }

    @Override
    public void enterStatement(BallerinaParser.StatementContext ctx) {
    }

    @Override
    public void exitStatement(BallerinaParser.StatementContext ctx) {
    }

    @Override
    public void enterTransformStatement(BallerinaParser.TransformStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startTransformStmt();
    }

    @Override
    public void exitTransformStatement(BallerinaParser.TransformStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getTransformStmtWS(tokenStream, ctx);
        }
        modelBuilder.createTransformStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterTransformStatementBody(BallerinaParser.TransformStatementBodyContext ctx) {

    }

    @Override
    public void exitTransformStatementBody(BallerinaParser.TransformStatementBodyContext ctx) {

    }

    @Override
    public void enterExpressionAssignmentStatement(BallerinaParser.ExpressionAssignmentStatementContext ctx) {

    }

    @Override
    public void exitExpressionAssignmentStatement(BallerinaParser.ExpressionAssignmentStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAssignmentStmtWS(tokenStream, ctx);
        }
        modelBuilder.createAssignmentStmt(getCurrentLocation(ctx), whiteSpaceDescriptor, false);
    }

    @Override
    public void enterExpressionVariableDefinitionStatement(
            BallerinaParser.ExpressionVariableDefinitionStatementContext ctx) {

    }

    @Override
    public void exitExpressionVariableDefinitionStatement(
            BallerinaParser.ExpressionVariableDefinitionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        SimpleTypeName typeName = typeNameStack.pop();
        String varName = ctx.Identifier().getText();
        boolean exprAvailable = (ctx.expression() != null);

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getVariableDefWS(tokenStream, ctx, exprAvailable);
        }

        modelBuilder.addVariableDefinitionStmt(getCurrentLocation(ctx), whiteSpaceDescriptor, typeName, varName,
                exprAvailable);

    }

    @Override
    public void enterVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {

    }

    @Override
    public void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        SimpleTypeName typeName = typeNameStack.pop();
        String varName = ctx.Identifier().getText();
        boolean exprAvailable = ctx.expression() != null ||
                ctx.connectorInitExpression() != null ||
                ctx.actionInvocation() != null;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getVariableDefWS(tokenStream, ctx, exprAvailable);
        }
        modelBuilder.addVariableDefinitionStmt(getCurrentLocation(ctx), whiteSpaceDescriptor, typeName, varName,
                exprAvailable);
    }

    @Override
    public void enterMapStructLiteral(MapStructLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        modelBuilder.startMapStructLiteral();


    }

    @Override
    public void exitMapStructLiteral(MapStructLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getMapStructLiteralWS(tokenStream, ctx);
        }
        modelBuilder.createMapStructLiteral(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterMapStructKeyValue(MapStructKeyValueContext ctx) {

    }

    @Override
    public void exitMapStructKeyValue(MapStructKeyValueContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getMapStructKeyValueWS(tokenStream, ctx);
        }
        modelBuilder.addMapStructKeyValue(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterArrayLiteral(ArrayLiteralContext ctx) {

    }

    @Override
    public void exitArrayLiteral(ArrayLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = ctx.expressionList() != null;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getArrayLiteralExpWS(tokenStream, ctx);
        }
        modelBuilder.createArrayInitExpr(getCurrentLocation(ctx), whiteSpaceDescriptor, argsAvailable);
    }

    @Override
    public void enterConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx) {

    }

    @Override
    public void exitConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        NodeLocation currentLocation = getCurrentLocation(ctx);
        boolean argsAvailable = ctx.expressionList() != null;
        BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
        modelBuilder.validateAndSetPackagePath(currentLocation, nameReference);
        SimpleTypeName connectorTypeName = new SimpleTypeName(nameReference.getName(),
                nameReference.getPackageName(), null);

        connectorTypeName.setWhiteSpaceDescriptor(nameReference.getWhiteSpaceDescriptor());
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getConnectorInitExpWS(tokenStream, ctx);
        }

        modelBuilder.createConnectorInitExpr(currentLocation, whiteSpaceDescriptor,
                connectorTypeName, argsAvailable);
    }

    @Override
    public void enterAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
    }

    @Override
    public void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAssignmentStmtWS(tokenStream, ctx);
        }
        boolean isVarDeclaration = false;
        if (ctx.getChild(0).getText().equals("var")) {
            isVarDeclaration = true;
        }
        modelBuilder.createAssignmentStmt(getCurrentLocation(ctx), whiteSpaceDescriptor, isVarDeclaration);
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
            modelBuilder.addIfElseStmt(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterIfClause(BallerinaParser.IfClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startIfClause();
        }
    }

    @Override
    public void exitIfClause(BallerinaParser.IfClauseContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getIfClauseWS(tokenStream, ctx);
            }
            modelBuilder.addIfClause(whiteSpaceDescriptor, getCurrentLocation(ctx));
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
        if (ctx.exception != null) {
            return;
        }

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getElseIfClauseWS(tokenStream, ctx);
        }
        modelBuilder.addElseIfClause(whiteSpaceDescriptor, getCurrentLocation(ctx));
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
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getElseClauseWS(tokenStream, ctx);
            }
            modelBuilder.addElseClause(whiteSpaceDescriptor, getCurrentLocation(ctx));
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

        modelBuilder.startWhileStmt();
    }

    @Override
    public void exitWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getWhileStmtWS(tokenStream, ctx);
        }
        modelBuilder.createWhileStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterContinueStatement(ContinueStatementContext ctx) {

    }

    @Override
    public void exitContinueStatement(ContinueStatementContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getContinueStatementWS(tokenStream, ctx);
            }
            modelBuilder.createContinueStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterBreakStatement(BallerinaParser.BreakStatementContext ctx) {
    }

    @Override
    public void exitBreakStatement(BallerinaParser.BreakStatementContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getBreakStatementWS(tokenStream, ctx);
            }
            modelBuilder.createBreakStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startForkJoinStmt();
        }
    }

    @Override
    public void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getForkJoinStatementWS(tokenStream, ctx);
        }
        modelBuilder.endForkJoinStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterJoinClause(BallerinaParser.JoinClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startJoinClause();
        }
    }

    @Override
    public void exitJoinClause(BallerinaParser.JoinClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor joinWhiteSpaceDescriptor = null;
        if (isVerboseMode) {
            joinWhiteSpaceDescriptor = WhiteSpaceUtil.getJoinClauseWS(tokenStream, ctx);
        }
        modelBuilder.endJoinClause(getCurrentLocation(ctx), typeNameStack.pop(), ctx.Identifier().getText(),
                joinWhiteSpaceDescriptor);

    }

    public void enterAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) {

    }

    @Override
    public void exitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getJoinConditionWS(tokenStream, ctx);
            }
            modelBuilder.createAnyJoinCondition("any", ctx.IntegerLiteral().getText(), getCurrentLocation(ctx),
                    whiteSpaceDescriptor);
            enterJoinWorkers(ctx.Identifier());
        }
    }

    @Override
    public void enterAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) {

    }

    @Override
    public void exitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getJoinConditionWS(tokenStream, ctx);
            }
            modelBuilder.createAllJoinCondition("all", whiteSpaceDescriptor);
            enterJoinWorkers(ctx.Identifier());
        }

    }

    private void enterJoinWorkers(List<TerminalNode> identifiers) {
        for (TerminalNode t : identifiers) {
            WhiteSpaceDescriptor workerWhiteSpaceDescriptor = null;
            if (isVerboseMode) {
                workerWhiteSpaceDescriptor = WhiteSpaceUtil.getJoinWorkerWS(tokenStream, t);
            }
            modelBuilder.createJoinWorkers(t.getText(), workerWhiteSpaceDescriptor);
        }
    }

    @Override
    public void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startTimeoutClause();
        }
    }

    @Override
    public void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getTimeoutClauseWS(tokenStream, ctx);
        }
        modelBuilder.endTimeoutClause(getCurrentLocation(ctx), typeNameStack.pop(), ctx.Identifier().getText(),
                whiteSpaceDescriptor);

    }

    @Override
    public void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startTryCatchStmt();
    }

    @Override
    public void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getTryClauseWS(tokenStream, ctx);
        }
        modelBuilder.addTryCatchStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterCatchClauses(BallerinaParser.CatchClausesContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.addTryCatchBlockStmt();
    }

    @Override
    public void exitCatchClauses(BallerinaParser.CatchClausesContext ctx) {
    }

    @Override
    public void enterCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startCatchClause();
    }

    @Override
    public void exitCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String key = ctx.Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getCatchClauseWS(tokenStream, ctx);
        }
        modelBuilder.addCatchClause(getCurrentLocation(ctx), whiteSpaceDescriptor, typeNameStack.pop(), key);
    }

    @Override
    public void enterFinallyClause(BallerinaParser.FinallyClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.startFinallyBlock();
    }

    @Override
    public void exitFinallyClause(BallerinaParser.FinallyClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getFinallyClauseWS(tokenStream, ctx);
        }
        modelBuilder.addFinallyBlock(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
    }

    @Override
    public void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getThrowStmtWS(tokenStream, ctx);
        }
        modelBuilder.createThrowStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
    }

    @Override
    public void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getReturnStmtWS(tokenStream, ctx);
            }
            modelBuilder.createReturnStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterReplyStatement(BallerinaParser.ReplyStatementContext ctx) {
    }

    @Override
    public void exitReplyStatement(BallerinaParser.ReplyStatementContext ctx) {
        // Here the expression is only a message reference
        //modelBuilder.createSimpleVarRefExpr();
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getReplyStmtWS(tokenStream, ctx);
            }
            modelBuilder.createReplyStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) {
    }

    @Override
    public void exitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) {
    }

    /**
     * Enter a parse tree produced by the {@code invokeWorker}
     * labeled alternative in {@link BallerinaParser#triggerWorker}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterInvokeWorker(BallerinaParser.InvokeWorkerContext ctx) {

    }

    /**
     * Exit a parse tree produced by the {@code invokeWorker}
     * labeled alternative in {@link BallerinaParser#triggerWorker}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitInvokeWorker(BallerinaParser.InvokeWorkerContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getWorkerInvokeStmtWS(tokenStream, ctx);
            }
            if (ctx.Identifier() != null) {
                modelBuilder.createWorkerInvocationStmt(ctx.Identifier().getText(),
                        getCurrentLocation(ctx), whiteSpaceDescriptor);
            } else {
                modelBuilder.createWorkerInvocationStmt(null,
                        getCurrentLocation(ctx), whiteSpaceDescriptor);
            }
        }
    }

    /**
     * Enter a parse tree produced by the {@code invokeFork}
     * labeled alternative in {@link BallerinaParser#triggerWorker}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterInvokeFork(BallerinaParser.InvokeForkContext ctx) {

    }

    /**
     * Exit a parse tree produced by the {@code invokeFork}
     * labeled alternative in {@link BallerinaParser#triggerWorker}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitInvokeFork(BallerinaParser.InvokeForkContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getForkInvokeStmtWS(tokenStream, ctx);
            }
            modelBuilder.createWorkerInvocationStmt("fork",
                    getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
    }

    @Override
    public void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getWorkerReplyStmtWS(tokenStream, ctx);
            }
            modelBuilder.createWorkerReplyStmt(ctx.Identifier().getText(),
                    getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterCommentStatement(BallerinaParser.CommentStatementContext ctx) {
    }

    @Override
    public void exitCommentStatement(BallerinaParser.CommentStatementContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getCommentStmtWS(tokenStream, ctx);
            }
            modelBuilder.addCommentStmt(getCurrentLocation(ctx), whiteSpaceDescriptor, ctx.getText());
        }
    }

    @Override
    public void enterXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx) {

    }

    @Override
    public void exitXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx) {

    }

    @Override
    public void enterSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx) {

    }

    @Override
    public void exitSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        NodeLocation currentLocation = getCurrentLocation(ctx);
        BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
        modelBuilder.resolvePackageFromNameReference(nameReference);
        // simple variable ref whitespaces are already captured through name ref
        modelBuilder.createSimpleVarRefExpr(currentLocation, nameReference.getWhiteSpaceDescriptor(), nameReference);
    }

    @Override
    public void enterFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx) {

    }

    @Override
    public void exitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String fieldName = ctx.field().Identifier().getText();
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getFieldBasedVarRedWS(tokenStream, ctx);
        }
        modelBuilder.createFieldBasedVarRefExpr(getCurrentLocation(ctx), whiteSpaceDescriptor, fieldName);
    }

    @Override
    public void enterMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx) {

    }

    @Override
    public void exitMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getIndexBasedVarRefWS(tokenStream, ctx);
        }

        modelBuilder.createIndexBasedVarRefExpr(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterField(BallerinaParser.FieldContext ctx) {

    }

    @Override
    public void exitField(BallerinaParser.FieldContext ctx) {

    }

    @Override
    public void enterIndex(BallerinaParser.IndexContext ctx) {

    }

    @Override
    public void exitIndex(BallerinaParser.IndexContext ctx) {

    }

    @Override
    public void enterXmlAttrib(BallerinaParser.XmlAttribContext ctx) {

    }

    @Override
    public void exitXmlAttrib(BallerinaParser.XmlAttribContext ctx) {

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

        boolean argsAvailable = ctx.expressionList() != null;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getFunctionInvocationStmtWS(tokenStream, ctx);
            whiteSpaceDescriptor.addChildDescriptor(NAME_REF, nameReferenceStack.peek().getWhiteSpaceDescriptor());
        }

        NodeLocation currentLocation = getCurrentLocation(ctx);
        BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
        modelBuilder.validateAndSetPackagePath(currentLocation, nameReference);
        modelBuilder.createFunctionInvocationStmt(currentLocation, whiteSpaceDescriptor,
                nameReference, argsAvailable);
    }

    @Override
    public void enterActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
    }

    @Override
    public void exitActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
        NodeLocation nodeLocation = getCurrentLocation(ctx);
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getActionInvocationStmtWS(tokenStream, ctx);
        }
        modelBuilder.createActionInvocationStmt(nodeLocation, whiteSpaceDescriptor);
    }

    @Override
    public void enterTransactionStatement(BallerinaParser.TransactionStatementContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startTransactionStmt();
        }
    }

    @Override
    public void exitTransactionStatement(BallerinaParser.TransactionStatementContext ctx) {
        if (ctx.exception == null) {
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getTransactionWS(tokenStream, ctx);
            }
            modelBuilder.addTransactionStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
        }
    }

    @Override
    public void enterTransactionHandlers(BallerinaParser.TransactionHandlersContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        modelBuilder.addTransactionBlockStmt();
    }

    @Override
    public void exitTransactionHandlers(BallerinaParser.TransactionHandlersContext ctx) {

    }

    @Override
    public void enterAbortedClause(BallerinaParser.AbortedClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startAbortedClause();
        }
    }

    @Override
    public void exitAbortedClause(BallerinaParser.AbortedClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.addAbortedClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterCommittedClause(BallerinaParser.CommittedClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.startCommittedClause();
        }
    }

    @Override
    public void exitCommittedClause(BallerinaParser.CommittedClauseContext ctx) {
        if (ctx.exception == null) {
            modelBuilder.addCommittedClause(getCurrentLocation(ctx));
        }
    }

    @Override
    public void enterAbortStatement(BallerinaParser.AbortStatementContext ctx) {

    }

    @Override
    public void exitAbortStatement(BallerinaParser.AbortStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getAbortStmtWS(tokenStream, ctx);
        }
        modelBuilder.createAbortStmt(getCurrentLocation(ctx), whiteSpaceDescriptor);
    }

    @Override
    public void enterActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
    }

    @Override
    public void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        NodeLocation nodeLocation = getCurrentLocation(ctx);
        String actionName = ctx.Identifier().getText();
        BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
        modelBuilder.validateAndSetPackagePath(nodeLocation, nameReference);
        boolean argsAvailable = ctx.expressionList() != null;

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getActionInvocationExprWS(tokenStream, ctx);
            whiteSpaceDescriptor.addChildDescriptor(NAME_REF, nameReference.getWhiteSpaceDescriptor());
        }
        modelBuilder.addActionInvocationExpr(nodeLocation, whiteSpaceDescriptor, nameReference, actionName,
                argsAvailable);
    }

    @Override
    public void enterBacktickString(BallerinaParser.BacktickStringContext ctx) {
    }

    @Override
    public void exitBacktickString(BallerinaParser.BacktickStringContext ctx) {
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
    public void enterBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
    }

    @Override
    public void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterValueTypeTypeExpression(ValueTypeTypeExpressionContext ctx) {

    }

    @Override
    public void exitValueTypeTypeExpression(ValueTypeTypeExpressionContext ctx) {

    }

    @Override
    public void enterTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
    }

    @Override
    public void exitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
    }

    @Override
    public void enterSimpleLiteralExpression(SimpleLiteralExpressionContext ctx) {

    }

    @Override
    public void exitSimpleLiteralExpression(SimpleLiteralExpressionContext ctx) {

    }

    @Override
    public void enterFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
    }

    @Override
    public void exitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = ctx.expressionList() != null;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getFunctionInvocationExprWS(tokenStream, ctx);
            whiteSpaceDescriptor.addChildDescriptor(NAME_REF, nameReferenceStack.peek().getWhiteSpaceDescriptor());
        }

        NodeLocation currentLocation = getCurrentLocation(ctx);
        BLangModelBuilder.NameReference nameReference = nameReferenceStack.pop();
        modelBuilder.validateAndSetPackagePath(currentLocation, nameReference);
        modelBuilder.addFunctionInvocationExpr(currentLocation, whiteSpaceDescriptor,
                nameReference, argsAvailable);
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
    public void enterArrayLiteralExpression(ArrayLiteralExpressionContext ctx) {

    }

    @Override
    public void exitArrayLiteralExpression(ArrayLiteralExpressionContext ctx) {

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
    }

    @Override
    public void enterMapStructLiteralExpression(MapStructLiteralExpressionContext ctx) {

    }

    @Override
    public void exitMapStructLiteralExpression(MapStructLiteralExpressionContext ctx) {

    }

    @Override
    public void enterTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
    }

    @Override
    public void exitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getTypeCastingExpWS(tokenStream, ctx);
            whiteSpaceDescriptor.addChildDescriptor(TYPE_NAME, typeNameStack.peek().getWhiteSpaceDescriptor());
        }
        modelBuilder.createTypeCastExpr(getCurrentLocation(ctx), whiteSpaceDescriptor, typeNameStack.pop());
    }

    @Override
    public void enterTypeConversionExpression(TypeConversionExpressionContext ctx) {
    }

    @Override
    public void exitTypeConversionExpression(TypeConversionExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getTypeConversionExpWS(tokenStream, ctx);
        }
        modelBuilder.createTypeConversionExpr(getCurrentLocation(ctx), whiteSpaceDescriptor, typeNameStack.pop());
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
    public void enterBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {

    }

    @Override
    public void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {
        if (ctx.exception == null) {
            createBinaryExpr(ctx);
        }
    }

    @Override
    public void enterBuiltInReferenceTypeTypeExpression(BuiltInReferenceTypeTypeExpressionContext ctx) {

    }

    @Override
    public void exitBuiltInReferenceTypeTypeExpression(BuiltInReferenceTypeTypeExpressionContext ctx) {

    }

    @Override
    public void enterUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
    }

    @Override
    public void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
        if (ctx.exception == null) {
            String op = ctx.getChild(0).getText();
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getUnaryExpWS(tokenStream, ctx);
            }
            modelBuilder.createUnaryExpr(getCurrentLocation(ctx), whiteSpaceDescriptor, op);
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
    public void enterNameReference(NameReferenceContext ctx) {

    }

    @Override
    public void exitNameReference(NameReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        BLangModelBuilder.NameReference nameReference;
        if (ctx.Identifier().size() == 2) {
            String pkgName = ctx.Identifier(0).getText();
            String name = ctx.Identifier(1).getText();
            nameReference = new BLangModelBuilder.NameReference(pkgName, name);
        } else {
            String name = ctx.Identifier(0).getText();
            nameReference = new BLangModelBuilder.NameReference(null, name);
        }

        if (isVerboseMode) {
            nameReference.setWhiteSpaceDescriptor(WhiteSpaceUtil.getNameRefWS(tokenStream, ctx));
        }
        nameReference.setNodeLocation(getCurrentLocation(ctx));

        nameReferenceStack.push(nameReference);
    }

    @Override
    public void enterReturnParameters(BallerinaParser.ReturnParametersContext ctx) {
        processingReturnParams = true;
    }

    @Override
    public void exitReturnParameters(BallerinaParser.ReturnParametersContext ctx) {
        processingReturnParams = false;
    }

    @Override
    public void enterReturnTypeList(BallerinaParser.ReturnTypeListContext ctx) {
    }

    @Override
    public void exitReturnTypeList(BallerinaParser.ReturnTypeListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        List<SimpleTypeName> list = new ArrayList<>(typeNameStack);
        modelBuilder.addReturnTypes(getCurrentLocation(ctx), list.toArray(new SimpleTypeName[0]));
        typeNameStack.removeAllElements();
    }

    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) {
    }

    @Override
    public void exitParameterList(BallerinaParser.ParameterListContext ctx) {
    }

    @Override
    public void enterParameter(BallerinaParser.ParameterContext ctx) {
    }

    @Override
    public void exitParameter(BallerinaParser.ParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        int annotationCount = ctx.annotationAttachment() != null ? ctx.annotationAttachment().size() : 0;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getParamWS(tokenStream, ctx);
        }
        modelBuilder.addParam(getCurrentLocation(ctx), whiteSpaceDescriptor, typeNameStack.pop(),
                ctx.Identifier().getText(), annotationCount, processingReturnParams);
    }

    @Override
    public void enterFieldDefinition(FieldDefinitionContext ctx) {

    }

    @Override
    public void exitFieldDefinition(FieldDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        SimpleTypeName typeName = typeNameStack.pop();
        String fieldName = ctx.Identifier().getText();
        boolean isDefaultValueAvalibale = ctx.simpleLiteral() != null;
        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getFieldDefWS(tokenStream, ctx);
        }
        modelBuilder.addFieldDefinition(getCurrentLocation(ctx), whiteSpaceDescriptor, typeName, fieldName,
                isDefaultValueAvalibale);
    }

    @Override
    public void enterSimpleLiteral(SimpleLiteralContext ctx) {

    }

    @Override
    public void exitSimpleLiteral(SimpleLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        createBasicLiteral(ctx);
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
            WhiteSpaceDescriptor whiteSpaceDescriptor = null;
            if (isVerboseMode) {
                whiteSpaceDescriptor = WhiteSpaceUtil.getBinaryExprWS(tokenStream, ctx);
            }
            modelBuilder.createBinaryExpr(getCurrentLocation(ctx), whiteSpaceDescriptor, opStr);
        }
    }

    protected void createBasicLiteral(BallerinaParser.SimpleLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        WhiteSpaceDescriptor whiteSpaceDescriptor = null;
        if (isVerboseMode) {
            whiteSpaceDescriptor = WhiteSpaceUtil.getSimpleLiteralWS(tokenStream, ctx);
        }
        TerminalNode terminalNode = ctx.IntegerLiteral();
        if (terminalNode != null) {
            String op = ctx.getChild(0).getText();
            String value = terminalNode.getText();
            if (op != null && "-".equals(op)) {
                value = "-" + value;
            }
            modelBuilder.createIntegerLiteral(getCurrentLocation(ctx), whiteSpaceDescriptor, value);
            return;
        }

        terminalNode = ctx.FloatingPointLiteral();
        if (terminalNode != null) {
            String op = ctx.getChild(0).getText();
            String value = terminalNode.getText();
            if (op != null && "-".equals(op)) {
                value = "-" + value;
            }
            modelBuilder.createFloatLiteral(getCurrentLocation(ctx), whiteSpaceDescriptor, value);
            return;
        }

        terminalNode = ctx.QuotedStringLiteral();
        if (terminalNode != null) {
            String stringLiteral = terminalNode.getText();
            stringLiteral = stringLiteral.substring(1, stringLiteral.length() - 1);
            stringLiteral = StringEscapeUtils.unescapeJava(stringLiteral);
            modelBuilder.createStringLiteral(getCurrentLocation(ctx), whiteSpaceDescriptor, stringLiteral);
            return;
        }

        terminalNode = ctx.BooleanLiteral();
        if (terminalNode != null) {
            modelBuilder.createBooleanLiteral(getCurrentLocation(ctx), whiteSpaceDescriptor, terminalNode.getText());
            return;
        }

        terminalNode = ctx.NullLiteral();
        if (terminalNode != null) {
            modelBuilder.createNullLiteral(getCurrentLocation(ctx), whiteSpaceDescriptor, terminalNode.getText());
        }
    }

    protected NodeLocation getCurrentLocation(ParserRuleContext ctx) {
        String fileName = ctx.getStart().getInputStream().getSourceName();
        int lineNo = ctx.getStart().getLine();
        int startLineNumber = ctx.getStart().getLine();
        int startColumn = ctx.getStart().getCharPositionInLine();
        int stopLineNumber = -1;
        int stopColumn = -1;
        Token stop = ctx.getStop();
        if (stop != null) {
            stopLineNumber = stop.getLine();
            stopColumn = stop.getCharPositionInLine();
        }
        return new NodeLocation(packageDirPath, fileName, lineNo, startLineNumber, startColumn, stopLineNumber,
                stopColumn);
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
