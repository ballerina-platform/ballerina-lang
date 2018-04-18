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
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser.FieldContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser.StringTemplateContentContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserBaseListener;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class BLangParserListener extends BallerinaParserBaseListener {
    private static final String KEYWORD_PUBLIC = "public";
    private static final String KEYWORD_NATIVE = "native";

    private BLangPackageBuilder pkgBuilder;
    private BDiagnosticSource diagnosticSrc;

    private List<String> pkgNameComps;
    private String pkgVersion;
    private boolean distributedTransactionEnabled;

    BLangParserListener(CompilerContext context, CompilationUnitNode compUnit,
                        BDiagnosticSource diagnosticSource) {
        this.pkgBuilder = new BLangPackageBuilder(context, compUnit);
        this.diagnosticSrc = diagnosticSource;
        this.distributedTransactionEnabled = CompilerUtils.isDistributedTransactionsEnabled();
    }

    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    @Override
    public void exitSimpleParameter(BallerinaParser.SimpleParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                false, ctx.annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFormalParameterList(BallerinaParser.FormalParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDefaultableParameter(BallerinaParser.DefaultableParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addDefaultableParam(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRestParameter(BallerinaParser.RestParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addRestParam(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                ctx.annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterTypeName(BallerinaParser.ParameterTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        //TODO setting annotation count 0 as parameters won't adding annotations to parameters.
        this.pkgBuilder.addVar(getCurrentPos(ctx), getWS(ctx), null, false, 0);
    }

    @Override
    public void enterCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
        this.pkgBuilder.endCompilationUnit(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.setPackageDeclaration(getCurrentPos(ctx), getWS(ctx), this.pkgNameComps, this.pkgVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitPackageName(BallerinaParser.PackageNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgNameComps = new ArrayList<>();
        ctx.Identifier().forEach(e -> pkgNameComps.add(e.getText()));
        this.pkgVersion = ctx.version() != null ? ctx.version().Identifier().getText() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String alias = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        BallerinaParser.OrgNameContext orgNameContext = ctx.orgName();
        if (orgNameContext == null) {
            this.pkgBuilder.addImportPackageDeclaration(getCurrentPos(ctx), getWS(ctx),
                    null, this.pkgNameComps, this.pkgVersion, alias);
        } else {
            this.pkgBuilder.addImportPackageDeclaration(getCurrentPos(ctx), getWS(ctx),
                    orgNameContext.getText(), this.pkgNameComps, this.pkgVersion, alias);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.startServiceDef(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        boolean constrained = ctx.nameReference() != null;
        this.pkgBuilder.endServiceDef(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(), constrained);
    }

    @Override
    public void exitServiceEndpointAttachments(BallerinaParser.ServiceEndpointAttachmentsContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        if (ctx.recordLiteral() != null) {
            this.pkgBuilder.addAnonymousEndpointBind();
            return;
        }
        this.pkgBuilder.addServiceEndpointAttachments(ctx.nameReference().size(), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterServiceBody(BallerinaParser.ServiceBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitServiceBody(BallerinaParser.ServiceBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addServiceBody(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.startResourceDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean docExists = ctx.documentationAttachment() != null;
        boolean isDeprecated = ctx.deprecatedAttachment() != null;
        boolean hasParameters = ctx.resourceParameterList() != null;
        this.pkgBuilder.endResourceDef(getCurrentPos(ctx), getWS(ctx),
                ctx.Identifier().getText(), docExists, isDeprecated, hasParameters);
    }

    @Override
    public void enterResourceParameterList(BallerinaParser.ResourceParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        final BallerinaParser.ResourceDefinitionContext parent = (BallerinaParser.ResourceDefinitionContext) ctx.parent;
        this.pkgBuilder.addResourceAnnotation(parent.annotationAttachment().size());
    }

    @Override
    public void exitResourceParameterList(BallerinaParser.ResourceParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        final boolean isEndpointDefined = ctx.ENDPOINT() != null;
        if (isEndpointDefined) {
            this.pkgBuilder.addEndpointVariable(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endCallableUnitBody(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startFunctionDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        int nativeKWTokenIndex = 0;
        boolean publicFunc = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        if (publicFunc) {
            nativeKWTokenIndex = 1;
        }
        boolean nativeFunc = KEYWORD_NATIVE.equals(ctx.getChild(nativeKWTokenIndex).getText());
        boolean bodyExists = ctx.callableUnitBody() != null;

        if (ctx.Identifier() != null) {
            this.pkgBuilder.endObjectOuterFunctionDef(getCurrentPos(ctx), getWS(ctx), publicFunc, nativeFunc,
                    bodyExists, ctx.Identifier().getText());
            return;
        }

        // TODO remove when removing struct
        boolean isReceiverAttached = ctx.parameter() != null;

        this.pkgBuilder.endFunctionDef(getCurrentPos(ctx), getWS(ctx), publicFunc, nativeFunc,
                bodyExists, isReceiverAttached, false);
    }

    @Override
    public void enterLambdaFunction(BallerinaParser.LambdaFunctionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startLambdaFunctionDef(diagnosticSrc.pkgID);
    }

    @Override
    public void exitLambdaFunction(BallerinaParser.LambdaFunctionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addLambdaFunctionDef(getCurrentPos(ctx), getWS(ctx), ctx.formalParameterList() != null,
                ctx.lambdaReturnParameter() != null,
                ctx.formalParameterList() != null && ctx.formalParameterList().restParameter() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endCallableUnitSignature(getCurrentPos(ctx), getWS(ctx), ctx.anyIdentifierName().getText(),
                ctx.formalParameterList() != null, ctx.returnParameter() != null,
                ctx.formalParameterList() != null && ctx.formalParameterList().restParameter() != null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitTypeDefinition(BallerinaParser.TypeDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicObject = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        this.pkgBuilder.endTypeDefinition(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(), publicObject);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterObjectBody(BallerinaParser.ObjectBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startObjectDef();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectBody(BallerinaParser.ObjectBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (!(ctx.parent.parent instanceof BallerinaParser.FiniteTypeUnitContext)) {
            this.pkgBuilder.addAnonObjectType(getCurrentPos(ctx), getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterObjectInitializer(BallerinaParser.ObjectInitializerContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startFunctionDef();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectInitializer(BallerinaParser.ObjectInitializerContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicFunc = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        boolean bodyExists = ctx.callableUnitBody() != null;
        this.pkgBuilder.endObjectInitFunctionDef(getCurrentPos(ctx), getWS(ctx), ctx.NEW().getText(),
                publicFunc, bodyExists);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectInitializerParameterList(BallerinaParser.ObjectInitializerParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endObjectInitParamList(getWS(ctx), ctx.objectParameterList() != null,
                ctx.objectParameterList() != null && ctx.objectParameterList().restParameter() != null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        DiagnosticPos currentPos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);
        String name = ctx.Identifier().getText();
        boolean exprAvailable = ctx.expression() != null;
        if (ctx.parent instanceof BallerinaParser.PublicObjectFieldsContext) {
            this.pkgBuilder.addFieldToObject(currentPos, ws, name,
                    exprAvailable, ctx.annotationAttachment().size(), false);
        } else if (ctx.parent instanceof BallerinaParser.PrivateObjectFieldsContext) {
            this.pkgBuilder.addFieldToObject(currentPos, ws, name,
                    exprAvailable, ctx.annotationAttachment().size(), true);
        } else if (ctx.parent instanceof BallerinaParser.FieldDefinitionListContext) {
            this.pkgBuilder.addFieldToRecord(currentPos, ws, name, exprAvailable, ctx.annotationAttachment().size());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterObjectParameterList(BallerinaParser.ObjectParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectParameter(BallerinaParser.ObjectParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isField = ctx.typeName() == null;
        this.pkgBuilder.addObjectParameter(getCurrentPos(ctx), getWS(ctx), isField, ctx.Identifier().getText(),
                false, ctx.annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectDefaultableParameter(BallerinaParser.ObjectDefaultableParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addDefaultableParam(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startFunctionDef();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        int nativeKWTokenIndex = 0;
        boolean publicFunc = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        if (publicFunc) {
            nativeKWTokenIndex = 1;
        }
        boolean nativeFunc = KEYWORD_NATIVE.equals(ctx.getChild(nativeKWTokenIndex).getText());
        boolean bodyExists = ctx.callableUnitBody() != null;
        this.pkgBuilder.endObjectAttachedFunctionDef(getCurrentPos(ctx), getWS(ctx),
                publicFunc, nativeFunc, bodyExists);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitObjectCallableUnitSignature(BallerinaParser.ObjectCallableUnitSignatureContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endCallableUnitSignature(getCurrentPos(ctx), getWS(ctx), ctx.anyIdentifierName().getText(),
                ctx.formalParameterList() != null, ctx.returnParameter() != null,
                ctx.formalParameterList() != null && ctx.formalParameterList().restParameter() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startAnnotationDef(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicAnnotation = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        boolean isTypeAttached = ctx.userDefineTypeName() != null;
        this.pkgBuilder.endAnnotationDef(getWS(ctx), ctx.Identifier().getText(), publicAnnotation, isTypeAttached);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicVar = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        this.pkgBuilder.addGlobalVariable(getCurrentPos(ctx), getWS(ctx),
                ctx.Identifier().getText(), ctx.expression() != null, publicVar);
    }

    @Override
    public void exitAttachmentPoint(BallerinaParser.AttachmentPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addAttachPoint(
                BLangAnnotationAttachmentPoint.AttachmentPoint.getAttachmentPoint(ctx.getText()));
    }

    @Override
    public void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startWorker();
    }

    @Override
    public void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String workerName = null;
        if (ctx.workerDefinition() != null) {
            workerName = ctx.workerDefinition().Identifier().getText();
        }
        this.pkgBuilder.addWorker(getCurrentPos(ctx), getWS(ctx), workerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.attachWorkerWS(getWS(ctx));
    }

    @Override
    public void exitArrayTypeNameLabel(BallerinaParser.ArrayTypeNameLabelContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addArrayType(getCurrentPos(ctx), getWS(ctx),
                (ctx.getChildCount() - 1) / 2);
    }

    @Override
    public void exitUnionTypeNameLabel(BallerinaParser.UnionTypeNameLabelContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addUnionType(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitTupleTypeNameLabel(BallerinaParser.TupleTypeNameLabelContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addTupleType(getCurrentPos(ctx), getWS(ctx), ctx.typeName().size());
    }

    @Override
    public void exitNullableTypeNameLabel(BallerinaParser.NullableTypeNameLabelContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.markTypeNodeAsNullable(getWS(ctx));
    }

    @Override
    public void exitGroupTypeNameLabel(BallerinaParser.GroupTypeNameLabelContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.markTypeNodeAsGrouped(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterFieldDefinitionList(BallerinaParser.FieldDefinitionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startRecordDef();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitFieldDefinitionList(BallerinaParser.FieldDefinitionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (!(ctx.parent.parent instanceof BallerinaParser.FiniteTypeUnitContext)) {
            this.pkgBuilder.addAnonRecordType(getCurrentPos(ctx), getWS(ctx));
        }
    }

    @Override
    public void exitSimpleTypeName(BallerinaParser.SimpleTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.referenceTypeName() != null || ctx.valueTypeName() != null) {
            return;
        }

        // This is 'any' type
        this.pkgBuilder.addValueType(getCurrentPos(ctx), getWS(ctx), ctx.getChild(0).getText());
    }

    @Override
    public void exitUserDefineTypeName(BallerinaParser.UserDefineTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addUserDefineType(getWS(ctx));
    }

    @Override
    public void exitValueTypeName(BallerinaParser.ValueTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addValueType(getCurrentPos(ctx), getWS(ctx), ctx.getText());
    }

    @Override
    public void exitBuiltInReferenceTypeName(BallerinaParser.BuiltInReferenceTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        if (ctx.functionTypeName() != null) {
            return;
        }
        String typeName = ctx.getChild(0).getText();
        if (ctx.nameReference() != null) {
            this.pkgBuilder.addConstraintType(getCurrentPos(ctx), getWS(ctx), typeName);
        } else if (ctx.typeName() != null) {
            this.pkgBuilder.addConstraintTypeWithTypeName(getCurrentPos(ctx), getWS(ctx), typeName);
        } else {
            this.pkgBuilder.addBuiltInReferenceType(getCurrentPos(ctx), getWS(ctx), typeName);
        }
    }

    @Override
    public void exitFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean paramsAvail = false, paramsTypeOnly = false, retParamAvail = false;
        if (ctx.parameterList() != null) {
            paramsAvail = ctx.parameterList().parameter().size() > 0;
        } else if (ctx.parameterTypeNameList() != null) {
            paramsAvail = ctx.parameterTypeNameList().parameterTypeName().size() > 0;
            paramsTypeOnly = true;
        }

        if (ctx.returnParameter() != null) {
            retParamAvail = true;
        }

        this.pkgBuilder.addFunctionType(getCurrentPos(ctx), getWS(ctx), paramsAvail, paramsTypeOnly, retParamAvail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startAnnotationAttachment(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.setAnnotationAttachmentName(getWS(ctx), ctx.recordLiteral() != null,
                getCurrentPos(ctx), false);
    }


    @Override
    public void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean exprAvailable = ctx.ASSIGN() != null;
        this.pkgBuilder.addVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                ctx.Identifier().getText(), exprAvailable, false);
    }

    @Override
    public void enterRecordLiteral(BallerinaParser.RecordLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startMapStructLiteral();
    }

    @Override
    public void exitRecordLiteral(BallerinaParser.RecordLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addMapStructLiteral(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitRecordKeyValue(BallerinaParser.RecordKeyValueContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addKeyValueRecord(getWS(ctx));
    }

    @Override
    public void exitRecordKey(BallerinaParser.RecordKeyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        // If the key is an expression, they are added to the model
        // from their respective listener methods
        if (ctx.Identifier() != null) {
            DiagnosticPos pos = getCurrentPos(ctx);
            this.pkgBuilder.addNameReference(pos, getWS(ctx), null, ctx.Identifier().getText());
            this.pkgBuilder.createSimpleVariableReference(pos, getWS(ctx));
        }
    }

    @Override
    public void exitTableLiteral(BallerinaParser.TableLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addTableLiteral(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitArrayLiteral(BallerinaParser.ArrayLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = ctx.expressionList() != null;
        this.pkgBuilder.addArrayInitExpr(getCurrentPos(ctx), getWS(ctx), argsAvailable);
    }

    @Override
    public void exitTypeInitExpr(BallerinaParser.TypeInitExprContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String initName = ctx.NEW().getText();
        boolean typeAvailable = ctx.userDefineTypeName() != null;
        boolean argsAvailable = ctx.invocationArgList() != null;
        this.pkgBuilder.addTypeInitExpression(getCurrentPos(ctx), getWS(ctx), initName, typeAvailable, argsAvailable);
    }

    @Override
    public void exitEndpointDeclaration(BallerinaParser.EndpointDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String endpointName = ctx.Identifier().getText();
        boolean isInitExprExist = ctx.endpointInitlization() != null;
        this.pkgBuilder.addEndpointDefinition(getCurrentPos(ctx), getWS(ctx), endpointName, isInitExprExist);
    }

    @Override
    public void exitEndpointType(BallerinaParser.EndpointTypeContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addEndpointType(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitGlobalEndpointDefinition(BallerinaParser.GlobalEndpointDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        if (KEYWORD_PUBLIC.equals(ctx.getChild(0).getText())) {
            this.pkgBuilder.markLastEndpointAsPublic();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isVarDeclaration = false;
        if (ctx.VAR() != null) {
            isVarDeclaration = true;
        }
        this.pkgBuilder.addAssignmentStatement(getCurrentPos(ctx), getWS(ctx),
                isVarDeclaration);
    }

    @Override
    public void exitTupleDestructuringStatement(BallerinaParser.TupleDestructuringStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isVarDeclaration = false;
        boolean isVarExist = ctx.variableReferenceList() != null;
        if (ctx.VAR() != null) {
            isVarDeclaration = true;
        }
        this.pkgBuilder.addTupleDestructuringStatement(getCurrentPos(ctx), getWS(ctx), isVarExist, isVarDeclaration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCompoundAssignmentStatement(BallerinaParser.CompoundAssignmentStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addCompoundAssignmentStatement(getCurrentPos(ctx), getWS(ctx),
                ctx.compoundOperator().getText().substring(0, 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitPostIncrementStatement(BallerinaParser.PostIncrementStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addPostIncrementStatement(getCurrentPos(ctx), getWS(ctx),
                ctx.postArithmeticOperator().getText().substring(0, 1));
    }

    @Override
    public void enterVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startExprNodeList();
    }

    @Override
    public void exitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endExprNodeList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startIfElseNode(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endIfElseNode(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitIfClause(BallerinaParser.IfClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addIfBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        // else-if clause is also modeled as an if-else statement
        this.pkgBuilder.startIfElseNode(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addElseIfBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addElseBlock(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterMatchStatement(BallerinaParser.MatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.createMatchNode(getCurrentPos(ctx));
    }

    @Override
    public void exitMatchStatement(BallerinaParser.MatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.completeMatchNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.startMatchStmtPattern();
    }

    @Override
    public void exitMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String identifier = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        this.pkgBuilder.addMatchStmtPattern(getCurrentPos(ctx), getWS(ctx), identifier);
    }

    @Override
    public void enterForeachStatement(BallerinaParser.ForeachStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.startForeachStatement();
    }

    @Override
    public void exitForeachStatement(BallerinaParser.ForeachStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addForeachStatement(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitIntRangeExpression(BallerinaParser.IntRangeExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addIntRangeExpression(getCurrentPos(ctx), getWS(ctx),
                ctx.LEFT_PARENTHESIS() == null, ctx.RIGHT_PARENTHESIS() == null,
                ctx.expression(1) == null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startWhileStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addWhileStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitNextStatement(BallerinaParser.NextStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addNextStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitBreakStatement(BallerinaParser.BreakStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addBreakStatement(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startForkJoinStmt();
    }

    @Override
    public void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addForkJoinStmt(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterJoinClause(BallerinaParser.JoinClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startJoinCause();
    }

    @Override
    public void exitJoinClause(BallerinaParser.JoinClauseContext ctx) {
        this.pkgBuilder.addJoinCause(this.getWS(ctx), ctx.Identifier().getText());
    }

    @Override
    public void exitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        List<String> workerNames = new ArrayList<>();
        if (ctx.Identifier() != null) {
            workerNames = ctx.Identifier().stream().map(TerminalNode::getText).collect(Collectors.toList());
        }
        int joinCount = 0;
        Long longObject;
        if ((longObject = getIntegerLiteral(ctx, ctx.integerLiteral())) != null) {
            try {
                joinCount = longObject.intValue();
            } catch (NumberFormatException ex) {
                // When ctx.IntegerLiteral() is not a string or missing, compilation fails due to NumberFormatException.
                // Hence catching the error and ignore. Still Parser complains about missing IntegerLiteral.
            }
        }
        this.pkgBuilder.addJoinCondition(getWS(ctx), "SOME", workerNames, joinCount);
    }

    @Override
    public void exitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        List<String> workerNames = new ArrayList<>();
        if (ctx.Identifier() != null) {
            workerNames = ctx.Identifier().stream().map(TerminalNode::getText).collect(Collectors.toList());
        }
        this.pkgBuilder.addJoinCondition(getWS(ctx), "ALL", workerNames, -1);
    }

    @Override
    public void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startTimeoutCause();
    }

    @Override
    public void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addTimeoutCause(this.getWS(ctx), ctx.Identifier().getText());
    }

    @Override
    public void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startTryCatchFinallyStmt();
    }

    @Override
    public void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addTryCatchFinallyStmt(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterCatchClauses(BallerinaParser.CatchClausesContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addTryClause(getCurrentPos(ctx));
    }

    @Override
    public void enterCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startCatchClause();
    }

    @Override
    public void exitCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String paramName = ctx.Identifier().getText();
        this.pkgBuilder.addCatchClause(getCurrentPos(ctx), getWS(ctx), paramName);
    }

    @Override
    public void enterFinallyClause(BallerinaParser.FinallyClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startFinallyBlock();
    }

    @Override
    public void exitFinallyClause(BallerinaParser.FinallyClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addFinallyBlock(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addThrowStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addReturnStatement(this.getCurrentPos(ctx), getWS(ctx), ctx.expression() != null);
    }

    @Override
    public void exitInvokeWorker(BallerinaParser.InvokeWorkerContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addWorkerSendStmt(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(), false);
    }

    @Override
    public void exitInvokeFork(BallerinaParser.InvokeForkContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addWorkerSendStmt(getCurrentPos(ctx), getWS(ctx), "FORK", true);
    }

    @Override
    public void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addWorkerReceiveStmt(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx) {
        boolean isSingleAttrRef = ctx.xmlAttrib().expression() != null;
        this.pkgBuilder.createXmlAttributesRefExpr(getCurrentPos(ctx), getWS(ctx), isSingleAttrRef);
    }

    @Override
    public void exitSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createSimpleVariableReference(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitFunctionInvocation(BallerinaParser.FunctionInvocationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = ctx.invocationArgList() != null;
        this.pkgBuilder.createFunctionInvocation(getCurrentPos(ctx), getWS(ctx), argsAvailable);
    }

    @Override
    public void exitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        FieldContext field = ctx.field();
        String fieldName;
        FieldKind fieldType;
        if (field.Identifier() != null) {
            fieldName = field.Identifier().getText();
            fieldType = FieldKind.SINGLE;
        } else {
            fieldName = field.MUL().getText();
            fieldType = FieldKind.ALL;
        }

        this.pkgBuilder.createFieldBasedAccessNode(getCurrentPos(ctx), getWS(ctx), fieldName, fieldType,
                ctx.field().NOT() != null);
    }

    @Override
    public void exitMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createIndexBasedAccessNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitReservedWord(BallerinaParser.ReservedWordContext ctx) {
        this.pkgBuilder.startInvocationNode(getWS(ctx));
    }

    @Override
    public void exitAnyIdentifierName(BallerinaParser.AnyIdentifierNameContext ctx) {
        this.pkgBuilder.startInvocationNode(getWS(ctx));
    }

    @Override
    public void exitInvocationReference(BallerinaParser.InvocationReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = ctx.invocation().invocationArgList() != null;
        String invocation = ctx.invocation().anyIdentifierName().getText();
        boolean safeNavigate = ctx.invocation().NOT() != null;
        this.pkgBuilder.createInvocationNode(getCurrentPos(ctx), getWS(ctx), invocation, argsAvailable, safeNavigate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInvocationArgList(BallerinaParser.InvocationArgListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startExprNodeList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInvocationArgList(BallerinaParser.InvocationArgListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endExprNodeList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    public void enterExpressionList(BallerinaParser.ExpressionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startExprNodeList();
    }

    @Override
    public void exitExpressionList(BallerinaParser.ExpressionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endExprNodeList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    @Override
    public void exitExpressionStmt(BallerinaParser.ExpressionStmtContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addExpressionStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterTransactionStatement(BallerinaParser.TransactionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startTransactionStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransactionStatement(BallerinaParser.TransactionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endTransactionStmt(getCurrentPos(ctx), getWS(ctx), this.distributedTransactionEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransactionClause(BallerinaParser.TransactionClauseContext ctx) {
        this.pkgBuilder.addTransactionBlock(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterLockStatement(BallerinaParser.LockStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startLockStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitLockStatement(BallerinaParser.LockStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addLockStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOnretryClause(BallerinaParser.OnretryClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startOnretryBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitOnretryClause(BallerinaParser.OnretryClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addOnretryBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAbortStatement(BallerinaParser.AbortStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAbortStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDoneStatement(BallerinaParser.DoneStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addDoneStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFailStatement(BallerinaParser.FailStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addFailStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRetriesStatement(BallerinaParser.RetriesStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addRetryCountExpression();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitOncommitStatement(BallerinaParser.OncommitStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addCommittedBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitOnabortStatement(BallerinaParser.OnabortStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addAbortedBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx) {
    }

    @Override
    public void exitNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isTopLevel = ctx.parent instanceof BallerinaParser.CompilationUnitContext;
        String namespaceUri = ctx.QuotedStringLiteral().getText();
        namespaceUri = namespaceUri.substring(1, namespaceUri.length() - 1);
        namespaceUri = StringEscapeUtils.unescapeJava(namespaceUri);
        String prefix = (ctx.Identifier() != null) ? ctx.Identifier().getText() : null;

        this.pkgBuilder.addXMLNSDeclaration(getCurrentPos(ctx), getWS(ctx), namespaceUri, prefix, isTopLevel);
    }

    @Override
    public void exitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTypeAccessExpression(BallerinaParser.TypeAccessExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createTypeAccessExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createActionInvocationNode(getCurrentPos(ctx), getWS(ctx), ctx.START() != null);
    }

    @Override
    public void exitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createTypeConversionExpr(getCurrentPos(ctx), getWS(ctx), ctx.functionInvocation() != null);
    }

    @Override
    public void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createUnaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(0).getText());
    }

    @Override
    public void exitBracedOrTupleExpression(BallerinaParser.BracedOrTupleExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.createBracedOrTupleExpression(getCurrentPos(ctx), getWS(ctx), ctx.expression().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTernaryExpression(BallerinaParser.TernaryExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createTernaryExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitCheckedExpression(BallerinaParser.CheckedExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createCheckedExpr(getCurrentPos(ctx), getWS(ctx));
    }


    @Override
    public void exitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitNameReference(BallerinaParser.NameReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.Identifier().size() == 2) {
            String pkgName = ctx.Identifier(0).getText();
            String name = ctx.Identifier(1).getText();
            this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), pkgName, name);
        } else {
            String name = ctx.Identifier(0).getText();
            this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), null, name);
        }
    }

    @Override
    public void exitFunctionNameReference(BallerinaParser.FunctionNameReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.Identifier() != null) {
            String pkgName = ctx.Identifier().getText();
            String name = ctx.anyIdentifierName().getText();
            this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), pkgName, name);
        } else {
            String name = ctx.anyIdentifierName().getText();
            this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), null, name);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitReturnParameter(BallerinaParser.ReturnParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addReturnParam(getCurrentPos(ctx), getWS(ctx), null, false, ctx.annotationAttachment().size());
    }

    @Override
    public void exitLambdaReturnParameter(BallerinaParser.LambdaReturnParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addReturnParam(getCurrentPos(ctx), getWS(ctx), null, false, ctx.annotationAttachment().size());
    }

    @Override
    public void enterParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        // This attaches WS of the commas to the def.
        ParserRuleContext parent = ctx.getParent();
        boolean inFuncTypeSig = parent instanceof BallerinaParser.FunctionTypeNameContext ||
                parent instanceof BallerinaParser.ReturnParameterContext &&
                        parent.parent instanceof BallerinaParser.FunctionTypeNameContext;
        if (inFuncTypeSig) {
            this.pkgBuilder.endFuncTypeParamList(getWS(ctx));
        } else {
            this.pkgBuilder.endCallableParamList(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterList(BallerinaParser.ParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        // This attaches WS of the commas to the def.
        ParserRuleContext parent = ctx.getParent();
        boolean inFuncTypeSig = parent instanceof BallerinaParser.FunctionTypeNameContext ||
                parent instanceof BallerinaParser.ReturnParameterContext &&
                        parent.parent instanceof BallerinaParser.FunctionTypeNameContext;
        if (inFuncTypeSig) {
            this.pkgBuilder.endFuncTypeParamList(getWS(ctx));
        } else {
            this.pkgBuilder.endCallableParamList(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        TerminalNode node;
        DiagnosticPos pos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);
        Long longObject;
        BallerinaParser.IntegerLiteralContext integerLiteralContext = ctx.integerLiteral();
        if (integerLiteralContext != null && (longObject = getIntegerLiteral(ctx, ctx.integerLiteral())) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.INT, longObject);
        } else if ((node = ctx.FloatingPointLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.FLOAT, Double.parseDouble(getNodeValue(ctx, node)));
        } else if ((node = ctx.BooleanLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.BOOLEAN, Boolean.parseBoolean(node.getText()));
        } else if ((node = ctx.QuotedStringLiteral()) != null) {
            String text = node.getText();
            text = text.substring(1, text.length() - 1);
            text = StringEscapeUtils.unescapeJava(text);
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.STRING, text);
        } else if (ctx.NullLiteral() != null || ctx.emptyTupleLiteral() != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.NIL, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitNamedArgs(BallerinaParser.NamedArgsContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addNamedArgument(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRestArgs(BallerinaParser.RestArgsContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addRestArgument(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlLiteral(BallerinaParser.XmlLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.attachXmlLiteralWS(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitComment(BallerinaParser.CommentContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        Stack<String> stringFragments = getTemplateTextFragments(ctx.XMLCommentTemplateText());
        String endingString = getTemplateEndingStr(ctx.XMLCommentText());
        endingString = endingString.substring(0, endingString.length() - 3);
        this.pkgBuilder.createXMLCommentLiteral(getCurrentPos(ctx), getWS(ctx), stringFragments, endingString);

        if (ctx.getParent() instanceof BallerinaParser.ContentContext) {
            this.pkgBuilder.addChildToXMLElement(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitElement(BallerinaParser.ElementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.getParent() instanceof BallerinaParser.ContentContext) {
            this.pkgBuilder.addChildToXMLElement(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitStartTag(BallerinaParser.StartTagContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isRoot = ctx.parent.parent instanceof BallerinaParser.XmlItemContext;
        this.pkgBuilder.startXMLElement(getCurrentPos(ctx), getWS(ctx), isRoot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCloseTag(BallerinaParser.CloseTagContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endXMLElement(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitEmptyTag(BallerinaParser.EmptyTagContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isRoot = ctx.parent.parent instanceof BallerinaParser.XmlItemContext;
        this.pkgBuilder.startXMLElement(getCurrentPos(ctx), getWS(ctx), isRoot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitProcIns(BallerinaParser.ProcInsContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String targetQName = ctx.XML_TAG_SPECIAL_OPEN().getText();
        // removing the starting '<?' and the trailing whitespace
        targetQName = targetQName.substring(2, targetQName.length() - 1);

        Stack<String> textFragments = getTemplateTextFragments(ctx.XMLPITemplateText());
        String endingText = getTemplateEndingStr(ctx.XMLPIText());
        endingText = endingText.substring(0, endingText.length() - 2);

        this.pkgBuilder.createXMLPILiteral(getCurrentPos(ctx), getWS(ctx), targetQName, textFragments, endingText);

        if (ctx.getParent() instanceof BallerinaParser.ContentContext) {
            this.pkgBuilder.addChildToXMLElement(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAttribute(BallerinaParser.AttributeContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createXMLAttribute(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitText(BallerinaParser.TextContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        Stack<String> textFragments = getTemplateTextFragments(ctx.XMLTemplateText());
        String endingText = getTemplateEndingStr(ctx.XMLText());
        if (ctx.getParent() instanceof BallerinaParser.ContentContext) {
            this.pkgBuilder.addXMLTextToElement(getCurrentPos(ctx), getWS(ctx), textFragments, endingText);
        } else {
            this.pkgBuilder.createXMLTextLiteral(getCurrentPos(ctx), getWS(ctx), textFragments, endingText);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlSingleQuotedString(BallerinaParser.XmlSingleQuotedStringContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        Stack<String> stringFragments = getTemplateTextFragments(ctx.XMLSingleQuotedTemplateString());
        String endingString = getTemplateEndingStr(ctx.XMLSingleQuotedString());
        this.pkgBuilder.createXMLQuotedLiteral(getCurrentPos(ctx), getWS(ctx), stringFragments, endingString,
                QuoteType.SINGLE_QUOTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlDoubleQuotedString(BallerinaParser.XmlDoubleQuotedStringContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        Stack<String> stringFragments = getTemplateTextFragments(ctx.XMLDoubleQuotedTemplateString());
        String endingString = getTemplateEndingStr(ctx.XMLDoubleQuotedString());
        this.pkgBuilder.createXMLQuotedLiteral(getCurrentPos(ctx), getWS(ctx), stringFragments, endingString,
                QuoteType.DOUBLE_QUOTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlQualifiedName(BallerinaParser.XmlQualifiedNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.expression() != null) {
            return;
        }

        List<TerminalNode> qnames = ctx.XMLQName();
        String prefix = null;
        String localname;

        if (qnames.size() > 1) {
            prefix = qnames.get(0).getText();
            localname = qnames.get(1).getText();
        } else {
            localname = qnames.get(0).getText();
        }

        this.pkgBuilder.createXMLQName(getCurrentPos(ctx), getWS(ctx), localname, prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitStringTemplateLiteral(BallerinaParser.StringTemplateLiteralContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        Stack<String> stringFragments;
        String endingText = null;
        StringTemplateContentContext contentContext = ctx.stringTemplateContent();
        if (contentContext != null) {
            stringFragments = getTemplateTextFragments(contentContext.StringTemplateExpressionStart());
            endingText = getTemplateEndingStr(contentContext.StringTemplateText());
        } else {
            stringFragments = new Stack<>();
        }

        this.pkgBuilder.createStringTemplateLiteral(getCurrentPos(ctx), getWS(ctx), stringFragments, endingText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTableQueryExpression(BallerinaParser.TableQueryExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addTableQueryExpression(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterOrderByClause(BallerinaParser.OrderByClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startOrderByClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitOrderByClause(BallerinaParser.OrderByClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endOrderByClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterLimitClause(BallerinaParser.LimitClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startLimitClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitLimitClause(BallerinaParser.LimitClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endLimitClauseNode(getCurrentPos(ctx), getWS(ctx), ctx.DecimalIntegerLiteral().getText());
    }

    @Override
    public void enterOrderByVariable(BallerinaParser.OrderByVariableContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startOrderByVariableNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override public void exitOrderByVariable(BallerinaParser.OrderByVariableContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isAscending = ctx.orderByType() != null && ctx.orderByType().ASCENDING() != null;
        boolean isDescending = ctx.orderByType() != null && ctx.orderByType().DESCENDING() != null;

        this.pkgBuilder.endOrderByVariableNode(getCurrentPos(ctx), getWS(ctx), isAscending, isDescending);
    }

    @Override
    public void enterGroupByClause(BallerinaParser.GroupByClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startGroupByClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitGroupByClause(BallerinaParser.GroupByClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endGroupByClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterHavingClause(BallerinaParser.HavingClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startHavingClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitHavingClause(BallerinaParser.HavingClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endHavingClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSelectExpression(BallerinaParser.SelectExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startSelectExpressionNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitSelectExpression(BallerinaParser.SelectExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String identifier = ctx.Identifier() == null ? null : ctx.Identifier().getText();
        this.pkgBuilder.endSelectExpressionNode(identifier, getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSelectClause(BallerinaParser.SelectClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startSelectClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitSelectClause(BallerinaParser.SelectClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isSelectAll = ctx.MUL() != null;
        boolean isGroupByClauseAvailable = ctx.groupByClause() != null;
        boolean isHavingClauseAvailable = ctx.havingClause() != null;
        this.pkgBuilder.endSelectClauseNode(isSelectAll, isGroupByClauseAvailable, isHavingClauseAvailable,
                getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSelectExpressionList(BallerinaParser.SelectExpressionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startSelectExpressionList();
    }

    @Override
    public void exitSelectExpressionList(BallerinaParser.SelectExpressionListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endSelectExpressionList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    @Override
    public void enterWhereClause(BallerinaParser.WhereClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startWhereClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitWhereClause(BallerinaParser.WhereClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endWhereClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSetAssignmentClause(BallerinaParser.SetAssignmentClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startSetAssignmentClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitSetAssignmentClause(BallerinaParser.SetAssignmentClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endSetAssignmentClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSetClause(BallerinaParser.SetClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startSetClauseNode();
    }

    @Override
    public void exitSetClause(BallerinaParser.SetClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endSetClauseNode(getWS(ctx), ctx.getChildCount() / 2);
    }

    @Override
    public void enterStreamingAction(BallerinaParser.StreamingActionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startStreamActionNode(getCurrentPos(ctx), getWS(ctx), diagnosticSrc.pkgID);
    }

    @Override
    public void exitStreamingAction(BallerinaParser.StreamingActionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.endStreamActionNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startPatternStreamingEdgeInputNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String alias = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        this.pkgBuilder.endPatternStreamingEdgeInputNode(getCurrentPos(ctx), getWS(ctx), alias);
    }

    @Override
    public void enterWindowClause(BallerinaParser.WindowClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startWindowClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitWindowClause(BallerinaParser.WindowClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endWindowsClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterWithinClause(BallerinaParser.WithinClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startWithinClause(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitWithinClause(BallerinaParser.WithinClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endWithinClause(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterPatternClause(BallerinaParser.PatternClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startPatternClause(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitPatternClause(BallerinaParser.PatternClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isForAllEvents = ctx.EVERY() != null;
        boolean isWithinClauseAvailable = ctx.withinClause() != null;

        this.pkgBuilder.endPatternClause(isForAllEvents, isWithinClauseAvailable, getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startPatternStreamingInputNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean followedByAvailable = ctx.FOLLOWED() != null && ctx.BY() != null;
        boolean enclosedInParenthesis = ctx.LEFT_PARENTHESIS() != null && ctx.RIGHT_PARENTHESIS() != null;
        boolean andWithNotAvailable = ctx.NOT() != null && ctx.AND() != null;
        boolean forWithNotAvailable = ctx.simpleLiteral() != null;
        boolean onlyAndAvailable = ctx.AND() != null && ctx.NOT() == null && ctx.FOR() == null;
        boolean onlyOrAvailable = ctx.OR() != null && ctx.NOT() == null && ctx.FOR() == null;
        boolean commaSeparated = ctx.COMMA() != null;
        this.pkgBuilder.endPatternStreamingInputNode(getCurrentPos(ctx), getWS(ctx), followedByAvailable,
                enclosedInParenthesis, andWithNotAvailable, forWithNotAvailable, onlyAndAvailable,
                onlyOrAvailable, commaSeparated);
    }

    @Override
    public void enterStreamingInput(BallerinaParser.StreamingInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startStreamingInputNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitStreamingInput(BallerinaParser.StreamingInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String alias = null;
        if (ctx.alias != null) {
            alias = ctx.alias.getText();
        }

        this.pkgBuilder.endStreamingInputNode(alias, getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterJoinStreamingInput(BallerinaParser.JoinStreamingInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startJoinStreamingInputNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitJoinStreamingInput(BallerinaParser.JoinStreamingInputContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean unidirectionalJoin = ctx.UNIDIRECTIONAL() != null;

        if (!unidirectionalJoin) {
            String joinType = (ctx).children.get(0).getText();
            this.pkgBuilder.endJoinStreamingInputNode(getCurrentPos(ctx), getWS(ctx), false,
                    false, joinType);
        } else {
            if (ctx.getChild(0).getText().equals("unidirectional")) {
                String joinType = (ctx).children.get(1).getText();
                this.pkgBuilder.endJoinStreamingInputNode(getCurrentPos(ctx), getWS(ctx), true,
                        false, joinType);
            } else {
                String joinType = (ctx).children.get(0).getText();
                this.pkgBuilder.endJoinStreamingInputNode(getCurrentPos(ctx), getWS(ctx), false,
                        true, joinType);
            }
        }
    }

    @Override
    public void enterOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startOutputRateLimitNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean isSnapshotOutputRateLimit = false;
        boolean isEventBasedOutputRateLimit = false;
        boolean isFirst = false;
        boolean isLast = false;
        boolean isAll = false;

        if (ctx.SNAPSHOT() != null) {
            isSnapshotOutputRateLimit = true;
        } else {
            if (ctx.EVENTS() != null) {
                isEventBasedOutputRateLimit = true;

                if (ctx.LAST() != null) {
                    isLast = true;
                } else if (ctx.FIRST() != null) {
                    isFirst = true;
                } else if (ctx.LAST() != null) {
                    isAll = true;
                }
            }
        }

        String timescale = null;
        if (ctx.timeScale() != null) {
            timescale = ctx.FIRST().getText();
        }

        this.pkgBuilder.endOutputRateLimitNode(getCurrentPos(ctx), getWS(ctx), isSnapshotOutputRateLimit,
                isEventBasedOutputRateLimit, isFirst, isLast, isAll, timescale, ctx.DecimalIntegerLiteral().getText());
    }

    @Override
    public void enterTableQuery(BallerinaParser.TableQueryContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startTableQueryNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitTableQuery(BallerinaParser.TableQueryContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        boolean isSelectClauseAvailable = ctx.selectClause() != null;
        boolean isOrderByClauseAvailable = ctx.orderByClause() != null;
        boolean isJoinClauseAvailable = ctx.joinStreamingInput() != null;
        boolean isLimitClauseAvailable = ctx.limitClause() != null;
        this.pkgBuilder.endTableQueryNode(isJoinClauseAvailable, isSelectClauseAvailable, isOrderByClauseAvailable,
                isLimitClauseAvailable, getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterStreamingQueryStatement(BallerinaParser.StreamingQueryStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startStreamingQueryStatementNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitStreamingQueryStatement(BallerinaParser.StreamingQueryStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endStreamingQueryStatementNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterForeverStatement(BallerinaParser.ForeverStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startForeverNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitForeverStatement(BallerinaParser.ForeverStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endForeverNode(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDocumentationAttachment(BallerinaParser.DocumentationAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.startDocumentationAttachment(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDocumentationAttachment(BallerinaParser.DocumentationAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.endDocumentationAttachment(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDocumentationTemplateContent(BallerinaParser.DocumentationTemplateContentContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String contentText = ctx.docText() != null ? ctx.docText().getText() : "";
        this.pkgBuilder.setDocumentationAttachmentContent(getCurrentPos(ctx), getWS(ctx), contentText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDocumentationTemplateAttributeDescription
    (BallerinaParser.DocumentationTemplateAttributeDescriptionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String attributeStart = ctx.DocumentationTemplateAttributeStart().getText();
        String docPrefix = attributeStart.substring(0, 1);
        String attributeName = ctx.Identifier() != null ? ctx.Identifier().getText() : "";
        String endText = ctx.docText() != null ? ctx.docText().getText() : "";
        this.pkgBuilder.createDocumentationAttribute(getCurrentPos(ctx), getWS(ctx),
                attributeName, endText, docPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDeprecatedAttachment(BallerinaParser.DeprecatedAttachmentContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String contentText = ctx.deprecatedText() != null ? ctx.deprecatedText().getText() : "";
        this.pkgBuilder.createDeprecatedNode(getCurrentPos(ctx), getWS(ctx), contentText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAwaitExpr(BallerinaParser.AwaitExprContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.createAwaitExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        if (ctx.START() != null) {
            this.pkgBuilder.markLastInvocationAsAsync(getCurrentPos(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterMatchExpression(BallerinaParser.MatchExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startMatchExpression();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitMatchExpression(BallerinaParser.MatchExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endMatchExpression(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitMatchExpressionPatternClause(BallerinaParser.MatchExpressionPatternClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String identifier = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        this.pkgBuilder.addMatchExprPattaern(getCurrentPos(ctx), getWS(ctx), identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitElvisExpression(BallerinaParser.ElvisExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createElvisExpr(getCurrentPos(ctx), getWS(ctx));
    }

    private DiagnosticPos getCurrentPos(ParserRuleContext ctx) {
        int startLine = ctx.getStart().getLine();
        int startCol = ctx.getStart().getCharPositionInLine() + 1;

        int endLine = -1;
        int endCol = -1;
        Token stop = ctx.getStop();
        if (stop != null) {
            endLine = stop.getLine();
            endCol = stop.getCharPositionInLine() + 1;
        }

        return new DiagnosticPos(diagnosticSrc, startLine, endLine, startCol, endCol);
    }

    protected Set<Whitespace> getWS(ParserRuleContext ctx) {
        return null;
    }

    private Stack<String> getTemplateTextFragments(List<TerminalNode> nodes) {
        Stack<String> templateStrFragments = new Stack<>();
        nodes.forEach(node -> {
            if (node == null) {
                templateStrFragments.push(null);
            } else {
                String str = node.getText();
                templateStrFragments.push(str.substring(0, str.length() - 2));
            }
        });
        return templateStrFragments;
    }

    private String getTemplateEndingStr(TerminalNode node) {
        return node == null ? null : node.getText();
    }

    private String getNodeValue(ParserRuleContext ctx, TerminalNode node) {
        String op = ctx.getChild(0).getText();
        String value = node.getText();
        if (op != null && "-".equals(op)) {
            value = "-" + value;
        }
        return value;
    }

    private Long getIntegerLiteral(ParserRuleContext simpleLiteralContext,
                                   BallerinaParser.IntegerLiteralContext integerLiteralContext) {
        if (integerLiteralContext.DecimalIntegerLiteral() != null) {
            return Long.parseLong(getNodeValue(simpleLiteralContext, integerLiteralContext.DecimalIntegerLiteral()));
        } else if (integerLiteralContext.HexIntegerLiteral() != null) {
            return Long.parseLong(getNodeValue(simpleLiteralContext, integerLiteralContext.HexIntegerLiteral())
                    .toLowerCase().replace("0x", ""), 16);
        } else if (integerLiteralContext.OctalIntegerLiteral() != null) {
            return Long.parseLong(getNodeValue(simpleLiteralContext, integerLiteralContext.OctalIntegerLiteral())
                    .replace("0_", ""), 8);
        } else if (integerLiteralContext.BinaryIntegerLiteral() != null) {
            return Long.parseLong(getNodeValue(simpleLiteralContext, integerLiteralContext.BinaryIntegerLiteral())
                    .toLowerCase().replace("0b", ""), 2);
        }
        return null;
    }
}
