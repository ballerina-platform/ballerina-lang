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
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser.StringTemplateContentContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserBaseListener;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
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

    BLangParserListener(CompilerContext context, CompilationUnitNode compUnit,
                        BDiagnosticSource diagnosticSource) {
        this.pkgBuilder = new BLangPackageBuilder(context, compUnit);
        this.diagnosticSrc = diagnosticSource;
    }

    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    @Override
    public void enterReturnParameterList(BallerinaParser.ReturnParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    @Override
    public void exitParameter(BallerinaParser.ParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.SENSITIVE() == null) {
            this.pkgBuilder.addVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                    false, ctx.annotationAttachment().size());
        } else {
            this.pkgBuilder.addVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                    false, ctx.annotationAttachment().size(), Flag.SENSITIVE);
        }
    }

    @Override
    public void exitReturnParameter(BallerinaParser.ReturnParameterContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.TAINTED() == null) {
            this.pkgBuilder.addVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                    false, ctx.annotationAttachment().size());
        } else {
            this.pkgBuilder.addVar(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(),
                    false, ctx.annotationAttachment().size(), Flag.TAINTED);
        }
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
        this.pkgBuilder.addImportPackageDeclaration(getCurrentPos(ctx), getWS(ctx),
                this.pkgNameComps, this.pkgVersion, alias);
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
        this.pkgBuilder.endServiceDef(getCurrentPos(ctx), getWS(ctx), ctx.Identifier(0).getText(),
                ctx.Identifier(1).getText());
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
        this.pkgBuilder.endResourceDef(getCurrentPos(ctx), getWS(ctx),
                ctx.Identifier().getText(), ctx.annotationAttachment().size());
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

        boolean isReceiverAttached = ctx.parameter() != null;

        int nativeKWTokenIndex = 0;
        boolean publicFunc = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        if (publicFunc) {
            nativeKWTokenIndex = 1;
        }
        boolean nativeFunc = KEYWORD_NATIVE.equals(ctx.getChild(nativeKWTokenIndex).getText());
        boolean bodyExists = ctx.callableUnitBody() != null;
        this.pkgBuilder.endFunctionDef(getCurrentPos(ctx), getWS(ctx), publicFunc, nativeFunc,
                bodyExists, isReceiverAttached);
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

        this.pkgBuilder.addLambdaFunctionDef(getCurrentPos(ctx), getWS(ctx), ctx.parameterList() != null,
                ctx.returnParameters() != null,
                ctx.returnParameters() != null && ctx.returnParameters().returnTypeList() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endCallableUnitSignature(getWS(ctx), ctx.Identifier().getText(),
                ctx.parameterList() != null, ctx.returnParameters() != null,
                ctx.returnParameters() != null && ctx.returnParameters().returnTypeList() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startConnectorDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicConnector = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        this.pkgBuilder.endConnectorDef(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(), publicConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startConnectorBody();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConnectorBody(BallerinaParser.ConnectorBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.endConnectorBody(getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterActionDefinition(BallerinaParser.ActionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startActionDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitActionDefinition(BallerinaParser.ActionDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean nativeAction = ctx.NATIVE() != null;
        boolean bodyExists = ctx.callableUnitBody() != null;
        this.pkgBuilder.endActionDef(
                getCurrentPos(ctx), getWS(ctx), ctx.annotationAttachment().size(), nativeAction, bodyExists);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startStructDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitStructDefinition(BallerinaParser.StructDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicStruct = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        this.pkgBuilder.endStructDef(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(), publicStruct);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterStructBody(BallerinaParser.StructBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
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
        this.pkgBuilder.endAnnotationDef(getWS(ctx), ctx.Identifier().getText(), publicAnnotation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEnumDefinition(BallerinaParser.EnumDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startEnumDef(getCurrentPos(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitEnumDefinition(BallerinaParser.EnumDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicEnum = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        this.pkgBuilder.endEnumDef(ctx.Identifier().getText(), publicEnum);
    }

    @Override
    public void exitEnumerator(BallerinaParser.EnumeratorContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addEnumerator(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterTransformerDefinition(BallerinaParser.TransformerDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startTransformerDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransformerDefinition(BallerinaParser.TransformerDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        TerminalNode identifier = ctx.Identifier();
        String transformerName = identifier == null ? null : identifier.getText();
        boolean publicFunc = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        boolean paramsAvailable = ctx.parameterList().size() > 1;
        this.pkgBuilder.endTransformerDef(getCurrentPos(ctx), getWS(ctx), publicFunc, transformerName, paramsAvailable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitServiceAttachPoint(BallerinaParser.ServiceAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        String pkgPath = null;
        if (ctx.getChildCount() == 4) {
            pkgPath = ctx.Identifier().getText();
        } else if (ctx.getChildCount() == 3) {
            pkgPath = "";
        }
        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.SERVICE, pkgPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitResourceAttachPoint(BallerinaParser.ResourceAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.RESOURCE, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConnectorAttachPoint(BallerinaParser.ConnectorAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONNECTOR, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitActionAttachPoint(BallerinaParser.ActionAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ACTION, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFunctionAttachPoint(BallerinaParser.FunctionAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.FUNCTION, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitStructAttachPoint(BallerinaParser.StructAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.STRUCT, null);
    }

    @Override
    public void exitEnumAttachPoint(BallerinaParser.EnumAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ENUM, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConstAttachPoint(BallerinaParser.ConstAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.CONST, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitParameterAttachPoint(BallerinaParser.ParameterAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.PARAMETER, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationAttachPoint(BallerinaParser.AnnotationAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.ANNOTATION, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTransformerAttachPoint(BallerinaParser.TransformerAttachPointContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint.TRANSFORMER, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAnnotationBody(BallerinaParser.AnnotationBodyContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startVarList();
    }

    @Override
    public void exitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean publicVar = KEYWORD_PUBLIC.equals(ctx.getChild(0).getText());
        this.pkgBuilder.addConstVariable(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText(), publicVar);
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
    public void exitTypeName(BallerinaParser.TypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.referenceTypeName() != null || ctx.valueTypeName() != null) {
            return;
        }
        if (ctx.typeName() != null) {
            // This ia an array Type.
            this.pkgBuilder.addArrayType(getCurrentPos(ctx), getWS(ctx), (ctx.getChildCount() - 1) / 2);
            return;
        }
        // This is 'any' type
        this.pkgBuilder.addValueType(getCurrentPos(ctx), getWS(ctx), ctx.getChild(0).getText());
    }

    @Override
    public void exitBuiltInTypeName(BallerinaParser.BuiltInTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        if (ctx.builtInReferenceTypeName() != null || ctx.valueTypeName() != null) {
            return;
        }
        if (ctx.typeName() != null) {
            // This is an array Type.
            this.pkgBuilder.addArrayType(getCurrentPos(ctx), getWS(ctx), (ctx.getChildCount() - 1) / 2);
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
    public void enterAnonStructTypeName(BallerinaParser.AnonStructTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startStructDef();
    }


    @Override
    public void exitAnonStructTypeName(BallerinaParser.AnonStructTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addAnonStructType(getCurrentPos(ctx), getWS(ctx));
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
        } else {
            this.pkgBuilder.addBuiltInReferenceType(getCurrentPos(ctx), getWS(ctx), typeName);
        }
    }

    @Override
    public void exitFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx) {
        if (ctx.exception != null) {
            return;
        }

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
            if (returnCtx.returnParameterList() != null) {
                retParamsAvail = returnCtx.returnParameterList().returnParameter().size() > 0;
            } else if (returnCtx.returnTypeList() != null) {
                retParamsAvail = returnCtx.returnTypeList().typeName().size() > 0;
                retParamTypeOnly = true;
            }
        }

        this.pkgBuilder.addFunctionType(getCurrentPos(ctx), getWS(ctx), paramsAvail, paramsTypeOnly, retParamsAvail,
                retParamTypeOnly, returnsKeywordExists);
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

        this.pkgBuilder.setAnnotationAttachmentName(getWS(ctx));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationAttribute(BallerinaParser.AnnotationAttributeContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String attrName = ctx.Identifier().getText();
        this.pkgBuilder.createAnnotAttachmentAttribute(getCurrentPos(ctx), getWS(ctx), attrName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void exitAnnotationAttributeValue(BallerinaParser.AnnotationAttributeValueContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        ParseTree childContext = ctx.getChild(0);
        if (childContext instanceof BallerinaParser.SimpleLiteralContext) {
            this.pkgBuilder.createLiteralTypeAttributeValue(getCurrentPos(ctx), getWS(ctx));
        } else if (childContext instanceof BallerinaParser.NameReferenceContext) {
            this.pkgBuilder.createVarRefTypeAttributeValue(getCurrentPos(ctx), getWS(ctx));
        } else if (childContext instanceof BallerinaParser.AnnotationAttachmentContext) {
            this.pkgBuilder.createAnnotationTypeAttributeValue(getCurrentPos(ctx), getWS(ctx));
        } else if (childContext instanceof BallerinaParser.AnnotationAttributeArrayContext) {
            this.pkgBuilder.createArrayTypeAttributeValue(getCurrentPos(ctx), getWS(ctx));
        }
    }

    @Override
    public void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                ctx.Identifier().getText(), ctx.ASSIGN() != null, false);
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

        // If the key is a stringLiteral or stringTemplateLiteral, they are added to the model
        // from their respective listener methods
        if (ctx.Identifier() != null) {
            DiagnosticPos pos = getCurrentPos(ctx);
            this.pkgBuilder.addNameReference(pos, getWS(ctx), null, ctx.Identifier().getText());
            this.pkgBuilder.createSimpleVariableReference(pos, getWS(ctx));
        }
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
    public void exitConnectorInit(BallerinaParser.ConnectorInitContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        boolean argsAvailable = ctx.expressionList() != null;
        this.pkgBuilder.addConnectorInitExpression(getCurrentPos(ctx), getWS(ctx), argsAvailable);
    }

    @Override
    public void exitEndpointDeclaration(BallerinaParser.EndpointDeclarationContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        if (ctx.endpointDefinition() == null) {
            return;
        }

        String endpointName = ctx.endpointDefinition().Identifier().getText();

        boolean exprAvailable = ctx.connectorInit() != null || ctx.variableReference() != null;
        this.pkgBuilder.addVariableDefStatement(getCurrentPos(ctx), getWS(ctx),
                endpointName, exprAvailable, true);
    }

    @Override
    public void exitEndpointDefinition(BallerinaParser.EndpointDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        this.pkgBuilder.addEndpointType(getCurrentPos(ctx), getWS(ctx));
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
        if (ctx.getChild(0).getText().equals("var")) {
            isVarDeclaration = true;
        }
        this.pkgBuilder.addAssignmentStatement(getCurrentPos(ctx), getWS(ctx), isVarDeclaration);
    }

    @Override
    public void exitBindStatement(BallerinaParser.BindStatementContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addBindStatement(getCurrentPos(ctx), getWS(ctx), ctx.Identifier().getText());
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
                ctx.LEFT_PARENTHESIS() == null, ctx.RIGHT_PARENTHESIS() == null);
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
        if (ctx.IntegerLiteral() != null) {
            try {
                joinCount = Integer.valueOf(ctx.IntegerLiteral().getText());
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

        this.pkgBuilder.addReturnStatement(this.getCurrentPos(ctx), getWS(ctx), ctx.expressionList() != null);
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

        boolean argsAvailable = ctx.expressionList() != null;
        this.pkgBuilder.createFunctionInvocation(getCurrentPos(ctx), getWS(ctx), argsAvailable);
    }

    @Override
    public void exitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        String fieldName = ctx.field().Identifier().getText();
        this.pkgBuilder.createFieldBasedAccessNode(getCurrentPos(ctx), getWS(ctx), fieldName);
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

        boolean argsAvailable = ctx.invocation().expressionList() != null;
        String invocation = ctx.invocation().anyIdentifierName().getText();
        this.pkgBuilder.createInvocationNode(getCurrentPos(ctx), getWS(ctx), invocation, argsAvailable);
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

        this.pkgBuilder.endTransactionStmt(getCurrentPos(ctx), getWS(ctx));
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
    public void enterFailedClause(BallerinaParser.FailedClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startFailedBlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFailedClause(BallerinaParser.FailedClauseContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.addFailedBlock(getCurrentPos(ctx), getWS(ctx));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitTaintStatement(BallerinaParser.TaintStatementContext ctx) {
        this.pkgBuilder.addTaintStmt(getCurrentPos(ctx), getWS(ctx));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitUntaintStatement(BallerinaParser.UntaintStatementContext ctx) {
        this.pkgBuilder.addUntaintStmt(getCurrentPos(ctx), getWS(ctx));
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
    public void exitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.createTypeCastExpr(getCurrentPos(ctx), getWS(ctx));
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
    public void enterTypeList(BallerinaParser.TypeListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startProcessingTypeNodeList();
    }

    @Override
    public void exitTypeList(BallerinaParser.TypeListContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        ParserRuleContext parent = ctx.getParent();
        boolean inFuncTypeSig = parent instanceof BallerinaParser.FunctionTypeNameContext ||
                parent instanceof BallerinaParser.ReturnParametersContext &&
                        parent.parent instanceof BallerinaParser.FunctionTypeNameContext;
        if (inFuncTypeSig) {
            this.pkgBuilder.endProcessingTypeNodeList(getWS(ctx), ctx.typeName().size());
        } else {
            this.pkgBuilder.endProcessingTypeNodeList(ctx.typeName().size());
        }
    }

    @Override
    public void enterReturnTypeList(BallerinaParser.ReturnTypeListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        this.pkgBuilder.startProcessingTypeNodeList();
    }

    @Override
    public void exitReturnTypeList(BallerinaParser.ReturnTypeListContext ctx) {
        if (ctx.exception != null) {
            return;
        }
        ParserRuleContext parent = ctx.getParent();
        boolean inFuncTypeSig = parent instanceof BallerinaParser.FunctionTypeNameContext ||
                parent instanceof BallerinaParser.ReturnParametersContext &&
                        parent.parent instanceof BallerinaParser.FunctionTypeNameContext;
        if (inFuncTypeSig) {
            this.pkgBuilder.endProcessingTypeNodeList(getWS(ctx), ctx.typeName().size());
        } else {
            this.pkgBuilder.endProcessingTypeNodeList(ctx.typeName().size());
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
                parent instanceof BallerinaParser.ReturnParametersContext &&
                        parent.parent instanceof BallerinaParser.FunctionTypeNameContext;
        if (parent instanceof BallerinaParser.ConnectorDefinitionContext) {
            this.pkgBuilder.endConnectorParamList(getWS(ctx));
        } else if (inFuncTypeSig) {
            this.pkgBuilder.endFuncTypeParamList(getWS(ctx));
        } else {
            this.pkgBuilder.endCallableParamList(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitReturnParameterList(BallerinaParser.ReturnParameterListContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        // This attaches WS of the commas to the def.
        ParserRuleContext parent = ctx.getParent();
        boolean inFuncTypeSig = parent instanceof BallerinaParser.FunctionTypeNameContext ||
                parent instanceof BallerinaParser.ReturnParametersContext &&
                        parent.parent instanceof BallerinaParser.FunctionTypeNameContext;
        if (parent instanceof BallerinaParser.ConnectorDefinitionContext) {
            this.pkgBuilder.endConnectorParamList(getWS(ctx));
        } else if (inFuncTypeSig) {
            this.pkgBuilder.endFuncTypeParamList(getWS(ctx));
        } else {
            this.pkgBuilder.endCallableParamList(getWS(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx) {
        if (ctx.exception != null) {
            return;
        }

        DiagnosticPos currentPos = getCurrentPos(ctx);
        Set<Whitespace> ws = getWS(ctx);
        String name = ctx.Identifier().getText();
        boolean exprAvailable = ctx.simpleLiteral() != null;
        if (ctx.parent instanceof BallerinaParser.StructBodyContext) {
            this.pkgBuilder.addVarToStruct(currentPos, ws, name, exprAvailable, 0, false);
        } else if (ctx.parent instanceof BallerinaParser.PrivateStructBodyContext) {
            this.pkgBuilder.addVarToStruct(currentPos, ws, name, exprAvailable, 0, true);
        } else if (ctx.parent instanceof BallerinaParser.AnnotationBodyContext) {
            this.pkgBuilder.addVarToAnnotation(currentPos, ws, name, exprAvailable, 0);
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
        if ((node = ctx.IntegerLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.INT, Long.parseLong(getNodeValue(ctx, node)));
        } else if ((node = ctx.FloatingPointLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.FLOAT, Double.parseDouble(getNodeValue(ctx, node)));
        } else if ((node = ctx.BooleanLiteral()) != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.BOOLEAN, Boolean.parseBoolean(node.getText()));
        } else if ((node = ctx.QuotedStringLiteral()) != null) {
            String text = node.getText();
            text = text.substring(1, text.length() - 1);
            text = StringEscapeUtils.unescapeJava(text);
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.STRING, text);
        } else if (ctx.NullLiteral() != null) {
            this.pkgBuilder.addLiteralValue(pos, ws, TypeTags.NULL, null);
        }
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

    private String getNodeValue(BallerinaParser.SimpleLiteralContext ctx, TerminalNode node) {
        String op = ctx.getChild(0).getText();
        String value = node.getText();
        if (op != null && "-".equals(op)) {
            value = "-" + value;
        }
        return value;
    }
}
