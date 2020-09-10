/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SchedulerPolicy;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Desugar annotations into executable entries.
 *
 * @since 0.965.0
 */
public class AnnotationDesugar {

    public static final String ANNOTATION_DATA = "$annotation_data";
    private static final String ANNOT_FUNC = "$annot_func$";
    public static final String BUILTIN_PKG_KEY = "ballerina" + "/" + "builtin";
    public static final String DEFAULTABLE_ANN = "DefaultableArgs";
    public static final String DEFAULTABLE_REC = "ArgsData";
    public static final String ARG_NAMES = "args";
    private static final String DOT = ".";
    private static final String FIELD = "$field$";
    private static final String PARAM = "$param$";
    private static final String RETURNS = "$returns$";
    private BLangSimpleVariable annotationMap;
    private int annotFuncCount = 0;

    private static final CompilerContext.Key<AnnotationDesugar> ANNOTATION_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final Desugar desugar;
    private final SymbolTable symTable;
    private final Types types;
    private final Names names;
    private SymbolResolver symResolver;

    public static AnnotationDesugar getInstance(CompilerContext context) {
        AnnotationDesugar annotationDesugar = context.get(ANNOTATION_DESUGAR_KEY);
        if (annotationDesugar == null) {
            annotationDesugar = new AnnotationDesugar(context);
        }
        return annotationDesugar;
    }

    private AnnotationDesugar(CompilerContext context) {
        context.put(ANNOTATION_DESUGAR_KEY, this);
        this.desugar = Desugar.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
    }

    /**
     * Initialize annotation map.
     *
     * @param pkgNode package node
     */
    void initializeAnnotationMap(BLangPackage pkgNode) {
        annotationMap = createGlobalAnnotationMapVar(pkgNode);
    }

    void rewritePackageAnnotations(BLangPackage pkgNode, SymbolEnv env) {
        BLangFunction initFunction = pkgNode.initFunction;

        defineTypeAnnotations(pkgNode, env, initFunction);
        defineServiceAnnotations(pkgNode, env, initFunction);
        defineFunctionAnnotations(pkgNode, env, initFunction);
    }

    void defineStatementAnnotations(List<BLangAnnotationAttachment> attachments, DiagnosticPos pos, PackageID pkgID,
                                    BSymbol owner, SymbolEnv env) {
        BLangFunction function = defineFunction(pos, pkgID, owner);
        BLangRecordLiteral mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
        addAnnotsToLiteral(attachments, mapLiteral, function, env);
    }

    private void defineTypeAnnotations(BLangPackage pkgNode, SymbolEnv env, BLangFunction initFunction) {
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            if (typeDef.isBuiltinTypeDef) {
                continue;
            }
            PackageID pkgID = typeDef.symbol.pkgID;
            BSymbol owner = typeDef.symbol.owner;

            BLangType typeNode = typeDef.typeNode;
            SymbolEnv typeEnv = SymbolEnv.createTypeEnv(typeNode, initFunction.symbol.scope, env);
            BLangLambdaFunction lambdaFunction;

            if (typeNode.getKind() == NodeKind.RECORD_TYPE || typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                lambdaFunction = defineAnnotations(typeDef, pkgNode, typeEnv, pkgID, owner);
            } else {
                lambdaFunction = defineAnnotations(typeDef, typeDef.pos, pkgNode, typeEnv, pkgID, owner);
            }

            if (lambdaFunction != null) {
                addInvocationToGlobalAnnotMap(typeDef.name.value, lambdaFunction, initFunction.body);
            }
        }
    }

    private void defineServiceAnnotations(BLangPackage pkgNode, SymbolEnv env, BLangFunction initFunction) {
        BLangBlockFunctionBody initFnBody = (BLangBlockFunctionBody) initFunction.body;
        for (BLangService service : pkgNode.services) {
            SymbolEnv funcEnv = SymbolEnv.createServiceEnv(service, initFunction.symbol.scope, env);
            BLangLambdaFunction lambdaFunction = defineAnnotations(service, service.pos, pkgNode,
                                                                   funcEnv, service.symbol.pkgID, service.symbol.owner);
            if (lambdaFunction != null) {
                // Add the lambda in a temporary block.
                BLangBlockStmt target = (BLangBlockStmt) TreeBuilder.createBlockNode();
                target.pos = initFnBody.pos;

                addLambdaToGlobalAnnotMap(service.serviceTypeDefinition.name.value, lambdaFunction, target);

                // Add the annotation assignment to immediately before the service init.
                int index = calculateIndex(initFnBody.stmts, service);
                for (BLangStatement stmt : target.stmts) {
                    initFnBody.stmts.add(index++, stmt);
                }
            }
        }
    }

    private void defineFunctionAnnotations(BLangPackage pkgNode, SymbolEnv env, BLangFunction initFunction) {
        BLangBlockFunctionBody initFnBody = (BLangBlockFunctionBody) initFunction.body;
        BLangFunction[] functions = pkgNode.functions.toArray(new BLangFunction[pkgNode.functions.size()]);
        for (BLangFunction function : functions) {
            PackageID pkgID = function.symbol.pkgID;
            BSymbol owner = function.symbol.owner;
            if (function.symbol.name.getValue().equals("main")) {
                addVarArgsAnnotation(function);
            }

            if (function.flagSet.contains(Flag.WORKER)) {
                attachSchedulerPolicy(function);
            }

            BLangLambdaFunction lambdaFunction = defineAnnotations(function, pkgNode, env, pkgID, owner);
            if (lambdaFunction != null) {
                // Add the lambda/invocation in a temporary block.
                BLangBlockStmt target = (BLangBlockStmt) TreeBuilder.createBlockNode();
                target.pos = initFnBody.pos;
                String identifier = function.attachedFunction ? function.symbol.name.value : function.name.value;

                int index;
                if (function.attachedFunction && function.receiver.type instanceof BServiceType) {
                    addLambdaToGlobalAnnotMap(identifier, lambdaFunction, target);
                    index = calculateIndex(initFnBody.stmts, function.receiver.type.tsymbol);
                } else {
                    addInvocationToGlobalAnnotMap(identifier, lambdaFunction, target);
                    index = initFnBody.stmts.size();
                }

                // Add the annotation assignment for resources to immediately before the service init.
                for (BLangStatement stmt : target.stmts) {
                    initFnBody.stmts.add(index++, stmt);
                }
            }
        }
    }

    private void attachSchedulerPolicy(BLangFunction function) {
        for (BLangAnnotationAttachment annotation : function.annAttachments) {
            if (!annotation.annotationName.value.equals("strand")) {
                continue;
            }
            List<RecordLiteralNode.RecordField> fields = ((BLangRecordLiteral) annotation.expr).fields;
            for (RecordLiteralNode.RecordField field : fields) {
                if (field.getKind() != NodeKind.RECORD_LITERAL_KEY_VALUE) {
                    continue;
                }
                BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                BLangExpression expr = keyValue.key.expr;
                if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                    continue;
                }
                BLangIdentifier variableName = ((BLangSimpleVarRef) expr).variableName;
                if (variableName.value.equals("name")) {
                    if (keyValue.valueExpr.getKind() != NodeKind.LITERAL) {
                        continue;
                    }
                    function.symbol.strandName = ((BLangLiteral) keyValue.valueExpr).value.toString();
                    continue;
                }
                if (variableName.value.equals("thread")) {
                    if (keyValue.valueExpr.getKind() != NodeKind.LITERAL) {
                        continue;
                    }
                    Object value = ((BLangLiteral) keyValue.valueExpr).value;
                    if ("any".equals(value)) {
                        function.symbol.schedulerPolicy = SchedulerPolicy.ANY;
                    }
                }

            }
        }
    }

    private BLangLambdaFunction defineAnnotations(AnnotatableNode node, DiagnosticPos pos, BLangPackage pkgNode,
                                                  SymbolEnv env, PackageID pkgID, BSymbol owner) {
        return defineAnnotations(getAnnotationList(node), pos, pkgNode, env, pkgID, owner);
    }

    private List<BLangAnnotationAttachment> getAnnotationList(AnnotatableNode node) {
        return node.getAnnotationAttachments().stream()
                .map(annotAttachment -> (BLangAnnotationAttachment) annotAttachment)
                .collect(Collectors.toList());
    }

    private BLangLambdaFunction defineAnnotations(List<BLangAnnotationAttachment> annAttachments, DiagnosticPos pos,
                                                  BLangPackage pkgNode, SymbolEnv env, PackageID pkgID, BSymbol owner) {
        if (annAttachments.isEmpty()) {
            return null;
        }

        BLangFunction function = defineFunction(pos, pkgID, owner);
        BLangRecordLiteral mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
        addAnnotsToLiteral(annAttachments, mapLiteral, function, env);

        if (mapLiteral.fields.isEmpty()) {
            return null;
        }

        return addReturnAndDefineLambda(function, mapLiteral, pkgNode, env, pkgID, owner);
    }

    private BLangLambdaFunction defineAnnotations(BLangTypeDefinition typeDef, BLangPackage pkgNode, SymbolEnv env,
                                                  PackageID pkgID, BSymbol owner) {
        BLangFunction function = null;
        BLangRecordLiteral mapLiteral = null;
        BLangLambdaFunction lambdaFunction = null;

        boolean annotFunctionDefined = false;

        if (!typeDef.annAttachments.isEmpty()) {
            function = defineFunction(typeDef.pos, pkgID, owner);
            mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
            addAnnotsToLiteral(typeDef.annAttachments, mapLiteral, function, env);
            annotFunctionDefined = true;
        }

        for (BLangSimpleVariable field : ((BLangStructureTypeNode) typeDef.typeNode).fields) {
            BLangLambdaFunction paramAnnotLambda = defineAnnotations(field.annAttachments, field.pos, pkgNode, env,
                                                                     pkgID, owner);
            if (paramAnnotLambda != null) {
                if (!annotFunctionDefined) {
                    function = defineFunction(typeDef.pos, pkgID, owner);
                    mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
                    annotFunctionDefined = true;
                }

                addInvocationToLiteral(mapLiteral, FIELD + DOT + field.name.value,
                                       field.annAttachments.get(0).pos, paramAnnotLambda);
            }
        }

        if (annotFunctionDefined) {
            if (mapLiteral.fields.isEmpty()) {
                return null;
            }
            lambdaFunction = addReturnAndDefineLambda(function, mapLiteral, pkgNode, env, pkgID, owner);
        }

        return lambdaFunction;
    }

    private BLangLambdaFunction defineAnnotations(BLangFunction bLangFunction, BLangPackage pkgNode, SymbolEnv env,
                                                  PackageID pkgID, BSymbol owner) {
        BLangFunction function = null;
        BLangRecordLiteral mapLiteral = null;
        BLangLambdaFunction lambdaFunction = null;

        boolean annotFunctionDefined = false;
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(bLangFunction, bLangFunction.symbol.scope, env);

        if (!bLangFunction.annAttachments.isEmpty()) {
            function = defineFunction(bLangFunction.pos, pkgID, owner);
            mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
            addAnnotsToLiteral(bLangFunction.annAttachments, mapLiteral, function, funcEnv);
            annotFunctionDefined = true;
        }

        for (BLangSimpleVariable param : getParams(bLangFunction)) {
            BLangLambdaFunction paramAnnotLambda = defineAnnotations(param.annAttachments, param.pos, pkgNode, funcEnv,
                                                                     pkgID, owner);
            if (paramAnnotLambda != null) {
                if (!annotFunctionDefined) {
                    function = defineFunction(bLangFunction.pos, pkgID, owner);
                    mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
                    annotFunctionDefined = true;
                }

                addInvocationToLiteral(mapLiteral, PARAM + DOT + param.name.value,
                                       param.annAttachments.get(0).pos, paramAnnotLambda);
            }
        }

        if (!bLangFunction.returnTypeAnnAttachments.isEmpty()) {
            if (!annotFunctionDefined) {
                function = defineFunction(bLangFunction.pos, pkgID, owner);
                mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
                annotFunctionDefined = true;
            }

            BLangFunction retFunction = defineFunction(bLangFunction.pos, pkgID, owner);
            BLangRecordLiteral retMapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
            addAnnotsToLiteral(bLangFunction.returnTypeAnnAttachments, retMapLiteral, retFunction, funcEnv);
            BLangLambdaFunction returnAnnotLambda = addReturnAndDefineLambda(retFunction, retMapLiteral, pkgNode,
                                                                             env, pkgID, owner);
            addInvocationToLiteral(mapLiteral, RETURNS, bLangFunction.returnTypeAnnAttachments.get(0).pos,
                                   returnAnnotLambda);
        }

        if (annotFunctionDefined) {
            if (mapLiteral.fields.isEmpty()) {
                return null;
            }
            lambdaFunction = addReturnAndDefineLambda(function, mapLiteral, pkgNode, env, pkgID, owner);
        }

        return lambdaFunction;
    }

    private void addVarArgsAnnotation(BLangFunction mainFunc) {
        if (mainFunc.symbol.getParameters().isEmpty() && mainFunc.symbol.restParam == null) {
            return;
        }
        DiagnosticPos pos = mainFunc.pos;
        // Create Annotation Attachment.
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        mainFunc.addAnnotationAttachment(annoAttachment);
        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get(mainFunc.symbol.getEnclosingSymbol());
        BSymbol annSymbol = symResolver.lookupSymbolInAnnotationSpace(pkgEnv, names.fromString(DEFAULTABLE_ANN));
        if (annSymbol instanceof BAnnotationSymbol) {
            annoAttachment.annotationSymbol = (BAnnotationSymbol) annSymbol;
        }
        BLangIdentifier identifierNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        annoAttachment.annotationName = identifierNode;
        annoAttachment.annotationName.value = DEFAULTABLE_ANN;
        annoAttachment.pos = pos;
        annoAttachment.annotationName.pos = pos;
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();

        annoAttachment.expr = literalNode;
        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue(BUILTIN_PKG_KEY);
        annoAttachment.pkgAlias = pkgAlias;
        annoAttachment.attachPoints.add(AttachPoint.Point.FUNCTION);
        literalNode.pos = pos;
        BStructureTypeSymbol bStructSymbol = null;
        BSymbol annTypeSymbol = symResolver.lookupSymbolInMainSpace(pkgEnv, names.fromString(DEFAULTABLE_REC));
        if (annTypeSymbol instanceof BStructureTypeSymbol) {
            bStructSymbol = (BStructureTypeSymbol) annTypeSymbol;
            literalNode.type = bStructSymbol.type;
        }

        //Add Root Descriptor
        BLangRecordLiteral.BLangRecordKeyValueField descriptorKeyValue = (BLangRecordLiteral.BLangRecordKeyValueField)
                TreeBuilder.createRecordKeyValue();
        literalNode.fields.add(descriptorKeyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = ARG_NAMES;
        keyLiteral.type = symTable.stringType;

        BLangListConstructorExpr.BLangArrayLiteral valueLiteral = (BLangListConstructorExpr.BLangArrayLiteral)
                TreeBuilder.createArrayLiteralExpressionNode();
        valueLiteral.type = new BArrayType(symTable.stringType);
        valueLiteral.pos = pos;

        for (BVarSymbol varSymbol : mainFunc.symbol.getParameters()) {
            BLangLiteral str = (BLangLiteral) TreeBuilder.createLiteralExpression();
            str.value = varSymbol.name.value;
            str.type = symTable.stringType;
            valueLiteral.exprs.add(str);
        }

        if (mainFunc.symbol.restParam != null) {
            BLangLiteral str = (BLangLiteral) TreeBuilder.createLiteralExpression();
            str.value = mainFunc.symbol.restParam.name.value;
            str.type = symTable.stringType;
            valueLiteral.exprs.add(str);
        }
        descriptorKeyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        BSymbol fieldSymbol = symResolver.resolveStructField(mainFunc.pos, pkgEnv,
                names.fromString(ARG_NAMES), bStructSymbol);
        if (fieldSymbol instanceof BVarSymbol) {
            descriptorKeyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        }
        if (valueLiteral != null) {
            descriptorKeyValue.valueExpr = valueLiteral;
        }
    }

    private BLangFunction defineFunction(DiagnosticPos pos, PackageID pkgID, BSymbol owner) {
        String funcName = ANNOT_FUNC + annotFuncCount++;
        BLangFunction function = ASTBuilderUtil.createFunction(pos, funcName);
        function.type = new BInvokableType(Collections.emptyList(), symTable.mapType, null);

        BLangBuiltInRefTypeNode anyMapType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        anyMapType.typeKind = TypeKind.MAP;
        anyMapType.pos = pos;

        BLangValueType anyType = new BLangValueType();
        anyType.typeKind = TypeKind.ANY;

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = anyMapType;
        constrainedType.constraint = anyType;
        constrainedType.pos = pos;

        function.returnTypeNode = anyMapType;
        function.returnTypeNode.type = symTable.mapType;

        function.body = ASTBuilderUtil.createBlockFunctionBody(pos, new ArrayList<>());

        BInvokableSymbol functionSymbol = new BInvokableSymbol(SymTag.INVOKABLE, Flags.asMask(function.flagSet),
                                                               new Name(funcName), pkgID, function.type, owner,
                                                               function.name.pos, VIRTUAL);
        functionSymbol.bodyExist = true;
        functionSymbol.kind = SymbolKind.FUNCTION;

        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.scope = new Scope(functionSymbol);
        function.symbol = functionSymbol;
        return function;
    }

    private BLangLambdaFunction addReturnAndDefineLambda(BLangFunction function, BLangRecordLiteral mapLiteral,
                                                         BLangPackage pkgNode, SymbolEnv env, PackageID pkgID,
                                                         BSymbol owner) {
        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(function.pos,
                                                                 (BLangBlockFunctionBody) function.body);
        returnStmt.expr = mapLiteral;

        BInvokableSymbol lambdaFunctionSymbol = createInvokableSymbol(function, pkgID, owner);
        BLangLambdaFunction lambdaFunction = desugar.createLambdaFunction(function, lambdaFunctionSymbol);
        lambdaFunction.capturedClosureEnv = env.createClone();

        pkgNode.functions.add(function);
        pkgNode.topLevelNodes.add(function);
        pkgNode.lambdaFunctions.add(lambdaFunction);
        return lambdaFunction;
    }

    private void addAnnotsToLiteral(List<BLangAnnotationAttachment> nodeAttachments, BLangRecordLiteral mapLiteral,
                                    BLangFunction function, SymbolEnv env) {
        Map<BAnnotationSymbol, List<BLangAnnotationAttachment>> attachments = new HashMap<>();

        for (AnnotationAttachmentNode attachment : nodeAttachments) {
            BLangAnnotationAttachment annotationAttachment = (BLangAnnotationAttachment) attachment;
            desugar.rewrite(annotationAttachment, env);

            BAnnotationSymbol annotationSymbol = annotationAttachment.annotationSymbol;
            if (attachments.containsKey(annotationSymbol)) {
                attachments.get(annotationSymbol).add(annotationAttachment);
            } else {
                AttachPoint attachPoint = null;
                for (AttachPoint.Point point : annotationAttachment.attachPoints) {
                    Optional<AttachPoint> attachPointOptional = annotationSymbol.points.stream()
                            .filter(annotAttachPoint -> annotAttachPoint.point == point).findAny();

                    if (attachPointOptional.isPresent()) {
                        attachPoint = attachPointOptional.get();
                        break;
                    }
                }

                if (attachPoint == null || attachPoint.source) {
                    // Avoid defining annotation values for source only annotations.
                    continue;
                }

                attachments.put(annotationSymbol,
                                new ArrayList<BLangAnnotationAttachment>() {{
                                    add(annotationAttachment);
                                }});
            }
        }

        if (attachments.isEmpty()) {
            return;
        }

        for (BAnnotationSymbol annotationSymbol : attachments.keySet()) {
            BTypeSymbol attachedTypeSymbol = annotationSymbol.attachedType;
            if (attachedTypeSymbol == null ||
                    types.isAssignable(attachedTypeSymbol.type, symTable.trueType)) {
                // annotation v1 on type; OR annotation TRUE v1 on type;
                // @v1
                // type X record {
                //     int i;
                // };
                // // Adds
                // { ..., v1: true, ... }
                addTrueAnnot(attachments.get(annotationSymbol).get(0), mapLiteral);
            } else if (attachedTypeSymbol.type.tag != TypeTags.ARRAY) {
                // annotation FooRecord v1 on type; OR annotation map<anydata> v1 on type;
                // @v1 {
                //     value: 1
                // }
                // type X record {
                //     int i;
                // };
                // // Adds
                // { ..., v1: { value: 1 }, ... }
                addSingleAnnot(attachments.get(annotationSymbol).get(0), mapLiteral);
            } else {
                // annotation FooRecord[] v1 on type; OR annotation map<anydata>[] v1 on type;
                // @v1 {
                //     value: 1
                // }
                // @v1 {
                //     value: 2
                // }
                // type X record {
                //     int i;
                // };
                // // Adds
                // { ..., v1: [{ value: 1 }, { value: 2 }], ... }
                addAnnotArray(function.pos, annotationSymbol.bvmAlias(), attachedTypeSymbol.type,
                              attachments.get(annotationSymbol), mapLiteral);
            }
        }
    }

    private BInvokableSymbol createInvokableSymbol(BLangFunction function, PackageID pkgID, BSymbol owner) {
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                                                                       new Name(function.name.value),
                                                                       pkgID, function.type, owner, true, function.pos,
                                                                       VIRTUAL);
        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = new Scope(functionSymbol);
        functionSymbol.restParam = function.restParam != null ? function.restParam.symbol : null;
        functionSymbol.type = new BInvokableType(Collections.emptyList(),
                function.restParam != null ? function.restParam.type : null,
                new BMapType(TypeTags.MAP, symTable.anyType, null),
                null);
        function.symbol = functionSymbol;
        return functionSymbol;
    }

    private BLangSimpleVariable createGlobalAnnotationMapVar(BLangPackage pkgNode) {
        BLangSimpleVariable annotationMap = ASTBuilderUtil.createVariable(pkgNode.pos, ANNOTATION_DATA,
                                                                          symTable.mapType,
                                                                          ASTBuilderUtil.createEmptyRecordLiteral(
                                                                                  pkgNode.pos, symTable.mapType), null);
        ASTBuilderUtil.defineVariable(annotationMap, pkgNode.symbol, names);
        pkgNode.globalVars.add(0, annotationMap); // TODO fix this
        pkgNode.topLevelNodes.add(0, annotationMap);
        return annotationMap;
    }

    private void addTrueAnnot(BLangAnnotationAttachment attachment, BLangRecordLiteral recordLiteral) {
        // Handle scenarios where type is a subtype of `true` explicitly or implicitly (by omission).
        // add { ..., v1: true, ... }
        BLangExpression expression = ASTBuilderUtil.wrapToConversionExpr(symTable.trueType,
                                                                         ASTBuilderUtil.createLiteral(
                                                                                 attachment.pos, symTable.booleanType,
                                                                                 Boolean.TRUE),
                                                                         symTable, types);
        addAnnotValueToLiteral(recordLiteral, attachment.annotationSymbol.bvmAlias(), expression, attachment.pos);
    }

    private void addSingleAnnot(BLangAnnotationAttachment attachment, BLangRecordLiteral recordLiteral) {
        // Handle scenarios where type is a subtype of `map<any|error>` or `record{any|error...;}`.
        // create: add { ..., v1: { value: 1 } ... } or { ..., v1: C1 ... } where C1 is a constant reference
        addAnnotValueToLiteral(recordLiteral, attachment.annotationSymbol.bvmAlias(), attachment.expr, attachment.pos);
    }


    private void addAnnotArray(DiagnosticPos pos, String name, BType annotType,
                               List<BLangAnnotationAttachment> attachments, BLangRecordLiteral recordLiteral) {
        // Handle scenarios where type is a subtype of `map<any|error>[]` or `record{any|error...;}[]`.
        // Create an empty array literal of the expected type.
        BLangListConstructorExpr.BLangArrayLiteral arrayLiteral =
                ASTBuilderUtil.createEmptyArrayLiteral(pos, (BArrayType) annotType);

        // Add value to the array literal, [{ foo: 1, bar: "b" }, { foo: C1, bar: "b2" }, ...  ]
        attachments.forEach(attachment -> arrayLiteral.exprs.add(attachment.expr));

        // Add the array literal to the record literal of all annots. Where `v1` is the annot-tag,
        // { ..., v1: [{ foo: 1, bar: "b" }, { foo: C1, bar: "b2" }, ...  ], ... }
        addAnnotValueToLiteral(recordLiteral, name, arrayLiteral, pos);
    }

    private void addInvocationToGlobalAnnotMap(String identifier, BLangLambdaFunction lambdaFunction,
                                               BLangBlockStmt target) {
        // create: $annotation_data["identifier"] = $annot_func$.call();
        addAnnotValueAssignmentToMap(annotationMap, identifier, target,
                                     getInvocation(lambdaFunction));
    }

    private void addInvocationToGlobalAnnotMap(String identifier, BLangLambdaFunction lambdaFunction,
                                               BLangFunctionBody target) {
        // create: $annotation_data["identifier"] = $annot_func$.call();
        addAnnotValueAssignmentToMap(annotationMap, identifier, (BLangBlockFunctionBody) target,
                                     getInvocation(lambdaFunction));
    }

    private void addLambdaToGlobalAnnotMap(String identifier, BLangLambdaFunction lambdaFunction,
                                           BLangBlockStmt target) {
        // create: $annotation_data["identifier"] = $annot_func$;
        addAnnotValueAssignmentToMap(annotationMap, identifier, target,
                                     ASTBuilderUtil.createVariableRef(lambdaFunction.pos,
                                                                      lambdaFunction.function.symbol));
    }

    private void addInvocationToLiteral(BLangRecordLiteral recordLiteral, String identifier,
                                        DiagnosticPos pos, BLangLambdaFunction lambdaFunction) {
        BLangInvocation annotFuncInvocation = getInvocation(lambdaFunction);
        recordLiteral.fields.add(ASTBuilderUtil.createBLangRecordKeyValue(
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, identifier), annotFuncInvocation));
    }

    private void addAnnotValueAssignmentToMap(BLangSimpleVariable mapVar, String identifier,
                                              BlockNode target, DiagnosticPos targetPos,
                                              BLangExpression expression) {
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(targetPos, target);
        assignmentStmt.expr = expression;

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = targetPos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(targetPos, symTable.stringType, identifier);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(targetPos, mapVar.symbol);
        indexAccessNode.type = ((BMapType) mapVar.type).constraint;
        assignmentStmt.varRef = indexAccessNode;
    }

    private void addAnnotValueAssignmentToMap(BLangSimpleVariable mapVar, String identifier,
                                              BLangBlockFunctionBody target, BLangExpression expression) {
        addAnnotValueAssignmentToMap(mapVar, identifier, target, target.pos, expression);
    }

    private void addAnnotValueAssignmentToMap(BLangSimpleVariable mapVar, String identifier,
                                              BLangBlockStmt target, BLangExpression expression) {
        addAnnotValueAssignmentToMap(mapVar, identifier, target, target.pos, expression);
    }

    private void addAnnotValueToLiteral(BLangRecordLiteral recordLiteral, String identifier,
                                        BLangExpression expression, DiagnosticPos pos) {
        recordLiteral.fields.add(ASTBuilderUtil.createBLangRecordKeyValue(
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, identifier), expression));
    }

    private BLangInvocation getInvocation(BLangLambdaFunction lambdaFunction) {
        BLangInvocation funcInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        funcInvocation.type = symTable.mapType;
        funcInvocation.expr = null;
        BInvokableSymbol lambdaSymbol = lambdaFunction.function.symbol;
        funcInvocation.symbol = lambdaSymbol;
        funcInvocation.name = ASTBuilderUtil.createIdentifier(lambdaFunction.pos, lambdaSymbol.name.value);
        return funcInvocation;
    }

    private int calculateIndex(List<BLangStatement> statements, BLangService service) {
        for (int i = 0; i < statements.size(); i++) {
            BLangStatement stmt = statements.get(i);
            if ((stmt.getKind() == NodeKind.ASSIGNMENT) &&
                    (((BLangAssignment) stmt).expr.getKind() == NodeKind.SERVICE_CONSTRUCTOR) &&
                    ((BLangServiceConstructorExpr) ((BLangAssignment) stmt).expr).serviceNode == service) {
                return i;
            }
        }
        return statements.size();
    }

    private int calculateIndex(List<BLangStatement> statements, BTypeSymbol symbol) {
        for (int i = 0; i < statements.size(); i++) {
            BLangStatement stmt = statements.get(i);
            if ((stmt.getKind() == NodeKind.ASSIGNMENT) &&
                    (((BLangAssignment) stmt).expr.getKind() == NodeKind.SERVICE_CONSTRUCTOR) &&
                    ((BLangServiceConstructorExpr) ((BLangAssignment) stmt).expr).type.tsymbol == symbol) {
                return i;
            }
        }
        return statements.size();
    }

    private List<BLangSimpleVariable> getParams(BLangFunction function) {
        List<BLangSimpleVariable> params = new ArrayList<>(function.getParameters());

        if (function.restParam != null) {
            params.add(function.restParam);
        }

        return params;
    }
}
