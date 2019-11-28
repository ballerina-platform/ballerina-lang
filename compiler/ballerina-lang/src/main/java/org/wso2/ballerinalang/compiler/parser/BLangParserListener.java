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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser.FieldContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser.ObjectTypeNameLabelContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser.StringTemplateContentContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserBaseListener;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringJoiner;

import static org.wso2.ballerinalang.compiler.parser.BLangPackageBuilder.escapeQuotedIdentifier;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_SEALED_ARRAY;
import static org.wso2.ballerinalang.compiler.util.Constants.OPEN_SEALED_ARRAY_INDICATOR;
import static org.wso2.ballerinalang.compiler.util.Constants.UNSEALED_ARRAY_INDICATOR;

/**
 * @since 0.94
 */
public class BLangParserListener extends BallerinaParserBaseListener {
    private static final String KEYWORD_PUBLIC = "public";
    private static final String KEYWORD_KEY = "key";

    private BLangPackageBuilder pkgBuilder;
    private BDiagnosticSource diagnosticSrc;
    private BLangDiagnosticLog dlog;

    private List<String> pkgNameComps;
    private String pkgVersion;
    private boolean isInErrorState = false;

    BLangParserListener(CompilerContext context, CompilationUnitNode compUnit, BDiagnosticSource diagnosticSource) {
        this.pkgBuilder = new BLangPackageBuilder(context, compUnit);
        this.diagnosticSrc = diagnosticSource;
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    @Override
    public void exitParameter(BallerinaParser.ParameterContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addSimpleVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                     getCurrentPos(ctx.Identifier()), false,
                                     ctx.annotationAttachment().size(), ctx.PUBLIC() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFormalParameterList(BallerinaParser.FormalParameterListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFormalParameterList(BallerinaParser.FormalParameterListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endFormalParameterList(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDefaultableParameter(BallerinaParser.DefaultableParameterContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addDefaultableParam(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRestParameter(BallerinaParser.RestParameterContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addRestParam(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                     getCurrentPos(ctx.Identifier()), ctx.annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterTypeName(BallerinaParser.ParameterTypeNameContext ctx) {
        if (isInErrorState) {
            return;
        }

        //TODO setting annotation count 0 as parameters won't adding annotations to parameters.
        this.pkgBuilder.addSimpleVar(getCurrentPos(ctx), getWS(ctx), null, null, false, 0);
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
    public void exitPackageName(BallerinaParser.PackageNameContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgNameComps = new ArrayList<>();
        ctx.Identifier().forEach(e -> pkgNameComps.add(e.getText()));
        this.pkgVersion = ctx.version() != null ? ctx.version().versionPattern().getText() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx) {
        if (isInErrorState) {
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
    public void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }
        final DiagnosticPos serviceDefPos = getCurrentPos(ctx);
        final String serviceVarName = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        final DiagnosticPos varPos =
                ctx.Identifier() != null ? getCurrentPos(ctx.Identifier()) : serviceDefPos;
        this.pkgBuilder.endServiceDef(serviceDefPos, getWS(ctx), serviceVarName, varPos, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterServiceBody(BallerinaParser.ServiceBodyContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startServiceDef(getCurrentPos(ctx));
        this.pkgBuilder.startObjectType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitServiceBody(BallerinaParser.ServiceBodyContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isFieldAnalyseRequired = (ctx.parent.parent instanceof BallerinaParser.GlobalVariableDefinitionContext
                || ctx.parent.parent instanceof BallerinaParser.ReturnParameterContext)
                || ctx.parent.parent.parent.parent instanceof BallerinaParser.TypeDefinitionContext;
        this.pkgBuilder
                .addObjectType(getCurrentPos(ctx), getWS(ctx), isFieldAnalyseRequired, false, false, false, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endCallableUnitBody(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitExternalFunctionBody(BallerinaParser.ExternalFunctionBodyContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endExternalFunctionBody(ctx.annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }
        // Since the function definition's super parent is CompilationUnit and it is the only super parent for
        // FunctionDefinition, following cast is safe.
        int annotCount = ((BallerinaParser.CompilationUnitContext) ctx.parent.parent).annotationAttachment().size();
        this.pkgBuilder.startFunctionDef(annotCount, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean publicFunc = ctx.PUBLIC() != null;
        boolean remoteFunc = ctx.REMOTE() != null;
        boolean nativeFunc = ctx.externalFunctionBody() != null;
        boolean bodyExists = ctx.callableUnitBody() != null;
        boolean privateFunc = ctx.PRIVATE() != null;

        this.pkgBuilder.endFunctionDef(getCurrentPos(ctx), getWS(ctx), publicFunc, remoteFunc, nativeFunc, privateFunc,
                                       bodyExists, false);
    }

    @Override
    public void enterLambdaFunction(BallerinaParser.LambdaFunctionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startLambdaFunctionDef(diagnosticSrc.pkgID);
    }

    @Override
    public void exitLambdaFunction(BallerinaParser.LambdaFunctionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addLambdaFunctionDef(getCurrentPos(ctx), getWS(ctx), ctx.formalParameterList() != null,
                ctx.lambdaReturnParameter() != null,
                ctx.formalParameterList() != null && ctx.formalParameterList().restParameter() != null);
    }

    @Override
    public void enterArrowFunction(BallerinaParser.ArrowFunctionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    @Override
    public void exitArrowFunctionExpression(BallerinaParser.ArrowFunctionExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addArrowFunctionDef(getCurrentPos(ctx), getWS(ctx), diagnosticSrc.pkgID);
    }

    @Override
    public void exitArrowParam(BallerinaParser.ArrowParamContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addVarWithoutType(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                          getCurrentPos(ctx.Identifier()), false, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endCallableUnitSignature(getCurrentPos(ctx), getWS(ctx), ctx.anyIdentifierName().getText(),
                getCurrentPos(ctx.anyIdentifierName()), ctx.formalParameterList() != null,
                ctx.returnParameter() != null, ctx.formalParameterList() != null
                        && ctx.formalParameterList().restParameter() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFiniteType(BallerinaParser.FiniteTypeContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endFiniteType(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTypeDefinition(BallerinaParser.TypeDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean publicObject = ctx.PUBLIC() != null;
        this.pkgBuilder.endTypeDefinition(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                          getCurrentPos(ctx.Identifier()), publicObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterObjectBody(BallerinaParser.ObjectBodyContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startObjectType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitObjectBody(BallerinaParser.ObjectBodyContext ctx) {
        if (isInErrorState) {
            return;
        }

        // When ObjectBody's parent's parent is not a FiniteTypeUnit then this is an anonymous object.
        // It's bit difficult to differentiate between Object type definition and an anonymous object
        // within a union type since parent lineage is similar in both cases.
        // Only difference is that in object type definition, object body's parent's parent does not have siblings.
        boolean isAnonymous = !(ctx.parent.parent instanceof BallerinaParser.FiniteTypeUnitContext)
                || (ctx.parent.parent instanceof BallerinaParser.FiniteTypeUnitContext
                    && ctx.parent.parent.parent instanceof BallerinaParser.FiniteTypeContext
                    && ctx.parent.parent.parent.getChildCount() > 1);

        boolean isFieldAnalyseRequired =
                (ctx.parent.parent instanceof BallerinaParser.GlobalVariableDefinitionContext ||
                        ctx.parent.parent instanceof BallerinaParser.ReturnParameterContext) ||
                        ctx.parent.parent.parent.parent instanceof BallerinaParser.TypeDefinitionContext;
        boolean isAbstract = ((ObjectTypeNameLabelContext) ctx.parent).ABSTRACT() != null;
        boolean isClient = ((ObjectTypeNameLabelContext) ctx.parent).CLIENT() != null;
        this.pkgBuilder.addObjectType(getCurrentPos(ctx), getWS(ctx), isFieldAnalyseRequired, isAnonymous, isAbstract,
                isClient, false);
    }

    @Override
    public void exitTypeReference(BallerinaParser.TypeReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addTypeReference(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        DiagnosticPos currentPos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);
        String name = ctx.Identifier().getText();
        DiagnosticPos identifierPos = getCurrentPos(ctx.Identifier());
        boolean exprAvailable = ctx.expression() != null;
        boolean isOptional = ctx.QUESTION_MARK() != null;
        this.pkgBuilder.addFieldVariable(currentPos, ws, name, identifierPos, exprAvailable,
                                         ctx.annotationAttachment().size(), false, isOptional);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitObjectFieldDefinition(BallerinaParser.ObjectFieldDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        DiagnosticPos currentPos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);
        String name = ctx.Identifier().getText();
        DiagnosticPos identifierPos = getCurrentPos(ctx.Identifier());
        boolean exprAvailable = ctx.expression() != null;

        int annotationCount = ctx.annotationAttachment().size();

        boolean isPrivate = ctx.PRIVATE() != null;
        boolean isPublic = ctx.PUBLIC() != null;

        this.pkgBuilder.addObjectFieldVariable(currentPos, ws, name, identifierPos, exprAvailable, annotationCount,
                                               isPrivate, isPublic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startObjectFunctionDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean publicFunc = ctx.PUBLIC() != null;
        boolean isPrivate = ctx.PRIVATE() != null;
        boolean remoteFunc = ctx.REMOTE() != null;
        boolean resourceFunc = ctx.RESOURCE() != null;
        boolean nativeFunc = ctx.externalFunctionBody() != null;
        boolean bodyExists = ctx.callableUnitBody() != null;
        boolean markdownDocExists = ctx.documentationString() != null;
        this.pkgBuilder.endObjectAttachedFunctionDef(getCurrentPos(ctx), getWS(ctx), publicFunc, isPrivate, remoteFunc,
                                                     resourceFunc, nativeFunc, bodyExists, markdownDocExists,
                                                     ctx.annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startAnnotationDef(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean publicAnnotation = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        boolean isTypeAttached = ctx.typeName() != null;
        boolean isConst = ctx.CONST() != null;
        this.pkgBuilder.endAnnotationDef(getWS(ctx), ctx.Identifier().getText(),
                getCurrentPos(ctx.Identifier()), publicAnnotation, isTypeAttached, isConst);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean isPublic = ctx.PUBLIC() != null;
        boolean isTypeAvailable = ctx.typeName() != null;
        this.pkgBuilder.addConstant(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                    getCurrentPos(ctx.Identifier()), isPublic, isTypeAvailable);
    }

    @Override
    public void exitConstDivMulModExpression(BallerinaParser.ConstDivMulModExpressionContext ctx) {

        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitConstAddSubExpression(BallerinaParser.ConstAddSubExpressionContext ctx) {

        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitConstGroupExpression(BallerinaParser.ConstGroupExpressionContext ctx) {

        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createGroupExpression(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean isPublic = ctx.PUBLIC() != null;
        boolean isFinal = ctx.FINAL() != null;
        boolean isDeclaredWithVar = ctx.VAR() != null;
        boolean isExpressionAvailable = ctx.expression() != null;
        boolean isListenerVar = ctx.LISTENER() != null;
        this.pkgBuilder.addGlobalVariable(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                          getCurrentPos(ctx.Identifier()), isPublic, isFinal,
                                          isDeclaredWithVar, isExpressionAvailable, isListenerVar);
    }

    @Override
    public void exitAttachmentPoint(BallerinaParser.AttachmentPointContext ctx) {
        if (isInErrorState) {
            return;
        }

        AttachPoint attachPoint;
        if (ctx.dualAttachPoint() != null) {
            if (ctx.dualAttachPoint().SOURCE() != null) {
                attachPoint = AttachPoint.getAttachmentPoint(ctx.dualAttachPoint().dualAttachPointIdent().getText(),
                                                             true);
            } else {
                attachPoint = AttachPoint.getAttachmentPoint(ctx.getText(), false);
            }
        } else {
            // source-only-attach-point
            attachPoint = AttachPoint.getAttachmentPoint(
                    ctx.sourceOnlyAttachPoint().sourceOnlyAttachPointIdent().getText(), true);
        }
        this.pkgBuilder.addAttachPoint(attachPoint, getWS(ctx));
    }

    @Override
    public void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startWorker(diagnosticSrc.pkgID);
    }

    @Override
    public void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx) {
        if (isInErrorState) {
            return;
        }

        String workerName = null;
        if (ctx.workerDefinition() != null) {
            workerName = escapeQuotedIdentifier(ctx.workerDefinition().Identifier().getText());
        }
        boolean retParamsAvail = ctx.workerDefinition().returnParameter() != null;
        int numAnnotations = ctx.annotationAttachment().size();
        this.pkgBuilder.addWorker(getCurrentPos(ctx), getWS(ctx), workerName, retParamsAvail, numAnnotations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.attachWorkerWS(getWS(ctx));
    }

    @Override
    public void exitArrayTypeNameLabel(BallerinaParser.ArrayTypeNameLabelContext ctx) {
        if (isInErrorState) {
            return;
        }

        int index = 1;
        int dimensions = 0;
        List<Integer> sizes = new ArrayList<>();
        List<ParseTree> children = ctx.children;
        while (index < children.size()) {
            if (children.get(index).getText().equals("[")) {
                if (children.get(index + 1).getText().equals("]")) {
                    sizes.add(UNSEALED_ARRAY_INDICATOR);
                    index += 2;
                } else if (children.get(index + 1).getText().equals(OPEN_SEALED_ARRAY)) {
                    sizes.add(OPEN_SEALED_ARRAY_INDICATOR);
                    index += 1;
                } else {
                    sizes.add(Integer.parseInt(children.get(index + 1).getText()));
                    index += 1;
                }
                dimensions++;
            } else {
                index++;
            }
        }
        Collections.reverse(sizes);
        this.pkgBuilder.addArrayType(
                getCurrentPos(ctx), getWS(ctx), dimensions, sizes.stream().mapToInt(val -> val).toArray());
    }

    @Override
    public void exitUnionTypeNameLabel(BallerinaParser.UnionTypeNameLabelContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addUnionType(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitTupleTypeNameLabel(BallerinaParser.TupleTypeNameLabelContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.addTupleType(getCurrentPos(ctx), getWS(ctx), ctx.tupleTypeDescriptor().typeName().size(),
                ctx.tupleTypeDescriptor().tupleRestDescriptor() != null);
    }

    @Override
    public void exitNullableTypeNameLabel(BallerinaParser.NullableTypeNameLabelContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.markTypeNodeAsNullable(getWS(ctx));
    }

    @Override
    public void exitGroupTypeNameLabel(BallerinaParser.GroupTypeNameLabelContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.markTypeNodeAsGrouped(getWS(ctx));
    }

    @Override
    public void enterInclusiveRecordTypeDescriptor(BallerinaParser.InclusiveRecordTypeDescriptorContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startRecordType();
    }

    @Override
    public void exitInclusiveRecordTypeDescriptor(BallerinaParser.InclusiveRecordTypeDescriptorContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isAnonymous = !(ctx.parent.parent instanceof BallerinaParser.FiniteTypeUnitContext);

        boolean isFieldAnalyseRequired =
                (ctx.parent.parent instanceof BallerinaParser.GlobalVariableDefinitionContext ||
                        ctx.parent.parent instanceof BallerinaParser.ReturnParameterContext) ||
                        ctx.parent.parent.parent.parent instanceof BallerinaParser.TypeDefinitionContext;

        this.pkgBuilder.addRecordType(getCurrentPos(ctx), getWS(ctx), isFieldAnalyseRequired, isAnonymous, false,
                                      false);
    }

    @Override
    public void enterExclusiveRecordTypeDescriptor(BallerinaParser.ExclusiveRecordTypeDescriptorContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startRecordType();
    }

    @Override
    public void exitExclusiveRecordTypeDescriptor(BallerinaParser.ExclusiveRecordTypeDescriptorContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isAnonymous = !(ctx.parent.parent instanceof BallerinaParser.FiniteTypeUnitContext);

        boolean isFieldAnalyseRequired =
                (ctx.parent.parent instanceof BallerinaParser.GlobalVariableDefinitionContext ||
                        ctx.parent.parent instanceof BallerinaParser.ReturnParameterContext) ||
                        ctx.parent.parent.parent.parent instanceof BallerinaParser.TypeDefinitionContext;

        boolean hasRestField = ctx.recordRestFieldDefinition() != null;

        this.pkgBuilder.addRecordType(getCurrentPos(ctx), getWS(ctx), isFieldAnalyseRequired, isAnonymous,
                                      hasRestField, true);
    }

    @Override
    public void exitSimpleTypeName(BallerinaParser.SimpleTypeNameContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addUserDefineType(getWS(ctx));
    }

    @Override
    public void exitValueTypeName(BallerinaParser.ValueTypeNameContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addValueType(getCurrentPos(ctx), getWS(ctx), ctx.getText());
    }

    @Override
    public void exitBuiltInReferenceTypeName(BallerinaParser.BuiltInReferenceTypeNameContext ctx) {
        if (isInErrorState) {
            return;
        }
        if (ctx.functionTypeName() != null) {
            return;
        }
        if (ctx.errorTypeName() != null) {
            return;
        }

        String typeName = ctx.getChild(0).getText();
        DiagnosticPos pos = getCurrentPos(ctx);

        if (ctx.typeName() != null) {
            this.pkgBuilder.addConstraintTypeWithTypeName(pos, getWS(ctx), typeName);
        } else {
            this.pkgBuilder.addBuiltInReferenceType(pos, getWS(ctx), typeName);
        }
    }

    @Override
    public void exitErrorTypeName(BallerinaParser.ErrorTypeNameContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean reasonTypeExists = !ctx.typeName().isEmpty();
        boolean detailsTypeExists = ctx.typeName().size() > 1;
        boolean isAnonymous = !(ctx.parent.parent.parent.parent.parent.parent
                                        instanceof BallerinaParser.FiniteTypeContext) && reasonTypeExists;
        this.pkgBuilder.addErrorType(getCurrentPos(ctx), getWS(ctx), reasonTypeExists, detailsTypeExists, isAnonymous);
    }

    @Override
    public void exitFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx) {
        if (isInErrorState) {
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

        this.pkgBuilder.addFunctionType(getCurrentPos(ctx), getWS(ctx), paramsAvail, retParamAvail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startAnnotationAttachment(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.setAnnotationAttachmentName(getWS(ctx), ctx.recordLiteral() != null,
                getCurrentPos(ctx), false);
    }

    @Override
    public void exitErrorBindingPattern(BallerinaParser.ErrorBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        // Error binding pattern using indirect error constructor.
        if (ctx.typeName() != null) {
            if (ctx.errorFieldBindingPatterns().errorRestBindingPattern() != null) {
                String restIdName = ctx.errorFieldBindingPatterns().errorRestBindingPattern().Identifier().getText();
                DiagnosticPos restPos = getCurrentPos(ctx.errorFieldBindingPatterns().errorRestBindingPattern());
                this.pkgBuilder.addErrorVariable(getCurrentPos(ctx), getWS(ctx), restIdName, restPos);
            } else {
                this.pkgBuilder.addErrorVariable(getCurrentPos(ctx), getWS(ctx), null, null);
            }
            return;
        }
        String reasonIdentifier = ctx.Identifier().getText();
        DiagnosticPos currentPos = getCurrentPos(ctx);

        String restIdentifier = null;
        DiagnosticPos restParamPos = null;
        if (ctx.errorRestBindingPattern() != null) {
            restIdentifier = ctx.errorRestBindingPattern().Identifier().getText();
            restParamPos = getCurrentPos(ctx.errorRestBindingPattern());
        }

        this.pkgBuilder.addErrorVariable(currentPos, getWS(ctx), reasonIdentifier, restIdentifier, false, false,
                restParamPos);
    }

    @Override
    public void enterErrorBindingPattern(BallerinaParser.ErrorBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startErrorBindingNode();
    }

    @Override
    public void enterErrorMatchPattern(BallerinaParser.ErrorMatchPatternContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startErrorBindingNode();
    }

    @Override
    public void exitSimpleMatchPattern(BallerinaParser.SimpleMatchPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endSimpleMatchPattern(getWS(ctx));
    }

    @Override
    public void exitErrorArgListMatchPattern(BallerinaParser.ErrorArgListMatchPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        String restIdentifier = null;
        DiagnosticPos restParamPos = null;
        if (ctx.restMatchPattern() != null) {
            restIdentifier = ctx.restMatchPattern().Identifier().getText();
            restParamPos = getCurrentPos(ctx.restMatchPattern());
        }

        String reasonIdentifier = null;
        boolean reasonVar = false;
        boolean constReasonMatchPattern = false;
        if (ctx.simpleMatchPattern() != null) {
            reasonVar = ctx.simpleMatchPattern().VAR() != null;
            if (ctx.simpleMatchPattern().Identifier() != null) {
                reasonIdentifier = ctx.simpleMatchPattern().Identifier().getText();
            } else {
                reasonIdentifier = ctx.simpleMatchPattern().QuotedStringLiteral().getText();
                constReasonMatchPattern = true;
            }
        }

        this.pkgBuilder.addErrorVariable(getCurrentPos(ctx), getWS(ctx), reasonIdentifier,
                restIdentifier, reasonVar, constReasonMatchPattern, restParamPos);
    }

    @Override
    public void exitErrorMatchPattern(BallerinaParser.ErrorMatchPatternContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean isIndirectErrorMatchPatern = ctx.typeName() != null;
        this.pkgBuilder.endErrorMatchPattern(getWS(ctx), isIndirectErrorMatchPatern);
    }

    @Override
    public void exitErrorDetailBindingPattern(BallerinaParser.ErrorDetailBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        String bindingVarName = null;
        if (ctx.bindingPattern() != null && ctx.bindingPattern().Identifier() != null) {
            bindingVarName = ctx.bindingPattern().Identifier().getText();
        }
        this.pkgBuilder.addErrorDetailBinding(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                bindingVarName);
    }

    @Override
    public void exitErrorRefBindingPattern(BallerinaParser.ErrorRefBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        int numNamedArgs = ctx.errorNamedArgRefPattern().size();
        boolean reasonRefAvailable = ctx.variableReference() != null;

        boolean restPatternAvailable = ctx.errorRefRestPattern() != null;

        boolean indirectErrorRefPattern = ctx.typeName() != null;

        this.pkgBuilder.addErrorVariableReference(getCurrentPos(ctx), getWS(ctx),
                numNamedArgs, reasonRefAvailable, restPatternAvailable, indirectErrorRefPattern);
    }

    @Override
    public void exitErrorNamedArgRefPattern(BallerinaParser.ErrorNamedArgRefPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addNamedArgument(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
    }

    @Override
    public void exitListBindingPattern(BallerinaParser.ListBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean restBindingAvailable = ctx.restBindingPattern() != null;
        this.pkgBuilder.addTupleVariable(getCurrentPos(ctx), getWS(ctx), ctx.bindingPattern().size(),
                restBindingAvailable);
    }

    @Override
    public void exitListRefBindingPattern(BallerinaParser.ListRefBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean restPatternAvailable = ctx.listRefRestPattern() != null;
        this.pkgBuilder.addTupleVariableReference(getCurrentPos(ctx), getWS(ctx), ctx.bindingRefPattern().size(),
                restPatternAvailable);
    }

    @Override
    public void enterRecordBindingPattern(BallerinaParser.RecordBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startRecordVariableList();
    }

    @Override
    public void exitRecordBindingPattern(BallerinaParser.RecordBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean hasRestBindingPattern = ctx.entryBindingPattern().restBindingPattern() != null;

        this.pkgBuilder.addRecordVariable(getCurrentPos(ctx), getWS(ctx), hasRestBindingPattern);
    }

    @Override
    public void enterRecordRefBindingPattern(BallerinaParser.RecordRefBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startRecordVariableReferenceList();
    }

    @Override
    public void exitRecordRefBindingPattern(BallerinaParser.RecordRefBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean hasRestBindingPattern = ctx.entryRefBindingPattern().restRefBindingPattern() != null;

        this.pkgBuilder.addRecordVariableReference(getCurrentPos(ctx), getWS(ctx), hasRestBindingPattern);
    }

    @Override
    public void exitBindingPattern(BallerinaParser.BindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        if ((ctx.Identifier() != null) && ((ctx.parent instanceof BallerinaParser.ListBindingPatternContext)
                || (ctx.parent instanceof BallerinaParser.FieldBindingPatternContext)
                || (ctx.parent instanceof BallerinaParser.MatchPatternClauseContext))) {
            this.pkgBuilder.addBindingPatternMemberVariable(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                                            getCurrentPos(ctx.Identifier()));
        } else if (ctx.Identifier() != null) {
            this.pkgBuilder.addBindingPatternNameWhitespace(getWS(ctx));
        }
    }

    @Override
    public void exitFieldBindingPattern(BallerinaParser.FieldBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addFieldBindingMemberVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                                 getCurrentPos(ctx.Identifier()),
                                                 ctx.bindingPattern() != null);
    }

    @Override
    public void exitFieldRefBindingPattern(BallerinaParser.FieldRefBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addFieldRefBindingMemberVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                ctx.bindingRefPattern() != null);
    }

    @Override
    public void exitRestBindingPattern(BallerinaParser.RestBindingPatternContext ctx) {
        if (isInErrorState) {
            return;
        }

        if (ctx.Identifier() != null) {
            this.pkgBuilder.addBindingPatternMemberVariable(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                                            getCurrentPos(ctx.Identifier()));
        }
    }

    @Override
    public void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isFinal = ctx.FINAL() != null;
        boolean isDeclaredWithVar = ctx.VAR() != null;
        boolean isExpressionAvailable = ctx.expression() != null;

        if (ctx.Identifier() != null) {
            this.pkgBuilder.addSimpleVariableDefStatement(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                                          getCurrentPos(ctx.Identifier()),
                                                          isFinal, isExpressionAvailable, isDeclaredWithVar);
        } else if (ctx.bindingPattern().Identifier() != null) {
            this.pkgBuilder.addSimpleVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                                                          ctx.bindingPattern().Identifier().getText(),
                                                          getCurrentPos(ctx.bindingPattern().Identifier()),
                                                          isFinal, isExpressionAvailable, isDeclaredWithVar);
        } else if (ctx.bindingPattern().structuredBindingPattern().recordBindingPattern() != null) {
            this.pkgBuilder.addRecordVariableDefStatement(getCurrentPos(ctx), getWS(ctx), isFinal, isDeclaredWithVar);
        } else if (ctx.bindingPattern().structuredBindingPattern().errorBindingPattern() != null) {
            this.pkgBuilder.addErrorVariableDefStatement(getCurrentPos(ctx), getWS(ctx), isFinal, isDeclaredWithVar);
        } else if (ctx.bindingPattern().structuredBindingPattern().listBindingPattern() != null) {
            this.pkgBuilder.addTupleVariableDefStatement(getCurrentPos(ctx), getWS(ctx), isFinal, isDeclaredWithVar);
        }
    }

    @Override
    public void enterRecordLiteral(BallerinaParser.RecordLiteralContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startMapStructLiteral();
    }

    @Override
    public void exitRecordLiteral(BallerinaParser.RecordLiteralContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addMapStructLiteral(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitRecordKeyValue(BallerinaParser.RecordKeyValueContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addKeyValueRecord(getWS(ctx), ctx.recordKey().LEFT_BRACKET() != null);
    }

    @Override
    public void exitRecordKey(BallerinaParser.RecordKeyContext ctx) {
        if (isInErrorState) {
            return;
        }

        // If the key is an expression, they are added to the model
        // from their respective listener methods
        if (ctx.Identifier() != null) {
            DiagnosticPos pos = getCurrentPos(ctx);
            this.pkgBuilder.addNameReference(pos, getWS(ctx), null, ctx.Identifier().getText());
            this.pkgBuilder.createSimpleVariableReference(pos, getWS(ctx));
        } else if (ctx.LEFT_BRACKET() != null) {
            this.pkgBuilder.addRecordKeyWS(getWS(ctx));
        }
    }

    @Override
    public void enterTableLiteral(BallerinaParser.TableLiteralContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startTableLiteral();
    }

    @Override
    public void exitTableColumnDefinition(BallerinaParser.TableColumnDefinitionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.endTableColumnDefinition(getWS(ctx));
    }

    @Override
    public void exitTableColumn(BallerinaParser.TableColumnContext ctx) {
        if (isInErrorState) {
            return;
        }

        String columnName;
        int childCount = ctx.getChildCount();
        if (childCount == 2) {
            boolean keyColumn = KEYWORD_KEY.equals(ctx.getChild(0).getText());
            if (keyColumn) {
                columnName = escapeQuotedIdentifier(ctx.getChild(1).getText());
                this.pkgBuilder.addTableColumn(columnName, getCurrentPos(ctx), getWS(ctx));
                this.pkgBuilder.markPrimaryKeyColumn(columnName);
            } else {
                DiagnosticPos pos = getCurrentPos(ctx);
                dlog.error(pos, DiagnosticCode.TABLE_KEY_EXPECTED);
            }
        } else {
            columnName = escapeQuotedIdentifier(ctx.getChild(0).getText());
            this.pkgBuilder.addTableColumn(columnName, getCurrentPos(ctx), getWS(ctx));
        }
    }

    @Override
    public void exitTableDataArray(BallerinaParser.TableDataArrayContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endTableDataArray(getWS(ctx));
    }

    @Override
    public void exitTableDataList(BallerinaParser.TableDataListContext ctx) {
        if (isInErrorState) {
            return;
        }
        if (ctx.expressionList() != null) {
            this.pkgBuilder.endTableDataRow(getWS(ctx));
        }
    }

    @Override
    public void exitTableData(BallerinaParser.TableDataContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.endTableDataList(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitTableLiteral(BallerinaParser.TableLiteralContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addTableLiteral(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitListConstructorExpr(BallerinaParser.ListConstructorExprContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean argsAvailable = ctx.expressionList() != null;
        this.pkgBuilder.addListConstructorExpression(getCurrentPos(ctx), getWS(ctx), argsAvailable);
    }

    @Override
    public void exitTypeInitExpr(BallerinaParser.TypeInitExprContext ctx) {
        if (isInErrorState) {
            return;
        }

        String initName = ctx.NEW().getText();
        boolean typeAvailable = ctx.userDefineTypeName() != null;
        boolean argsAvailable = ctx.invocationArgList() != null;
        this.pkgBuilder.addTypeInitExpression(getCurrentPos(ctx), getWS(ctx), initName, typeAvailable, argsAvailable);
    }

    @Override
    public void exitServiceConstructorExpression(BallerinaParser.ServiceConstructorExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        final DiagnosticPos serviceDefPos = getCurrentPos(ctx);
        final String serviceVarName = null;
        final DiagnosticPos varPos = serviceDefPos;
        this.pkgBuilder.endServiceDef(serviceDefPos, getWS(ctx), serviceVarName, varPos, true,
                                      ctx.serviceConstructorExpr().annotationAttachment().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addAssignmentStatement(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitListDestructuringStatement(BallerinaParser.ListDestructuringStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addTupleDestructuringStatement(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitRecordDestructuringStatement(BallerinaParser.RecordDestructuringStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addRecordDestructuringStatement(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitErrorDestructuringStatement(BallerinaParser.ErrorDestructuringStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addErrorDestructuringStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCompoundAssignmentStatement(BallerinaParser.CompoundAssignmentStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        String compoundOperatorText = ctx.compoundOperator().getText();
        String operator = compoundOperatorText.substring(0, compoundOperatorText.length() - 1);
        this.pkgBuilder.addCompoundAssignmentStatement(getCurrentPos(ctx), getWS(ctx), operator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCompoundOperator(BallerinaParser.CompoundOperatorContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addCompoundOperator(getWS(ctx));
    }

    @Override
    public void enterVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startExprNodeList();
    }

    @Override
    public void exitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endExprNodeList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startIfElseNode(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endIfElseNode(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitIfClause(BallerinaParser.IfClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addIfBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addElseIfBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitElseClause(BallerinaParser.ElseClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addElseBlock(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterMatchStatement(BallerinaParser.MatchStatementContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createMatchNode(getCurrentPos(ctx));
    }

    @Override
    public void exitMatchStatement(BallerinaParser.MatchStatementContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.completeMatchNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startMatchStmtPattern();
    }

    @Override
    public void exitMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        if (ctx.bindingPattern() != null || ctx.errorMatchPattern() != null) {
            boolean isTypeGuardPresent = ctx.IF() != null;
            this.pkgBuilder.addMatchStmtStructuredBindingPattern(getCurrentPos(ctx), getWS(ctx), isTypeGuardPresent);
            return;
        }

        this.pkgBuilder.addMatchStmtStaticBindingPattern(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterForeachStatement(BallerinaParser.ForeachStatementContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startForeachStatement();
    }

    @Override
    public void exitForeachStatement(BallerinaParser.ForeachStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isDeclaredWithVar = ctx.VAR() != null;

        if (ctx.bindingPattern().Identifier() != null) {
            String identifier = ctx.bindingPattern().Identifier().getText();
            DiagnosticPos identifierPos = getCurrentPos(ctx.bindingPattern().Identifier());
            this.pkgBuilder.addForeachStatementWithSimpleVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                                                                              identifier, identifierPos,
                                                                              isDeclaredWithVar);
        } else if (ctx.bindingPattern().structuredBindingPattern().recordBindingPattern() != null) {
            this.pkgBuilder.addForeachStatementWithRecordVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                    isDeclaredWithVar);
        }  else if (ctx.bindingPattern().structuredBindingPattern().errorBindingPattern() != null) {
            this.pkgBuilder.addForeachStatementWithErrorVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                    isDeclaredWithVar);
        } else {
            this.pkgBuilder.addForeachStatementWithTupleVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                    isDeclaredWithVar);
        }
    }

    @Override
    public void exitIntRangeExpression(BallerinaParser.IntRangeExpressionContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startWhileStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addWhileStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitContinueStatement(BallerinaParser.ContinueStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addContinueStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitBreakStatement(BallerinaParser.BreakStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addBreakStatement(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startForkJoinStmt();
    }

    @Override
    public void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addForkJoinStmt(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startTryCatchFinallyStmt();
    }

    @Override
    public void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addTryCatchFinallyStmt(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterCatchClauses(BallerinaParser.CatchClausesContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addTryClause(getCurrentPos(ctx));
    }

    @Override
    public void enterCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startCatchClause();
    }

    @Override
    public void exitCatchClause(BallerinaParser.CatchClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        String paramName = ctx.Identifier().getText();
        this.pkgBuilder.addCatchClause(getCurrentPos(ctx), getWS(ctx), paramName);
    }

    @Override
    public void enterFinallyClause(BallerinaParser.FinallyClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startFinallyBlock();
    }

    @Override
    public void exitFinallyClause(BallerinaParser.FinallyClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addFinallyBlock(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addThrowStmt(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitPanicStatement(BallerinaParser.PanicStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addPanicStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addReturnStatement(this.getCurrentPos(ctx), getWS(ctx), ctx.expression() != null);
    }

    @Override
    public void exitWorkerReceiveExpression(BallerinaParser.WorkerReceiveExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        String workerName = ctx.peerWorker().DEFAULT() != null ?
                ctx.peerWorker().DEFAULT().getText() : ctx.peerWorker().workerName().getText();

        this.pkgBuilder.addWorkerReceiveExpr(getCurrentPos(ctx), getWS(ctx), workerName, ctx.expression() != null);
    }

    @Override
    public void exitFlushWorker(BallerinaParser.FlushWorkerContext ctx) {
        if (isInErrorState) {
            return;
        }
        String workerName = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        this.pkgBuilder.addWorkerFlushExpr(getCurrentPos(ctx), getWS(ctx), workerName);
    }

    @Override
    public void exitWorkerSendAsyncStatement(BallerinaParser.WorkerSendAsyncStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        String workerName = ctx.peerWorker().DEFAULT() != null ?
                ctx.peerWorker().DEFAULT().getText() : ctx.peerWorker().workerName().getText();

        this.pkgBuilder.addWorkerSendStmt(getCurrentPos(ctx), getWS(ctx), workerName, ctx.expression().size() > 1);
    }

    @Override
    public void exitWorkerSendSyncExpression(BallerinaParser.WorkerSendSyncExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        String workerName = ctx.peerWorker().DEFAULT() != null ?
                ctx.peerWorker().DEFAULT().getText() : ctx.peerWorker().workerName().getText();

        this.pkgBuilder.addWorkerSendSyncExpr(getCurrentPos(ctx), getWS(ctx), workerName);
    }

    @Override
    public void exitWaitExpression(BallerinaParser.WaitExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        // Wait for all
        if (ctx.waitForCollection() != null) {
            this.pkgBuilder.handleWaitForAll(getCurrentPos(ctx), getWS(ctx));
        } else {
            // Wait for Any or Wait for one
            this.pkgBuilder.handleWait(getCurrentPos(ctx), getWS(ctx));
        }
    }

    @Override
    public void enterWaitForCollection(BallerinaParser.WaitForCollectionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startWaitForAll();
    }

    @Override
    public void exitWaitKeyValue(BallerinaParser.WaitKeyValueContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean containsExpr = ctx.expression() != null;
        this.pkgBuilder.addKeyValueToWaitForAll(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                                                containsExpr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }
        boolean isSingleAttrRef = ctx.xmlAttrib().expression() != null;
        this.pkgBuilder.createXmlAttributesRefExpr(getCurrentPos(ctx), getWS(ctx), isSingleAttrRef);
    }

    @Override
    public void exitSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createSimpleVariableReference(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitStringFunctionInvocationReference(BallerinaParser.StringFunctionInvocationReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        TerminalNode node = ctx.QuotedStringLiteral();
        DiagnosticPos pos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);

        String actualText = node.getText();
        actualText = actualText.substring(1, actualText.length() - 1);
        actualText = StringEscapeUtils.unescapeJava(actualText);

        this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.STRING, actualText, node.getText());

        boolean argsAvailable = ctx.invocation().invocationArgList() != null;
        BallerinaParser.AnyIdentifierNameContext identifierContext = ctx.invocation().anyIdentifierName();
        String invocation = identifierContext.getText();
        this.pkgBuilder.createInvocationNode(getCurrentPos(ctx), getWS(ctx), invocation, argsAvailable,
                getCurrentPos(identifierContext));
    }

    @Override
    public void exitFunctionInvocation(BallerinaParser.FunctionInvocationContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean argsAvailable = ctx.invocationArgList() != null;
        this.pkgBuilder.createFunctionInvocation(getCurrentPos(ctx), getWS(ctx), argsAvailable);
    }

    @Override
    public void exitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        FieldContext field = ctx.field();
        String fieldName;
        DiagnosticPos fieldNamePos;
        FieldKind fieldType;
        if (field.Identifier() != null) {
            fieldName = field.Identifier().getText();
            fieldNamePos = getCurrentPos(field);
            fieldType = FieldKind.SINGLE;

        } else {
            fieldName = field.MUL().getText();
            // Set the position as same as field position.
            fieldNamePos = getCurrentPos(field);
            fieldType = FieldKind.ALL;
        }
        this.pkgBuilder.createFieldBasedAccessNode(getCurrentPos(ctx), getWS(ctx), fieldName, fieldNamePos,
                fieldType, ctx.field().OPTIONAL_FIELD_ACCESS() != null);
    }

    @Override
    public void exitMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createIndexBasedAccessNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitReservedWord(BallerinaParser.ReservedWordContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startInvocationNode(getWS(ctx));
    }

    @Override
    public void exitAnyIdentifierName(BallerinaParser.AnyIdentifierNameContext ctx) {
        if (isInErrorState) {
            return;
        }

        if (ctx.reservedWord() == null) {
            this.pkgBuilder.startInvocationNode(getWS(ctx));
        }
    }

    @Override
    public void exitInvocationReference(BallerinaParser.InvocationReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean argsAvailable = ctx.invocation().invocationArgList() != null;
        BallerinaParser.AnyIdentifierNameContext identifierContext = ctx.invocation().anyIdentifierName();
        String invocation = identifierContext.getText();
        this.pkgBuilder.createInvocationNode(getCurrentPos(ctx), getWS(ctx), invocation, argsAvailable,
                getCurrentPos(identifierContext));
    }

    @Override
    public void exitTypeDescExprInvocationReference(BallerinaParser.TypeDescExprInvocationReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean argsAvailable = ctx.invocation().invocationArgList() != null;
        BallerinaParser.AnyIdentifierNameContext identifierContext = ctx.invocation().anyIdentifierName();
        String invocation = identifierContext.getText();
        this.pkgBuilder.createInvocationNode(getCurrentPos(ctx), getWS(ctx), invocation, argsAvailable,
                getCurrentPos(identifierContext));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInvocationArgList(BallerinaParser.InvocationArgListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startExprNodeList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInvocationArgList(BallerinaParser.InvocationArgListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endExprNodeList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    public void enterExpressionList(BallerinaParser.ExpressionListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startExprNodeList();
    }

    @Override
    public void exitExpressionList(BallerinaParser.ExpressionListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endExprNodeList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    @Override
    public void exitExpressionStmt(BallerinaParser.ExpressionStmtContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addExpressionStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterTransactionStatement(BallerinaParser.TransactionStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startTransactionStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransactionStatement(BallerinaParser.TransactionStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        DiagnosticPos pos = getCurrentPos(ctx);
        this.pkgBuilder.endTransactionStmt(pos, getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransactionClause(BallerinaParser.TransactionClauseContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.addTransactionBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransactionPropertyInitStatementList(
            BallerinaParser.TransactionPropertyInitStatementListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endTransactionPropertyInitStatementList(getWS(ctx));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterLockStatement(BallerinaParser.LockStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startLockStmt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitLockStatement(BallerinaParser.LockStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addLockStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOnretryClause(BallerinaParser.OnretryClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startOnretryBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitOnretryClause(BallerinaParser.OnretryClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addOnretryBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterCommittedClause(BallerinaParser.CommittedClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startCommittedBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCommittedClause(BallerinaParser.CommittedClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endCommittedBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAbortedClause(BallerinaParser.AbortedClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startAbortedBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAbortedClause(BallerinaParser.AbortedClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endAbortedBlock(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAbortStatement(BallerinaParser.AbortStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addAbortStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRetryStatement(BallerinaParser.RetryStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addRetryStatement(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRetriesStatement(BallerinaParser.RetriesStatementContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.addRetryCountExpression(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx) {
    }

    @Override
    public void exitNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isTopLevel = ctx.parent instanceof BallerinaParser.CompilationUnitContext;
        String namespaceUri = ctx.QuotedStringLiteral().getText();
        DiagnosticPos pos = getCurrentPos(ctx);

        namespaceUri = namespaceUri.substring(1, namespaceUri.length() - 1);
        namespaceUri = StringEscapeUtils.unescapeJava(namespaceUri);
        String prefix = (ctx.Identifier() != null) ? ctx.Identifier().getText() : null;
        DiagnosticPos prefixPos = (ctx.Identifier() != null) ? getCurrentPos(ctx.Identifier()) : null;

        this.pkgBuilder.addXMLNSDeclaration(pos, getWS(ctx), namespaceUri, prefix, prefixPos, isTopLevel);
    }

    @Override
    public void exitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryRefEqualExpression(BallerinaParser.BinaryRefEqualExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitStaticMatchOrExpression(BallerinaParser.StaticMatchOrExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitStaticMatchIdentifierLiteral(BallerinaParser.StaticMatchIdentifierLiteralContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), null, ctx.Identifier().getText());
        this.pkgBuilder.createSimpleVariableReference(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitTypeDescExpr(BallerinaParser.TypeDescExprContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createTypeAccessExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx) {
        if (isInErrorState) {
            return;
        }
        int numAnnotations = ctx.annotationAttachment().size();
        this.pkgBuilder.createActionInvocationNode(getCurrentPos(ctx), getWS(ctx), ctx.START() != null,
                numAnnotations);
    }

    @Override
    public void exitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitBitwiseExpression(BallerinaParser.BitwiseExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }


    @Override
    public void exitBitwiseShiftExpression(BallerinaParser.BitwiseShiftExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        StringBuilder operator = new StringBuilder();

        for (int i = 1; i < ctx.getChildCount() - 1; i++) {
            operator.append(ctx.getChild(i).getText());
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), operator.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createTypeConversionExpr(getCurrentPos(ctx), getWS(ctx),
                ctx.annotationAttachment().size(), ctx.typeName() != null);
    }

    @Override
    public void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitIntegerRangeExpression(BallerinaParser.IntegerRangeExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createBinaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(1).getText());
    }

    @Override
    public void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createUnaryExpr(getCurrentPos(ctx), getWS(ctx), ctx.getChild(0).getText());
    }

    @Override
    public void exitTypeTestExpression(BallerinaParser.TypeTestExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createTypeTestExpression(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitAnnotAccessExpression(BallerinaParser.AnnotAccessExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createAnnotAccessNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitGroupExpression(BallerinaParser.GroupExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createGroupExpression(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTernaryExpression(BallerinaParser.TernaryExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createTernaryExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitCheckedExpression(BallerinaParser.CheckedExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createCheckedExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitCheckPanickedExpression(BallerinaParser.CheckPanickedExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createCheckPanickedExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitNameReference(BallerinaParser.NameReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        if (ctx.Identifier().size() == 2) {
            String pkgName = ctx.Identifier(0).getText();
            String name = ctx.Identifier(1).getText();
            DiagnosticPos pos = getCurrentPos(ctx);
            if (Names.IGNORE.value.equals(pkgName))  {
                dlog.error(pos, DiagnosticCode.INVALID_PACKAGE_NAME_QUALIFER, pkgName);
            }
            this.pkgBuilder.addNameReference(pos, getWS(ctx), pkgName, name);
        } else {
            String name = ctx.Identifier(0).getText();
            this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), null, name);
        }
    }

    @Override
    public void exitFunctionNameReference(BallerinaParser.FunctionNameReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }

        if (ctx.Identifier() != null) {
            String pkgName = ctx.Identifier().getText();
            String name = ctx.anyIdentifierName().getText();
            DiagnosticPos pos = getCurrentPos(ctx);
            if (Names.IGNORE.value.equals(pkgName))  {
                dlog.error(pos, DiagnosticCode.INVALID_PACKAGE_NAME_QUALIFER, pkgName);
            }
            this.pkgBuilder.addNameReference(pos, getWS(ctx), pkgName, name);
        } else {
            String name = ctx.anyIdentifierName().getText();
            this.pkgBuilder.addNameReference(getCurrentPos(ctx), getWS(ctx), null, name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitReturnParameter(BallerinaParser.ReturnParameterContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addReturnParam(getCurrentPos(ctx), getWS(ctx), ctx.annotationAttachment().size());
    }

    @Override
    public void exitLambdaReturnParameter(BallerinaParser.LambdaReturnParameterContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addReturnParam(getCurrentPos(ctx), getWS(ctx), ctx.annotationAttachment().size());
    }

    @Override
    public void enterParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        TerminalNode node;
        DiagnosticPos pos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);
        Object value;
        BallerinaParser.IntegerLiteralContext integerLiteralContext = ctx.integerLiteral();
        if (integerLiteralContext != null && (value = getIntegerLiteral(ctx, ctx.integerLiteral())) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.INT, value, ctx.getText());
        } else if (ctx.floatingPointLiteral() != null) {
            if ((node = ctx.floatingPointLiteral().DecimalFloatingPointNumber()) != null) {
                String nodeValue = getNodeValue(ctx, node);
                int literalTypeTag = NumericLiteralSupport.isDecimalDiscriminated(nodeValue)
                        ? TypeTags.DECIMAL : TypeTags.FLOAT;
                this.pkgBuilder.addLiteralValue(pos, ws, literalTypeTag, nodeValue, node.getText());
            } else if ((node = ctx.floatingPointLiteral().HexadecimalFloatingPointLiteral()) != null) {
                this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.FLOAT, getHexNodeValue(ctx, node), node.getText());
            }
        } else if ((node = ctx.BooleanLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.BOOLEAN, Boolean.parseBoolean(node.getText()),
                                            node.getText());
        } else if ((node = ctx.QuotedStringLiteral()) != null) {
            String text = node.getText();
            text = text.substring(1, text.length() - 1);
            text = StringEscapeUtils.unescapeJava(text);
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.STRING, text, node.getText());
        } else if (ctx.NullLiteral() != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.NIL, null, "null");
        } else if (ctx.nilLiteral() != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.NIL, null, "()");
        } else if (ctx.blobLiteral() != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.BYTE_ARRAY, ctx.blobLiteral().getText());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitNamedArgs(BallerinaParser.NamedArgsContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addNamedArgument(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitRestArgs(BallerinaParser.RestArgsContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.addRestArgument(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitXmlLiteral(BallerinaParser.XmlLiteralContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.attachXmlLiteralWS(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitComment(BallerinaParser.CommentContext ctx) {
        if (isInErrorState) {
            return;
        }

        Stack<String> stringFragments = getTemplateTextFragments(ctx.XMLCommentTemplateText());
        String endingString = getTemplateEndingStr(ctx.XMLCommentText());
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
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endXMLElement(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitEmptyTag(BallerinaParser.EmptyTagContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.createXMLAttribute(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitText(BallerinaParser.TextContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        DiagnosticPos pos = getCurrentPos(ctx);
        this.pkgBuilder.addTableQueryExpression(pos, getWS(ctx));
    }

    @Override
    public void enterOrderByClause(BallerinaParser.OrderByClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startOrderByClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitOrderByClause(BallerinaParser.OrderByClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endOrderByClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterLimitClause(BallerinaParser.LimitClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startLimitClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitLimitClause(BallerinaParser.LimitClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endLimitClauseNode(getCurrentPos(ctx), getWS(ctx), ctx.DecimalIntegerLiteral().getText());
    }

    @Override
    public void enterOrderByVariable(BallerinaParser.OrderByVariableContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startOrderByVariableNode(getCurrentPos(ctx));
    }

    @Override
    public void exitOrderByVariable(BallerinaParser.OrderByVariableContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isAscending = ctx.orderByType() != null && ctx.orderByType().ASCENDING() != null;
        boolean isDescending = ctx.orderByType() != null && ctx.orderByType().DESCENDING() != null;

        this.pkgBuilder.endOrderByVariableNode(getCurrentPos(ctx), getWS(ctx), isAscending, isDescending);
    }

    @Override
    public void enterGroupByClause(BallerinaParser.GroupByClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startGroupByClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitGroupByClause(BallerinaParser.GroupByClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endGroupByClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterHavingClause(BallerinaParser.HavingClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startHavingClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitHavingClause(BallerinaParser.HavingClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endHavingClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSelectExpression(BallerinaParser.SelectExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startSelectExpressionNode(getCurrentPos(ctx));
    }

    @Override
    public void exitSelectExpression(BallerinaParser.SelectExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }

        String identifier = ctx.Identifier() == null ? null : ctx.Identifier().getText();
        this.pkgBuilder.endSelectExpressionNode(identifier, getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterSelectClause(BallerinaParser.SelectClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startSelectClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitSelectClause(BallerinaParser.SelectClauseContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startSelectExpressionList();
    }

    @Override
    public void exitSelectExpressionList(BallerinaParser.SelectExpressionListContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endSelectExpressionList(getWS(ctx), ctx.getChildCount() / 2 + 1);
    }

    @Override
    public void enterWhereClause(BallerinaParser.WhereClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startWhereClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitWhereClause(BallerinaParser.WhereClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endWhereClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterStreamingAction(BallerinaParser.StreamingActionContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startStreamActionNode(getCurrentPos(ctx), diagnosticSrc.pkgID);
    }

    @Override
    public void exitStreamingAction(BallerinaParser.StreamingActionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.endStreamActionNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startPatternStreamingEdgeInputNode(getCurrentPos(ctx));
    }

    @Override
    public void exitPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx) {
        if (isInErrorState) {
            return;
        }

        String alias = ctx.Identifier() != null ? ctx.Identifier().getText() : null;
        this.pkgBuilder.endPatternStreamingEdgeInputNode(getCurrentPos(ctx), getWS(ctx), alias);
    }

    @Override
    public void enterWindowClause(BallerinaParser.WindowClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startWindowClauseNode(getCurrentPos(ctx));
    }

    @Override
    public void exitWindowClause(BallerinaParser.WindowClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endWindowsClauseNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterWithinClause(BallerinaParser.WithinClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startWithinClause(getCurrentPos(ctx));
    }

    @Override
    public void exitWithinClause(BallerinaParser.WithinClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        String timeScale = null;
        String timeDurationValue = null;
        if (ctx.timeScale() != null) {
            timeScale = ctx.timeScale().getText();
            timeDurationValue = ctx.DecimalIntegerLiteral().getText();
        }

        this.pkgBuilder.endWithinClause(getCurrentPos(ctx), getWS(ctx), timeDurationValue, timeScale);
    }

    @Override
    public void enterPatternClause(BallerinaParser.PatternClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startPatternClause(getCurrentPos(ctx));
    }

    @Override
    public void exitPatternClause(BallerinaParser.PatternClauseContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isForAllEvents = ctx.EVERY() != null;
        boolean isWithinClauseAvailable = ctx.withinClause() != null;

        this.pkgBuilder.endPatternClause(isForAllEvents, isWithinClauseAvailable, getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startPatternStreamingInputNode(getCurrentPos(ctx));
    }

    @Override
    public void exitPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean followedByAvailable = ctx.FOLLOWED() != null && ctx.BY() != null;
        boolean enclosedInParenthesis = ctx.LEFT_PARENTHESIS() != null && ctx.RIGHT_PARENTHESIS() != null;
        boolean andWithNotAvailable = ctx.NOT() != null && ctx.AND() != null;
        boolean forWithNotAvailable = ctx.timeScale() != null;
        boolean onlyAndAvailable = ctx.AND() != null && ctx.NOT() == null && ctx.FOR() == null;
        boolean onlyOrAvailable = ctx.OR() != null && ctx.NOT() == null && ctx.FOR() == null;
        boolean commaSeparated = ctx.COMMA() != null;

        String timeScale = null;
        String timeDurationValue = null;
        if (ctx.timeScale() != null) {
            timeScale = ctx.timeScale().getText();
            timeDurationValue = ctx.DecimalIntegerLiteral().getText();
        }

        this.pkgBuilder.endPatternStreamingInputNode(getCurrentPos(ctx), getWS(ctx), followedByAvailable,
                enclosedInParenthesis, andWithNotAvailable, forWithNotAvailable, onlyAndAvailable,
                onlyOrAvailable, commaSeparated, timeDurationValue, timeScale);
    }

    @Override
    public void enterStreamingInput(BallerinaParser.StreamingInputContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startStreamingInputNode(getCurrentPos(ctx));
    }

    @Override
    public void exitStreamingInput(BallerinaParser.StreamingInputContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startJoinStreamingInputNode(getCurrentPos(ctx));
    }

    @Override
    public void exitJoinStreamingInput(BallerinaParser.JoinStreamingInputContext ctx) {
        if (isInErrorState) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitJoinType(BallerinaParser.JoinTypeContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endJoinType(getWS(ctx));
    }

    @Override
    public void enterOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startOutputRateLimitNode(getCurrentPos(ctx));
    }

    @Override
    public void exitOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx) {
        if (isInErrorState) {
            return;
        }

        boolean isSnapshotOutputRateLimit = false;
        boolean isFirst = false;
        boolean isLast = false;
        boolean isAll = false;

        if (ctx.SNAPSHOT() != null) {
            isSnapshotOutputRateLimit = true;
        } else {
            if (ctx.LAST() != null) {
                isLast = true;
            } else if (ctx.FIRST() != null) {
                isFirst = true;
            } else if (ctx.LAST() != null) {
                isAll = true;
            }
        }

        String timeScale = null;
        if (ctx.timeScale() != null) {
            timeScale = ctx.timeScale().getText();
        }

        this.pkgBuilder.endOutputRateLimitNode(getCurrentPos(ctx), getWS(ctx), isSnapshotOutputRateLimit,
                isFirst, isLast, isAll, timeScale, ctx.DecimalIntegerLiteral().getText());
    }

    @Override
    public void enterTableQuery(BallerinaParser.TableQueryContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startTableQueryNode(getCurrentPos(ctx));
    }

    @Override
    public void exitTableQuery(BallerinaParser.TableQueryContext ctx) {
        if (isInErrorState) {
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
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startStreamingQueryStatementNode(getCurrentPos(ctx));
    }

    @Override
    public void exitStreamingQueryStatement(BallerinaParser.StreamingQueryStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endStreamingQueryStatementNode(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void enterForeverStatement(BallerinaParser.ForeverStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.startForeverNode(getCurrentPos(ctx));
    }

    @Override
    public void exitForeverStatement(BallerinaParser.ForeverStatementContext ctx) {
        if (isInErrorState) {
            return;
        }

        DiagnosticPos pos = getCurrentPos(ctx);
        this.pkgBuilder.endForeverNode(pos, getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDocumentationString(BallerinaParser.DocumentationStringContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.startMarkdownDocumentationString(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDocumentationString(BallerinaParser.DocumentationStringContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.endMarkdownDocumentationString(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDocumentationLine(BallerinaParser.DocumentationLineContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endMarkDownDocumentLine(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDocumentationContent(BallerinaParser.DocumentationContentContext ctx) {
        if (isInErrorState) {
            return;
        }
        String text = ctx.getText() != null ? ctx.getText() : "";
        this.pkgBuilder.endMarkdownDocumentationText(getCurrentPos(ctx), getWS(ctx), text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterDocumentationLine(BallerinaParser.ParameterDocumentationLineContext ctx) {
        if (isInErrorState) {
            return;
        }

        this.pkgBuilder.endParameterDocumentationLine(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterDocumentation(BallerinaParser.ParameterDocumentationContext ctx) {
        if (isInErrorState) {
            return;
        }
        String parameterName = ctx.docParameterName() != null ? ctx.docParameterName().getText() : "";
        String description = ctx.documentationText() != null ? ctx.documentationText().getText() : "";
        this.pkgBuilder.endParameterDocumentation(getCurrentPos(ctx.docParameterName()), getWS(ctx), parameterName,
                description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterDescriptionLine(BallerinaParser.ParameterDescriptionLineContext ctx) {
        if (isInErrorState) {
            return;
        }
        String description = ctx.documentationText() != null ? ctx.documentationText().getText() : "";
        this.pkgBuilder.endParameterDocumentationDescription(getWS(ctx), description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitReturnParameterDocumentation(BallerinaParser.ReturnParameterDocumentationContext ctx) {
        if (isInErrorState) {
            return;
        }
        String description = ctx.documentationText() != null ? ctx.documentationText().getText() : "";
        this.pkgBuilder.endReturnParameterDocumentation(getCurrentPos(ctx.getParent()), getWS(ctx), description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitReturnParameterDescriptionLine(BallerinaParser.ReturnParameterDescriptionLineContext ctx) {
        if (isInErrorState) {
            return;
        }
        String description = ctx.documentationText() != null ? ctx.documentationText().getText() : "";
        this.pkgBuilder.endReturnParameterDocumentationDescription(getWS(ctx), description);
    }

    @Override
    public void exitTrapExpression(BallerinaParser.TrapExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        this.pkgBuilder.createTrapExpr(getCurrentPos(ctx), getWS(ctx));
    }

    @Override
    public void exitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) {
        if (isInErrorState) {
            return;
        }
        if (ctx.START() != null) {
            int numAnnotations = ctx.annotationAttachment().size();
            this.pkgBuilder.markLastInvocationAsAsync(getCurrentPos(ctx), numAnnotations);
        }
    }

    @Override
    public void exitDocumentationReference(BallerinaParser.DocumentationReferenceContext ctx) {
        if (isInErrorState) {
            return;
        }
        BallerinaParser.ReferenceTypeContext referenceType  = ctx.referenceType();
        BallerinaParser.SingleBacktickedContentContext backtickedContent = ctx.singleBacktickedContent();
        this.pkgBuilder.endDocumentationReference(getCurrentPos(ctx), referenceType.getText(),
                backtickedContent.getText());
    }

    @Override
    public void exitSingleBacktickedBlock(BallerinaParser.SingleBacktickedBlockContext ctx) {
        if (isInErrorState) {
            return;
        }
        BallerinaParser.SingleBacktickedContentContext backtickedContent = ctx.singleBacktickedContent();
        this.pkgBuilder.endSingleBacktickedBlock(getCurrentPos(ctx), backtickedContent.getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitElvisExpression(BallerinaParser.ElvisExpressionContext ctx) {
        if (isInErrorState) {
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
            endCol = stop.getCharPositionInLine() + (stop.getStopIndex() - stop.getStartIndex() + 1) + 1;
        }

        return new DiagnosticPos(diagnosticSrc, startLine, endLine, startCol, endCol);
    }

    private DiagnosticPos getCurrentPos(TerminalNode node) {
        Token symbol = node.getSymbol();
        int startLine = symbol.getLine();
        int startCol = symbol.getCharPositionInLine() + 1;

        int endLine = startLine;
        int endCol = startCol + symbol.getText().length();
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

    private String getTemplateEndingStr(List<TerminalNode> nodes) {
        StringJoiner joiner = new StringJoiner("");
        nodes.forEach(node -> joiner.add(node.getText()));
        return joiner.toString();
    }

    private String getNodeValue(ParserRuleContext ctx, TerminalNode node) {
        String op = ctx.getChild(0).getText();
        String value = node.getText();
        if (op != null && "-".equals(op)) {
            value = "-" + value;
        }
        return value;
    }

    private String getHexNodeValue(ParserRuleContext ctx, TerminalNode node) {
        String value = getNodeValue(ctx, node);
        if (!(value.contains("p") || value.contains("P"))) {
            value = value + "p0";
        }
        return value;
    }

    private Object getIntegerLiteral(ParserRuleContext simpleLiteralContext,
                                     BallerinaParser.IntegerLiteralContext integerLiteralContext) {
        if (integerLiteralContext.DecimalIntegerLiteral() != null) {
            String nodeValue = getNodeValue(simpleLiteralContext, integerLiteralContext.DecimalIntegerLiteral());
            return parseLong(simpleLiteralContext, nodeValue, nodeValue, 10, DiagnosticCode.INTEGER_TOO_SMALL,
                    DiagnosticCode.INTEGER_TOO_LARGE);
        } else if (integerLiteralContext.HexIntegerLiteral() != null) {
            String nodeValue = getNodeValue(simpleLiteralContext, integerLiteralContext.HexIntegerLiteral());
            String processedNodeValue = nodeValue.toLowerCase().replace("0x", "");
            return parseLong(simpleLiteralContext, nodeValue, processedNodeValue, 16,
                    DiagnosticCode.HEXADECIMAL_TOO_SMALL, DiagnosticCode.HEXADECIMAL_TOO_LARGE);
        }
        return null;
    }

    private Object parseLong(ParserRuleContext context, String originalNodeValue, String processedNodeValue, int radix,
                             DiagnosticCode code1, DiagnosticCode code2) {
        try {
            return Long.parseLong(processedNodeValue, radix);
        } catch (Exception e) {
            DiagnosticPos pos = getCurrentPos(context);
            Set<Whitespace> ws = getWS(context);
            if (originalNodeValue.startsWith("-")) {
                dlog.error(pos, code1, originalNodeValue);
            } else {
                dlog.error(pos, code2, originalNodeValue);
            }
        }
        return originalNodeValue;
    }

    /**
     * Mark that this listener is in error state.
     */
    public void setErrorState() {
        this.isInErrorState = true;
    }

    /**
     * Mark that this listener is not in an error state.
     */
    public void unsetErrorState() {
        this.isInErrorState = false;
    }
}
