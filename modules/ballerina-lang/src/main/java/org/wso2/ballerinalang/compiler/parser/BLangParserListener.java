/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserBaseListener;
import org.wso2.ballerinalang.compiler.util.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.DiagnosticPos;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangParserListener extends BallerinaParserBaseListener {

    private BLangPackageBuilder pkgBuilder;
    private BDiagnosticSource diagnosticSrc;

    public BLangParserListener(CompilationUnitNode compUnit, BDiagnosticSource diagnosticSource) {
        this.pkgBuilder = new BLangPackageBuilder(compUnit);
        this.diagnosticSrc = diagnosticSource;
    }
    
    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) {
        this.pkgBuilder.startVarList();
    }
    
    @Override 
    public void exitParameter(BallerinaParser.ParameterContext ctx) {
        this.pkgBuilder.addVar(ctx.Identifier().getText(), false);
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCompilationUnit(BallerinaParser.CompilationUnitContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
        this.pkgBuilder.populatePackageDeclaration();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterPackageName(BallerinaParser.PackageNameContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitPackageName(BallerinaParser.PackageNameContext ctx) {
        List<String> nameComps = new ArrayList<>();
        ctx.Identifier().stream().forEach(e -> nameComps.add(e.getText()));
        String version;
        if (ctx.version() != null) {
            version = ctx.version().Identifier().getText();
        } else {
            version = null;
        }
        this.pkgBuilder.addPackageId(nameComps, version);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) { }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
        String alias;
        if (ctx.Identifier() != null) {
            alias = ctx.Identifier().getText();
        } else {
            alias = null;
        }
        this.pkgBuilder.addImportPackageDeclaration(alias);
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDefinition(BallerinaParser.DefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDefinition(BallerinaParser.DefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterServiceBody(BallerinaParser.ServiceBodyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitServiceBody(BallerinaParser.ServiceBodyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx) { 
        this.pkgBuilder.startBlock();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx) { 
        this.pkgBuilder.endCallableUnitBody();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) { 
        this.pkgBuilder.startFunctionDef();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        this.pkgBuilder.endFunctionDef();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterLambdaFunction(BallerinaParser.LambdaFunctionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitLambdaFunction(BallerinaParser.LambdaFunctionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx) { 
        this.pkgBuilder.endCallableUnitSignature(ctx.Identifier().getText(), 
                ctx.parameterList() != null, ctx.returnParameters() != null, 
                ctx.returnParameters() != null ? ctx.returnParameters().typeList() != null : false);
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void enterConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        this.pkgBuilder.startConnectorDef();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) { 
        this.pkgBuilder.endConnectorDef(ctx.Identifier().getText());
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
        this.pkgBuilder.startConnectorBody();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitConnectorBody(BallerinaParser.ConnectorBodyContext ctx) { 
        this.pkgBuilder.endConnectorBody();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void enterActionDefinition(BallerinaParser.ActionDefinitionContext ctx) { 
        this.pkgBuilder.startActionDef();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitActionDefinition(BallerinaParser.ActionDefinitionContext ctx) { 
        this.pkgBuilder.endActionDef();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void enterStructDefinition(BallerinaParser.StructDefinitionContext ctx) { 
        this.pkgBuilder.startStructDef();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStructDefinition(BallerinaParser.StructDefinitionContext ctx) { 
        this.pkgBuilder.endStructDef(ctx.Identifier().getText());
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void enterStructBody(BallerinaParser.StructBodyContext ctx) { 
        this.pkgBuilder.startVarList();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStructBody(BallerinaParser.StructBodyContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx) {
        this.pkgBuilder.startAnnotationDef();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx) {
        this.pkgBuilder.endAnnotationDef(ctx.Identifier().getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx) { }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx) { 
        this.pkgBuilder.addGlobalVariable(ctx.Identifier().getText(), ctx.expression() != null);
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterServiceAttachPoint(BallerinaParser.ServiceAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitServiceAttachPoint(BallerinaParser.ServiceAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterResourceAttachPoint(BallerinaParser.ResourceAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitResourceAttachPoint(BallerinaParser.ResourceAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterConnectorAttachPoint(BallerinaParser.ConnectorAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitConnectorAttachPoint(BallerinaParser.ConnectorAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterActionAttachPoint(BallerinaParser.ActionAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitActionAttachPoint(BallerinaParser.ActionAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctionAttachPoint(BallerinaParser.FunctionAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctionAttachPoint(BallerinaParser.FunctionAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTypemapperAttachPoint(BallerinaParser.TypemapperAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTypemapperAttachPoint(BallerinaParser.TypemapperAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStructAttachPoint(BallerinaParser.StructAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStructAttachPoint(BallerinaParser.StructAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterConstAttachPoint(BallerinaParser.ConstAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitConstAttachPoint(BallerinaParser.ConstAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterParameterAttachPoint(BallerinaParser.ParameterAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitParameterAttachPoint(BallerinaParser.ParameterAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnnotationAttachPoint(BallerinaParser.AnnotationAttachPointContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAnnotationAttachPoint(BallerinaParser.AnnotationAttachPointContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterAnnotationBody(BallerinaParser.AnnotationBodyContext ctx) {
        this.pkgBuilder.startVarList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAnnotationBody(BallerinaParser.AnnotationBodyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTypeMapperSignature(BallerinaParser.TypeMapperSignatureContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTypeMapperSignature(BallerinaParser.TypeMapperSignatureContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx) { }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx) { 
        this.pkgBuilder.addConstVariable(ctx.Identifier().getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx) { }

    @Override
    public void exitTypeName(BallerinaParser.TypeNameContext ctx) {
        if (ctx.referenceTypeName() != null || ctx.valueTypeName() != null) {
            return;
        }
        if (ctx.typeName() != null) {
            // This ia an array Type.
            this.pkgBuilder.addArrayType(getCurrentPos(ctx), (ctx.getChildCount() - 1) / 2);
            return;
        }
        // This is 'any' type
        this.pkgBuilder.addValueType(getCurrentPos(ctx), ctx.getChild(0).getText());
    }

    @Override
    public void exitReferenceTypeName(BallerinaParser.ReferenceTypeNameContext ctx) {
        if (ctx.nameReference() != null) {
            this.pkgBuilder.addUserDefineType(getCurrentPos(ctx));
        }
    }

    @Override
    public void exitValueTypeName(BallerinaParser.ValueTypeNameContext ctx) {
        this.pkgBuilder.addValueType(getCurrentPos(ctx), ctx.getText());
    }

    @Override
    public void exitBuiltInReferenceTypeName(BallerinaParser.BuiltInReferenceTypeNameContext ctx) {
        if (ctx.functionTypeName() != null) {
            return;
        }
        String typeName = ctx.getChild(0).getText();
        if (ctx.nameReference() != null) {
            this.pkgBuilder.addConstraintType(getCurrentPos(ctx), typeName);
        } else {
            this.pkgBuilder.addBuiltInReferenceType(getCurrentPos(ctx), typeName);
        }
    }

    @Override
    public void exitFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx) {
        boolean paramsAvail = false, paramsTypeOnly = false, retParamsAvail = false, retParamTypeOnly = false,
                returnsKeywordExists = false;
        if (ctx.parameterList() != null) {
            paramsAvail = ctx.parameterList().parameter().size() > 0;
        } else if (ctx.typeList() != null) {
            paramsAvail = ctx.typeList().typeName().size() > 0;
            paramsTypeOnly = true;
        }

        if (ctx.returnParameters() != null) {
            BallerinaParser.ReturnParametersContext returnCtx = ctx.returnParameters();
            returnsKeywordExists = "returns".equals(returnCtx.getChild(0).getText());
            if (returnCtx.parameterList() != null) {
                retParamsAvail = returnCtx.parameterList().parameter().size() > 0;
            } else if (returnCtx.typeList() != null) {
                retParamsAvail = returnCtx.typeList().typeName().size() > 0;
                retParamTypeOnly = true;
            }
        }

        this.pkgBuilder.addFunctionType(getCurrentPos(ctx), paramsAvail, paramsTypeOnly, retParamsAvail,
                retParamTypeOnly, returnsKeywordExists);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlNamespaceName(BallerinaParser.XmlNamespaceNameContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlNamespaceName(BallerinaParser.XmlNamespaceNameContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlLocalName(BallerinaParser.XmlLocalNameContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlLocalName(BallerinaParser.XmlLocalNameContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx) {
        this.pkgBuilder.startAnnotationAttachment(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnnotationAttributeList(BallerinaParser.AnnotationAttributeListContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAnnotationAttributeList(BallerinaParser.AnnotationAttributeListContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnnotationAttribute(BallerinaParser.AnnotationAttributeContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitAnnotationAttribute(BallerinaParser.AnnotationAttributeContext ctx) {
        String attrName = ctx.Identifier().getText();
        this.pkgBuilder.createAnnotationKeyValue(attrName, getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnnotationAttributeValue(BallerinaParser.AnnotationAttributeValueContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitAnnotationAttributeValue(BallerinaParser.AnnotationAttributeValueContext ctx) {
        ParseTree childContext = ctx.getChild(0);
        if (childContext instanceof BallerinaParser.SimpleLiteralContext) {
            this.pkgBuilder.createLiteralTypeAttributeValue(getCurrentPos(ctx));
        } else if (childContext instanceof BallerinaParser.NameReferenceContext) {
            this.pkgBuilder.createVarRefTypeAttributeValue(getCurrentPos(ctx));
        } else if (childContext instanceof BallerinaParser.AnnotationAttachmentContext) {
            this.pkgBuilder.createAnnotationTypeAttributeValue(getCurrentPos(ctx));
        } else if (childContext instanceof BallerinaParser.AnnotationAttributeArrayContext) {
            this.pkgBuilder.createArrayTypeAttributeValue(getCurrentPos(ctx));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnnotationAttributeArray(BallerinaParser.AnnotationAttributeArrayContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAnnotationAttributeArray(BallerinaParser.AnnotationAttributeArrayContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStatement(BallerinaParser.StatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStatement(BallerinaParser.StatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTransformStatement(BallerinaParser.TransformStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTransformStatement(BallerinaParser.TransformStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTransformStatementBody(BallerinaParser.TransformStatementBodyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTransformStatementBody(BallerinaParser.TransformStatementBodyContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) { }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {
        this.pkgBuilder.addVariableDefStatement(ctx.Identifier().getText(), ctx.ASSIGN() != null);
    }

    @Override
    public void enterMapStructLiteral(BallerinaParser.MapStructLiteralContext ctx) {
        this.pkgBuilder.startMapStructLiteral();
    }

    @Override
    public void exitMapStructLiteral(BallerinaParser.MapStructLiteralContext ctx) {
        this.pkgBuilder.addMapStructLiteral(getCurrentPos(ctx));
    }

    @Override
    public void exitMapStructKeyValue(BallerinaParser.MapStructKeyValueContext ctx) {
        this.pkgBuilder.addKeyValueRecord();
    }

    @Override
    public void exitArrayLiteral(BallerinaParser.ArrayLiteralContext ctx) {
        boolean argsAvailable = ctx.expressionList() != null;
        this.pkgBuilder.addArrayInitExpr(getCurrentPos(ctx), argsAvailable);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFilterInitExpression(BallerinaParser.FilterInitExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFilterInitExpression(BallerinaParser.FilterInitExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFilterInitExpressionList(BallerinaParser.FilterInitExpressionListContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFilterInitExpressionList(BallerinaParser.FilterInitExpressionListContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) { 
        log("assignmentStatement");
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) { 
        log("exitAssignmentStatement");
    }

    @Override
    public void enterVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        this.pkgBuilder.startExprNodeList();
    }

    @Override
    public void exitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        this.pkgBuilder.endExprNodeList(ctx.getChildCount() / 2 + 1);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        this.pkgBuilder.startIfElseNode();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        this.pkgBuilder.endIfElseNode();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterIfClause(BallerinaParser.IfClauseContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitIfClause(BallerinaParser.IfClauseContext ctx) {
        this.pkgBuilder.addIfBlock();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        // else-if clause is also modeled as an if-else statement
        this.pkgBuilder.startIfElseNode();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        this.pkgBuilder.addElseIfBlock();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterElseClause(BallerinaParser.ElseClauseContext ctx) {
        this.pkgBuilder.startBlock();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitElseClause(BallerinaParser.ElseClauseContext ctx) {
        this.pkgBuilder.addElseBlock();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterIterateStatement(BallerinaParser.IterateStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitIterateStatement(BallerinaParser.IterateStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWhileStatement(BallerinaParser.WhileStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWhileStatement(BallerinaParser.WhileStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterContinueStatement(BallerinaParser.ContinueStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitContinueStatement(BallerinaParser.ContinueStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterBreakStatement(BallerinaParser.BreakStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitBreakStatement(BallerinaParser.BreakStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterJoinClause(BallerinaParser.JoinClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitJoinClause(BallerinaParser.JoinClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCatchClauses(BallerinaParser.CatchClausesContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCatchClauses(BallerinaParser.CatchClausesContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCatchClause(BallerinaParser.CatchClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCatchClause(BallerinaParser.CatchClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFinallyClause(BallerinaParser.FinallyClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFinallyClause(BallerinaParser.FinallyClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterThrowStatement(BallerinaParser.ThrowStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterReturnStatement(BallerinaParser.ReturnStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterReplyStatement(BallerinaParser.ReplyStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitReplyStatement(BallerinaParser.ReplyStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterInvokeWorker(BallerinaParser.InvokeWorkerContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitInvokeWorker(BallerinaParser.InvokeWorkerContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterInvokeFork(BallerinaParser.InvokeForkContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitInvokeFork(BallerinaParser.InvokeForkContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWorkerReply(BallerinaParser.WorkerReplyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCommentStatement(BallerinaParser.CommentStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCommentStatement(BallerinaParser.CommentStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx) { }

    @Override
    public void exitSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx) {
        this.pkgBuilder.createSimpleVariableReference(getCurrentPos(ctx));
    }

    @Override
    public void exitFunctionInvocationReference(BallerinaParser.FunctionInvocationReferenceContext ctx) {
        boolean argsAvailable = ctx.expressionList() != null;
        this.pkgBuilder.createInvocationNode(getCurrentPos(ctx), argsAvailable);
    }

    @Override
    public void exitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx) {
        String fieldName = ctx.field().Identifier().getText();
        this.pkgBuilder.createFieldBasedAccessNode(getCurrentPos(ctx), fieldName);
    }

    @Override
    public void exitMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx) {
        this.pkgBuilder.createIndexBasedAccessNode(getCurrentPos(ctx));
    }

    public void enterExpressionList(BallerinaParser.ExpressionListContext ctx) {
        this.pkgBuilder.startExprNodeList();
    }

    @Override
    public void exitExpressionList(BallerinaParser.ExpressionListContext ctx) {
        this.pkgBuilder.endExprNodeList(ctx.getChildCount() / 2 + 1);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTransactionStatement(BallerinaParser.TransactionStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTransactionStatement(BallerinaParser.TransactionStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTransactionHandlers(BallerinaParser.TransactionHandlersContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTransactionHandlers(BallerinaParser.TransactionHandlersContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFailedClause(BallerinaParser.FailedClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFailedClause(BallerinaParser.FailedClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAbortedClause(BallerinaParser.AbortedClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAbortedClause(BallerinaParser.AbortedClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCommittedClause(BallerinaParser.CommittedClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCommittedClause(BallerinaParser.CommittedClauseContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAbortStatement(BallerinaParser.AbortStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAbortStatement(BallerinaParser.AbortStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterRetryStatement(BallerinaParser.RetryStatementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRetryStatement(BallerinaParser.RetryStatementContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx) { }

    @Override
    public void exitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlLiteralExpression(BallerinaParser.XmlLiteralExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlLiteralExpression(BallerinaParser.XmlLiteralExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterValueTypeTypeExpression(BallerinaParser.ValueTypeTypeExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitValueTypeTypeExpression(BallerinaParser.ValueTypeTypeExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterSimpleLiteralExpression(BallerinaParser.SimpleLiteralExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitSimpleLiteralExpression(BallerinaParser.SimpleLiteralExpressionContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterLambdaFunctionExpression(BallerinaParser.LambdaFunctionExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitLambdaFunctionExpression(BallerinaParser.LambdaFunctionExpressionContext ctx) { }

    @Override
    public void exitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterArrayLiteralExpression(BallerinaParser.ArrayLiteralExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitArrayLiteralExpression(BallerinaParser.ArrayLiteralExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterBracedExpression(BallerinaParser.BracedExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitBracedExpression(BallerinaParser.BracedExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) { }

    @Override
    public void exitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx) { }

    @Override
    public void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
        this.pkgBuilder.createUnaryExpr(getCurrentPos(ctx), ctx.getChild(0).getText());
    }

    @Override
    public void exitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx) {
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitNameReference(BallerinaParser.NameReferenceContext ctx) {
        if (ctx.Identifier().size() == 2) {
            String pkgName = ctx.Identifier(0).getText();
            String name = ctx.Identifier(1).getText();
            this.pkgBuilder.addNameReference(pkgName, name);
        } else {
            String name = ctx.Identifier(0).getText();
            this.pkgBuilder.addNameReference(null, name);
        }
    }

    @Override 
    public void enterTypeList(BallerinaParser.TypeListContext ctx) { 
        this.pkgBuilder.startProcessingTypeNodeList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitParameterList(BallerinaParser.ParameterListContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterParameter(BallerinaParser.ParameterContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFieldDefinition(BallerinaParser.FieldDefinitionContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override 
    public void exitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx) { 
        this.pkgBuilder.addVar(ctx.Identifier().getText(), ctx.simpleLiteral() != null);
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx) {
        TerminalNode node;
        if ((node = ctx.IntegerLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(Integer.parseInt(node.getText()));
        } else if ((node = ctx.FloatingPointLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(Double.parseDouble(node.getText()));
        } else if ((node = ctx.BooleanLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(Boolean.parseBoolean(node.getText()));
        } else if ((node = ctx.QuotedStringLiteral()) != null) {
            String text = node.getText();
            text = text.substring(1, text.length() - 1);
            text = StringEscapeUtils.unescapeJava(text);
            this.pkgBuilder.addLiteralValue(text);
        } else if ((node = ctx.NullLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(null);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlLiteral(BallerinaParser.XmlLiteralContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlLiteral(BallerinaParser.XmlLiteralContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlItem(BallerinaParser.XmlItemContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlItem(BallerinaParser.XmlItemContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterContent(BallerinaParser.ContentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitContent(BallerinaParser.ContentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterComment(BallerinaParser.CommentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitComment(BallerinaParser.CommentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterElement(BallerinaParser.ElementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitElement(BallerinaParser.ElementContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStartTag(BallerinaParser.StartTagContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStartTag(BallerinaParser.StartTagContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCloseTag(BallerinaParser.CloseTagContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCloseTag(BallerinaParser.CloseTagContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterEmptyTag(BallerinaParser.EmptyTagContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitEmptyTag(BallerinaParser.EmptyTagContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterProcIns(BallerinaParser.ProcInsContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitProcIns(BallerinaParser.ProcInsContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAttribute(BallerinaParser.AttributeContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAttribute(BallerinaParser.AttributeContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterText(BallerinaParser.TextContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitText(BallerinaParser.TextContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlQuotedString(BallerinaParser.XmlQuotedStringContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlQuotedString(BallerinaParser.XmlQuotedStringContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlSingleQuotedString(BallerinaParser.XmlSingleQuotedStringContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlSingleQuotedString(BallerinaParser.XmlSingleQuotedStringContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlDoubleQuotedString(BallerinaParser.XmlDoubleQuotedStringContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlDoubleQuotedString(BallerinaParser.XmlDoubleQuotedStringContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterXmlQualifiedName(BallerinaParser.XmlQualifiedNameContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitXmlQualifiedName(BallerinaParser.XmlQualifiedNameContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStringTemplateLiteral(BallerinaParser.StringTemplateLiteralContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStringTemplateLiteral(BallerinaParser.StringTemplateLiteralContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStringTemplateContent(BallerinaParser.StringTemplateContentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStringTemplateContent(BallerinaParser.StringTemplateContentContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterStringTemplateText(BallerinaParser.StringTemplateTextContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitStringTemplateText(BallerinaParser.StringTemplateTextContext ctx) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterEveryRule(ParserRuleContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitEveryRule(ParserRuleContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void visitTerminal(TerminalNode node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void visitErrorNode(ErrorNode node) { }
    
    private void log(Object value) {
        PrintStream writer = System.out;
        writer.println(value);
    }

    private DiagnosticPos getCurrentPos(ParserRuleContext ctx) {
        int startLine = ctx.getStart().getLine();
        int startCol = ctx.getStart().getCharPositionInLine();

        int endLine = -1;
        int endCol = -1;
        Token stop = ctx.getStop();
        if (stop != null) {
            endLine = stop.getLine();
            endCol = stop.getCharPositionInLine();
        }

        return new DiagnosticPos(diagnosticSrc, startLine, endLine, startCol, endCol);
    }
}
