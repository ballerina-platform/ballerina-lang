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

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.ConstantValueResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
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
    public static final String LANG_ANNOT_PKG_KEY = "ballerina" + "/" + "lang.annotations";
    public static final String DEFAULTABLE_ANN = "DefaultableArgs";
    public static final String DEFAULTABLE_REC = "ArgsData";
    public static final String ARG_NAMES = "args";
    public static final String SERVICE_INTROSPECTION_INFO_ANN = "IntrospectionDocConfig";
    public static final String SERVICE_INTROSPECTION_INFO_REC = "ServiceIntrospectionDocConfig";
    public static final String SERVICE_NAME = "name";
    private static final String DOT = ".";
    private static final String FIELD = "$field$";
    private static final String PARAM = "$param$";
    private static final String RETURNS = "$returns$";
    public static final String STRAND = "strand";
    public static final String THREAD = "thread";
    public static final String STRAND_DATA = "StrandData";
    private BLangSimpleVariable annotationMap;
    private int annotFuncCount = 0;

    private static final CompilerContext.Key<AnnotationDesugar> ANNOTATION_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final Desugar desugar;
    private final SymbolTable symTable;
    private final Types types;
    private final Names names;
    private SymbolResolver symResolver;
    private ConstantValueResolver constantValueResolver;
    private ClosureGenerator closureGenerator;

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
        this.constantValueResolver = ConstantValueResolver.getInstance(context);
        this.closureGenerator = ClosureGenerator.getInstance(context);
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
        defineClassAnnotations(pkgNode, env, initFunction);
        defineFunctionAnnotations(pkgNode, env, initFunction);
    }

    private void defineClassAnnotations(BLangPackage pkgNode, SymbolEnv env2, BLangFunction initFunction) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;

        for (int i = 0, topLevelNodesSize = topLevelNodes.size(); i < topLevelNodesSize; i++) {
            TopLevelNode topLevelNode = topLevelNodes.get(i);
            if (topLevelNode.getKind() != NodeKind.CLASS_DEFN) {
                continue;
            }

            BLangBlockFunctionBody initBody = (BLangBlockFunctionBody) initFunction.body;
            boolean normalMode = true;
            BLangClassDefinition classDef = (BLangClassDefinition) topLevelNode;
            if (isServiceDeclaration(classDef)) {
                addIntrospectionInfoAnnotation(classDef, env2);
            }

            PackageID pkgID = classDef.symbol.pkgID;
            BSymbol owner = classDef.symbol.owner;
            SymbolEnv env = env2;
            Scope scope = initFunction.symbol.scope;
            Location pos = classDef.pos;
            if (classDef.flagSet.contains(Flag.OBJECT_CTOR)) {
                SymbolEnv closureEnv = classDef.oceEnvData.capturedClosureEnv;
                // check if local or top level object ctor expression
                if (closureEnv != null && closureEnv.enclEnv != null && closureEnv.enclEnv.enclInvokable != null) {
                    BLangInvokableNode invokableNode = closureEnv.enclEnv.enclInvokable;
                    // ctor expression
                    normalMode = false;
                    pos = invokableNode.pos;
                    scope = invokableNode.symbol.scope;
                    owner = invokableNode.symbol;
                    pkgID = invokableNode.symbol.pkgID;
                    BLangFunction functionWeCreateOce = (BLangFunction) invokableNode;
                    if (functionWeCreateOce.body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
                        initBody = (BLangBlockFunctionBody) functionWeCreateOce.body;
                        pos = functionWeCreateOce.body.pos;
                    }
                }
            }

            SymbolEnv classEnv = SymbolEnv.createClassEnv(classDef, scope, env);
            BLangLambdaFunction lambdaFunction = defineAnnotations(pos, classDef, pkgNode, classEnv, pkgID,
                    owner);
            if (lambdaFunction != null) {
                BType type = classDef.getBType();
                if (Symbols.isFlagOn(type.flags, Flags.OBJECT_CTOR)) {
                    if (normalMode) {
                        // Add the lambda/invocation in a temporary block.
                        BLangBlockStmt target = (BLangBlockStmt) TreeBuilder.createBlockNode();
                        target.pos = initBody.pos;
                        addLambdaToGlobalAnnotMap(classDef.name.value, lambdaFunction, target);
                        int index = calculateIndex((initBody).stmts, type.tsymbol);
                        for (BLangStatement stmt : target.stmts) {
                            initBody.stmts.add(index++, stmt);
                        }
                    } else {
                        // Add the lambda/invocation in a temporary block.
                        LocationData locationData = new LocationData(pkgID, owner, pos, initBody);
                        addAnnotationLambdaToGlobalAnnotationMapWithBlockDef(pkgNode, classDef, locationData,
                                lambdaFunction);
                    }
                } else {
                    addInvocationToGlobalAnnotMap(classDef.name.value, lambdaFunction, initBody);
                }
            }
        }
    }

    private void addAnnotationLambdaToGlobalAnnotationMapWithBlockDef(BLangPackage pkgNode,
                                                                      BLangClassDefinition classDef,
                                                                      LocationData location,
                                                                      BLangLambdaFunction lambdaFunction) {
        BLangBlockStmt target = (BLangBlockStmt) TreeBuilder.createBlockNode();
        PackageID pkgID = location.pkgID;
        Location pos = location.pos;
        BSymbol owner = location.owner;

        BLangBlockFunctionBody initBody = location.body;
        target.pos = pos;

        String lamdaName = "$anon" + classDef.name.value + "_lambda_" + lambdaFunction.function.name.value;
        BVarSymbol symbolVar = new BVarSymbol(0, names.fromString(lamdaName), pkgID,
                lambdaFunction.getBType(), owner, pos, VIRTUAL);

        // function () returns (map) $anon$anonType$_1_lambda = LambdaRef:$annot_func$_0
        BLangSimpleVariable funcVar = ASTBuilderUtil.createVariable(pos, lamdaName,
                lambdaFunction.getBType(), lambdaFunction, symbolVar);
        BLangSimpleVariableDef funcVarDef = ASTBuilderUtil.createVariableDef(funcVar.pos, funcVar);
        funcVarDef.setBType(lambdaFunction.getBType());
        target.stmts.add(funcVarDef);

        // $anon$anonType$_1_lamda
        BLangSimpleVarRef simpleVarRef = ASTBuilderUtil.createVariableRef(funcVar.pos, funcVar.symbol);
        simpleVarRef.pkgSymbol = pkgNode.symbol;
        funcVarDef.parent = initBody;
        lambdaFunction.parent = initBody;
        lambdaFunction.function.parent = initBody;

        // $annotation_data[$anonType$_1]
        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(pos, symTable.stringType,
                StringEscapeUtils.unescapeJava(classDef.name.value));
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(pos, annotationMap.symbol);
        indexAccessNode.setBType(((BMapType) annotationMap.getBType()).constraint);

        // $annotation_data[$anonType$_1] = $anon$anonType$_1_lamda
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(pos, target);
        assignmentStmt.expr = simpleVarRef;
        assignmentStmt.varRef = indexAccessNode;

        simpleVarRef.parent = initBody;
        assignmentStmt.parent = initBody;

        int index = calculateOCEExprIndex(initBody.stmts, classDef.getBType().tsymbol);
        for (BLangStatement stmt : target.stmts) {
            initBody.stmts.add(index++, stmt);
        }
    }

    private boolean isServiceDeclaration(BLangClassDefinition serviceClass) {
        return serviceClass.getFlags().contains(Flag.SERVICE) && serviceClass.isServiceDecl;
    }

    private void addIntrospectionInfoAnnotation(BLangClassDefinition serviceClass, SymbolEnv env) {
        Location position = serviceClass.pos;

        // Create Annotation Attachment.
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get(serviceClass.symbol.getEnclosingSymbol());
        BSymbol annSymbol = symResolver.lookupSymbolInAnnotationSpace(symTable.pkgEnvMap.get(symTable.rootPkgSymbol),
                names.fromString(SERVICE_INTROSPECTION_INFO_ANN));
        annSymbol.origin = SymbolOrigin.BUILTIN;
        if (annSymbol instanceof BAnnotationSymbol) {
            annoAttachment.annotationSymbol = (BAnnotationSymbol) annSymbol;
        }

        BLangIdentifier identifierNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        annoAttachment.annotationName = identifierNode;
        annoAttachment.annotationName.value = SERVICE_INTROSPECTION_INFO_ANN;
        annoAttachment.pos = position;
        annoAttachment.annotationName.pos = position;

        // create record literal related to annotation node
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        annoAttachment.expr = literalNode;
        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue(LANG_ANNOT_PKG_KEY);
        annoAttachment.pkgAlias = pkgAlias;
        annoAttachment.attachPoints.add(AttachPoint.Point.SERVICE);
        literalNode.pos = position;
        BSymbol annTypeSymbol = symResolver.lookupSymbolInMainSpace(
                pkgEnv, names.fromString(SERVICE_INTROSPECTION_INFO_REC));
        BStructureTypeSymbol bStructSymbol = (BStructureTypeSymbol) annTypeSymbol.type.tsymbol;
        literalNode.setBType(bStructSymbol.type);
        literalNode.typeChecked = true;

        //Add Root Descriptor
        BLangRecordLiteral.BLangRecordKeyValueField descriptorKeyValue = (BLangRecordLiteral.BLangRecordKeyValueField)
                TreeBuilder.createRecordKeyValue();
        literalNode.fields.add(descriptorKeyValue);

        // create annotation field key for `name`
        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = SERVICE_NAME;
        keyLiteral.setBType(symTable.stringType);
        keyLiteral.typeChecked = true;

        // create annotation field value for `name`
        BLangLiteral valueLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        valueLiteral.setBType(symTable.stringType);
        valueLiteral.value = generateServiceHashCode(serviceClass);
        valueLiteral.pos = position;
        valueLiteral.typeChecked = true;

        descriptorKeyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        BSymbol fieldSymbol = symResolver.resolveStructField(position, pkgEnv,
                names.fromString(SERVICE_NAME), bStructSymbol);

        if (fieldSymbol instanceof BVarSymbol) {
            descriptorKeyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        }
        if (valueLiteral != null) {
            descriptorKeyValue.valueExpr = valueLiteral;
        }

        symResolver.populateAnnotationAttachmentSymbol(annoAttachment, env, constantValueResolver);

        // add generated annotation to the service definition
        serviceClass.addAnnotationAttachment(annoAttachment);
        ((BClassSymbol) serviceClass.symbol).addAnnotation(annoAttachment.annotationAttachmentSymbol);
    }

    private String generateServiceHashCode(BLangClassDefinition serviceClass) {
        String serviceName = "service$" + serviceClass.name.getValue();
        PackageID moduleId = serviceClass.symbol.pkgID;
        LineRange lineRange = serviceClass.pos.lineRange();
        return String.format("%d", Objects.hash(serviceName, moduleId, lineRange));
    }

    private BLangLambdaFunction defineAnnotations(Location pos, BLangClassDefinition classDef, BLangPackage pkgNode,
                                                  SymbolEnv env, PackageID pkgID, BSymbol owner) {
        BLangFunction function = null;
        BLangRecordLiteral mapLiteral = null;
        boolean isLocalObjectCtor = classDef.flagSet.contains(Flag.OBJECT_CTOR);
        if (!classDef.annAttachments.isEmpty()) {
            function = defineFunction(pos, pkgID, owner);
            mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(pos, symTable.mapType);
            addAnnotsToLiteral(classDef.annAttachments, mapLiteral, pos, env, isLocalObjectCtor);
        }

        for (BLangSimpleVariable field : classDef.fields) {
            BLangLambdaFunction paramAnnotLambda =
                    defineAnnotations(field.annAttachments, pos, pkgNode, env, pkgID, owner, isLocalObjectCtor);
            if (paramAnnotLambda == null) {
                continue;
            }

            if (function == null) {
                function = defineFunction(pos, pkgID, owner);
                mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(pos, symTable.mapType);
            }

            String fieldName = FIELD + DOT + field.name.value;
            addInvocationToLiteral(mapLiteral, fieldName, field.annAttachments.get(0).pos, paramAnnotLambda);
        }

        if (function != null && !mapLiteral.fields.isEmpty()) {
            return addReturnAndDefineLambda(function, mapLiteral, pkgNode, env, pkgID, owner);
        }
        return null;
    }

    private Map<BAnnotationSymbol, List<BLangAnnotationAttachment>>
    collectAnnotationAttachments(List<BLangAnnotationAttachment> nodeAttachments, SymbolEnv env) {
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
        return attachments;
    }

    void defineStatementAnnotations(List<BLangAnnotationAttachment> attachments,
                                    Location location,
                                    PackageID pkgID,
                                    BSymbol owner,
                                    SymbolEnv env) {
        BLangFunction function = defineFunction(location, pkgID, owner);
        BLangRecordLiteral mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
        addAnnotsToLiteral(attachments, mapLiteral, location, env, false);
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

            if (typeNode.getKind() == NodeKind.RECORD_TYPE || typeNode.getKind() == NodeKind.OBJECT_TYPE
                    || typeNode.getKind() == NodeKind.TUPLE_TYPE_NODE) {
                lambdaFunction = defineAnnotations(typeDef, pkgNode, typeEnv, pkgID, owner);
            } else {
                lambdaFunction = defineAnnotations(typeDef, typeDef.pos, pkgNode, typeEnv, pkgID, owner);
            }

            if (lambdaFunction != null) {
                addInvocationToGlobalAnnotMap(typeDef.name.value, lambdaFunction, initFunction.body);
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
                addVarArgsAnnotation(function, env);
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
                if (function.attachedFunction
                        && Symbols.isFlagOn(function.receiver.getBType().flags, Flags.OBJECT_CTOR)) {
                    addLambdaToGlobalAnnotMap(identifier, lambdaFunction, target);
                    index = calculateIndex(initFnBody.stmts, function.receiver.getBType().tsymbol);
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
            if (annotation.expr == null) {
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

    private BLangLambdaFunction defineAnnotations(AnnotatableNode node, Location location,
                                                  BLangPackage pkgNode, SymbolEnv env, PackageID pkgID, BSymbol owner) {
        return defineAnnotations(getAnnotationList(node), location, pkgNode, env, pkgID, owner, false);
    }

    private List<BLangAnnotationAttachment> getAnnotationList(AnnotatableNode node) {
        return node.getAnnotationAttachments().stream()
                .map(annotAttachment -> (BLangAnnotationAttachment) annotAttachment)
                .collect(Collectors.toList());
    }

    private BLangLambdaFunction defineAnnotations(List<BLangAnnotationAttachment> annAttachments,
                                                  Location location,
                                                  BLangPackage pkgNode,
                                                  SymbolEnv env,
                                                  PackageID pkgID,
                                                  BSymbol owner, boolean isLocalObjectCtor) {
        if (annAttachments.isEmpty()) {
            return null;
        }

        BLangFunction function = defineFunction(location, pkgID, owner);
        BLangRecordLiteral mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(function, function.symbol.scope, env);
        addAnnotsToLiteral(annAttachments, mapLiteral, location, funcEnv, isLocalObjectCtor);

        if (mapLiteral.fields.isEmpty()) {
            return null;
        }

        return addReturnAndDefineLambda(function, mapLiteral, pkgNode, env, pkgID, owner);
    }

    private BLangLambdaFunction defineAnnotations(BLangTypeDefinition typeDef, BLangPackage pkgNode, SymbolEnv env,
                                                  PackageID pkgID, BSymbol owner) {
        BLangFunction function = null;
        BLangRecordLiteral mapLiteral = null;

        if (!typeDef.annAttachments.isEmpty()) {
            function = defineFunction(typeDef.pos, pkgID, owner);
            mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
            addAnnotsToLiteral(typeDef.annAttachments, mapLiteral, typeDef.pos, env, false);
        }
        List<BLangSimpleVariable> fields;
        if (typeDef.typeNode.getKind() == NodeKind.TUPLE_TYPE_NODE) {
            fields = ((BLangTupleTypeNode) typeDef.typeNode).members;
        } else {
            fields = ((BLangStructureTypeNode) typeDef.typeNode).fields;
        }
        for (BLangSimpleVariable field : fields) {
            BLangLambdaFunction paramAnnotLambda = defineAnnotations(field.annAttachments, field.pos, pkgNode, env,
                                                                     pkgID, owner, false);
            if (paramAnnotLambda != null) {
                if (function == null) {
                    function = defineFunction(typeDef.pos, pkgID, owner);
                    mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
                }

                addInvocationToLiteral(mapLiteral, FIELD + DOT + field.name.value,
                                       field.annAttachments.get(0).pos, paramAnnotLambda);
            }
        }

        if (function == null || mapLiteral.fields.isEmpty()) {
            return null;
        }
        return addReturnAndDefineLambda(function, mapLiteral, pkgNode, env, pkgID, owner);
    }

    public BLangLambdaFunction defineFieldAnnotations(List<BLangSimpleVariable> fields, Location pos,
                                                      BLangPackage pkgNode, SymbolEnv env, PackageID pkgID,
                                                      BSymbol owner) {
        BLangFunction function = null;
        BLangRecordLiteral mapLiteral = null;
        BLangLambdaFunction lambdaFunction = null;

        boolean annotFunctionDefined = false;

        for (BLangSimpleVariable field : fields) {
            BLangLambdaFunction fieldAnnotLambda = defineAnnotations(field.annAttachments, field.pos, pkgNode, env,
                                                                     pkgID, owner, false);
            if (fieldAnnotLambda != null) {
                BInvokableSymbol invokableSymbol =
                        closureGenerator.createSimpleVariable(fieldAnnotLambda.function, fieldAnnotLambda,
                                                              owner.getKind() == SymbolKind.PACKAGE);
                env.scope.define(invokableSymbol.name, invokableSymbol);
                if (!annotFunctionDefined) {
                    function = defineFunction(pos, pkgID, owner);
                    mapLiteral = ASTBuilderUtil.createEmptyRecordLiteral(function.pos, symTable.mapType);
                    annotFunctionDefined = true;
                }
                addInvocationToLiteral(mapLiteral, FIELD + DOT + field.name.value,
                                       field.annAttachments.get(0).pos, invokableSymbol);
            }
        }

        if (annotFunctionDefined) {
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
            addAnnotsToLiteral(bLangFunction.annAttachments, mapLiteral, bLangFunction.pos, funcEnv, false);
            annotFunctionDefined = true;
        }

        for (BLangSimpleVariable param : getParams(bLangFunction)) {
            BLangLambdaFunction paramAnnotLambda = defineAnnotations(param.annAttachments, param.pos, pkgNode, funcEnv,
                                                                     pkgID, owner, false);
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
            addAnnotsToLiteral(bLangFunction.returnTypeAnnAttachments, retMapLiteral, bLangFunction.pos, funcEnv,
                               false);
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

    private void addVarArgsAnnotation(BLangFunction mainFunc, SymbolEnv env) {
        if (mainFunc.symbol.getParameters().isEmpty() && mainFunc.symbol.restParam == null) {
            return;
        }
        Location pos = mainFunc.pos;
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
        BSymbol annTypeSymbol = symResolver.lookupSymbolInMainSpace(pkgEnv, names.fromString(DEFAULTABLE_REC));
        BStructureTypeSymbol bStructSymbol = (BStructureTypeSymbol) annTypeSymbol.type.tsymbol;
        literalNode.setBType(annTypeSymbol.type);
        literalNode.typeChecked = true;

        //Add Root Descriptor
        BLangRecordLiteral.BLangRecordKeyValueField descriptorKeyValue = (BLangRecordLiteral.BLangRecordKeyValueField)
                TreeBuilder.createRecordKeyValue();
        literalNode.fields.add(descriptorKeyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = ARG_NAMES;
        keyLiteral.setBType(symTable.stringType);
        keyLiteral.typeChecked = true;

        BLangListConstructorExpr.BLangArrayLiteral valueLiteral = (BLangListConstructorExpr.BLangArrayLiteral)
                TreeBuilder.createArrayLiteralExpressionNode();
        valueLiteral.setBType(new BArrayType(symTable.stringType));
        valueLiteral.typeChecked = true;
        valueLiteral.pos = pos;

        for (BVarSymbol varSymbol : mainFunc.symbol.getParameters()) {
            BLangLiteral str = (BLangLiteral) TreeBuilder.createLiteralExpression();
            str.value = varSymbol.name.value;
            str.setBType(symTable.stringType);
            str.typeChecked = true;
            valueLiteral.exprs.add(str);
        }

        if (mainFunc.symbol.restParam != null) {
            BLangLiteral str = (BLangLiteral) TreeBuilder.createLiteralExpression();
            str.value = mainFunc.symbol.restParam.name.value;
            str.setBType(symTable.stringType);
            str.typeChecked = true;
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

        symResolver.populateAnnotationAttachmentSymbol(annoAttachment, env, constantValueResolver);
        ((List<BAnnotationAttachmentSymbol>) mainFunc.symbol.getAnnotations()).add(
                annoAttachment.annotationAttachmentSymbol);
    }

    private BLangFunction defineFunction(Location pos, PackageID pkgID, BSymbol owner) {
        String funcName = ANNOT_FUNC + UNDERSCORE + annotFuncCount++;
        BLangFunction function = ASTBuilderUtil.createFunction(pos, funcName);
        function.setBType(new BInvokableType(Collections.emptyList(), symTable.mapType, null));
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
        function.returnTypeNode.setBType(symTable.mapType);

        function.body = ASTBuilderUtil.createBlockFunctionBody(pos, new ArrayList<>());

        BInvokableSymbol functionSymbol = new BInvokableSymbol(SymTag.INVOKABLE, Flags.asMask(function.flagSet),
                                                               new Name(funcName), pkgID, function.getBType(), owner,
                                                               function.name.pos, VIRTUAL);
        functionSymbol.bodyExist = true;
        functionSymbol.kind = SymbolKind.FUNCTION;

        functionSymbol.retType = function.returnTypeNode.getBType();
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
        BLangLambdaFunction lambdaFunction = desugar.createLambdaFunction(function, lambdaFunctionSymbol, env);
        lambdaFunction.capturedClosureEnv = env.createClone();

        pkgNode.functions.add(function);
        pkgNode.topLevelNodes.add(function);
        pkgNode.lambdaFunctions.add(lambdaFunction);
        return lambdaFunction;
    }

    public void addAnnotsToLiteral(List<BLangAnnotationAttachment> nodeAttachments, BLangRecordLiteral mapLiteral,
                                    Location pos, SymbolEnv env, boolean isLocalObjectCtor) {
        Map<BAnnotationSymbol, List<BLangAnnotationAttachment>> attachments =
                collectAnnotationAttachments(nodeAttachments, env);

        if (attachments.isEmpty()) {
            return;
        }

        for (BAnnotationSymbol annotationSymbol : attachments.keySet()) {
            BType attachedType = annotationSymbol.attachedType;
            if (attachedType == null ||
                    types.isAssignable(attachedType, symTable.trueType)) {
                // annotation v1 on type; OR annotation TRUE v1 on type;
                // @v1
                // type X record {
                //     int i;
                // };
                // // Adds
                // { ..., v1: true, ... }
                addTrueAnnot(attachments.get(annotationSymbol).get(0), mapLiteral, isLocalObjectCtor);
            } else if (attachedType.tag != TypeTags.ARRAY) {
                // annotation FooRecord v1 on type; OR annotation map<anydata> v1 on type;
                // @v1 {
                //     value: 1
                // }
                // type X record {
                //     int i;
                // };
                // // Adds
                // { ..., v1: { value: 1 }, ... }
                addSingleAnnot(attachments.get(annotationSymbol).get(0), mapLiteral, isLocalObjectCtor);
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
                addAnnotArray(pos, annotationSymbol.bvmAlias(), attachedType,
                              attachments.get(annotationSymbol), mapLiteral, isLocalObjectCtor);
            }
        }
    }

    private BInvokableSymbol createInvokableSymbol(BLangFunction function, PackageID pkgID, BSymbol owner) {
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                                                                       new Name(function.name.value),
                                                                       new Name(function.name.originalValue),
                                                                       pkgID, function.getBType(), owner, true,
                                                                       function.pos, VIRTUAL);
        functionSymbol.retType = function.returnTypeNode.getBType();
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = new Scope(functionSymbol);
        functionSymbol.restParam = function.restParam != null ? function.restParam.symbol : null;
        functionSymbol.type = new BInvokableType(Collections.emptyList(),
                function.restParam != null ? function.restParam.getBType() : null,
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

    private void addTrueAnnot(BLangAnnotationAttachment attachment, BLangRecordLiteral recordLiteral,
                              boolean isLocalObjectCtor) {
        // Handle scenarios where type is a subtype of `true` explicitly or implicitly (by omission).
        // add { ..., v1: true, ... }
        BLangExpression expression = ASTBuilderUtil.wrapToConversionExpr(symTable.trueType,
                                                                         ASTBuilderUtil.createLiteral(
                                                                                 attachment.pos, symTable.booleanType,
                                                                                 Boolean.TRUE),
                                                                         symTable, types);
        addAnnotValueToLiteral(recordLiteral, attachment.annotationSymbol.bvmAlias(), expression, attachment.pos);
    }

    private void addSingleAnnot(BLangAnnotationAttachment attachment, BLangRecordLiteral recordLiteral,
                                boolean isLocalObjectCtor) {
        // Handle scenarios where type is a subtype of `map<any|error>` or `record{any|error...;}`.
        // create: add { ..., v1: { value: 1 } ... } or { ..., v1: C1 ... } where C1 is a constant reference
        addAnnotValueToLiteral(recordLiteral, attachment.annotationSymbol.bvmAlias(), attachment.expr, attachment.pos);
    }


    private void addAnnotArray(Location pos, String name, BType annotType,
                               List<BLangAnnotationAttachment> attachments, BLangRecordLiteral recordLiteral,
                               boolean isLocalObjectCtor) {
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
                                        Location pos, BLangLambdaFunction lambdaFunction) {
        BLangInvocation annotFuncInvocation = getInvocation(lambdaFunction);
        recordLiteral.fields.add(ASTBuilderUtil.createBLangRecordKeyValue(
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, identifier), annotFuncInvocation));
    }

    private void addInvocationToLiteral(BLangRecordLiteral recordLiteral, String identifier,
                                        Location pos, BInvokableSymbol invokableSymbol) {
        BLangInvocation annotFuncInvocation = getInvocation(invokableSymbol);
        recordLiteral.fields.add(ASTBuilderUtil.createBLangRecordKeyValue(
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, identifier), annotFuncInvocation));
    }

    private BLangInvocation getInvocation(BInvokableSymbol symbol) {
        BLangInvocation funcInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        funcInvocation.setBType(symbol.retType);
        funcInvocation.symbol = symbol;
        funcInvocation.name = ASTBuilderUtil.createIdentifier(symbol.pos, symbol.name.value);
        funcInvocation.functionPointerInvocation = true;
        return funcInvocation;
    }

    private void addAnnotValueAssignmentToMap(BLangSimpleVariable mapVar, String identifier,
                                              BlockNode target, Location targetPos,
                                              BLangExpression expression) {
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(targetPos, target);
        assignmentStmt.expr = expression;

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = targetPos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(targetPos, symTable.stringType,
                StringEscapeUtils.unescapeJava(identifier));
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(targetPos, mapVar.symbol);
        indexAccessNode.setBType(((BMapType) mapVar.getBType()).constraint);
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
                                        BLangExpression expression, Location pos) {
        recordLiteral.fields.add(ASTBuilderUtil.createBLangRecordKeyValue(
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, identifier), expression));
    }

    private BLangInvocation getInvocation(BLangLambdaFunction lambdaFunction) {
        BLangInvocation funcInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        funcInvocation.setBType(symTable.mapType);
        funcInvocation.expr = null;
        BInvokableSymbol lambdaSymbol = lambdaFunction.function.symbol;
        funcInvocation.symbol = lambdaSymbol;
        funcInvocation.name = ASTBuilderUtil.createIdentifier(lambdaFunction.pos, lambdaSymbol.name.value);
        return funcInvocation;
    }

    private int calculateIndex(List<BLangStatement> statements, BTypeSymbol symbol) {
        return calculateOCEExprIndex(statements, symbol);
//        for (int i = 0; i < statements.size(); i++) {
//            BLangStatement stmt = statements.get(i);
//
//            if (stmt.getKind() != NodeKind.ASSIGNMENT) {
//                continue;
//            }
//
//            BLangExpression expr = ((BLangAssignment) stmt).expr;
//            if ((desugar.isMappingOrObjectConstructorOrObjInit(expr))
//                    && isMappingOrObjectCtorOrObjInitWithSymbol(expr, symbol)) {
//                return i;
//            }
//        }
//        return statements.size();
    }

    private int calculateOCEExprIndex(List<BLangStatement> statements, BTypeSymbol symbol) {
        for (int i = 0; i < statements.size(); i++) {
            BLangStatement stmt = statements.get(i);
            NodeKind stmtKind = stmt.getKind();
            if (stmtKind == NodeKind.RETURN) {
                return i;
            }
            BLangExpression expr = null;
            if (stmtKind == NodeKind.VARIABLE_DEF) {
                BLangSimpleVariable variable = ((BLangSimpleVariableDef) stmt).var;
                expr  = variable.expr;
            } else if (stmtKind == NodeKind.ASSIGNMENT) {
                expr = ((BLangAssignment) stmt).expr;
            }

            if (expr != null && desugar.isMappingOrObjectConstructorOrObjInit(expr)
                    && isMappingOrObjectCtorOrObjInitWithSymbol(expr, symbol)) {
                return i;
            }
        }
        return statements.size();
    }

    private boolean isMappingOrObjectCtorOrObjInitWithSymbol(BLangExpression expr, BTypeSymbol symbol) {
        if (expr.getKind() == NodeKind.CHECK_EXPR) {
            return isMappingOrObjectCtorOrObjInitWithSymbol(((BLangCheckedExpr) expr).expr, symbol);
        } else if (expr.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            return isMappingOrObjectCtorOrObjInitWithSymbol(((BLangTypeConversionExpr) expr).expr, symbol);
        }
        return hasTypeSymbol(symbol, expr.getBType());
    }

    private boolean hasTypeSymbol(BTypeSymbol symbol, BType type) {
        BType bType = Types.getReferredType(type);
        if (bType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) bType).getMemberTypes()) {
                if (hasTypeSymbol(symbol, memberType)) {
                    return true;
                }
            }
        }
        return bType.tsymbol == symbol;
    }

    private List<BLangSimpleVariable> getParams(BLangFunction function) {
        List<BLangSimpleVariable> params = new ArrayList<>(function.getParameters());

        if (function.restParam != null) {
            params.add(function.restParam);
        }

        return params;
    }

    public BLangAnnotationAttachment createStrandAnnotationWithThreadAny(Location position, SymbolEnv env) {
        BLangAnnotationAttachment annotAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        annotAttachment.annotationSymbol = symResolver.getStrandAnnotationSymbol();

        annotAttachment.annotationName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        annotAttachment.annotationName.value = STRAND;
        annotAttachment.pos = position;
        annotAttachment.annotationName.pos = position;

        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue(LANG_ANNOT_PKG_KEY);
        annotAttachment.pkgAlias = pkgAlias;
        annotAttachment.attachPoints.add(AttachPoint.Point.WORKER);

        BLangRecordLiteral strandDataRecord = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        annotAttachment.expr = strandDataRecord;
        strandDataRecord.pos = position;
        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get(symTable.rootPkgSymbol);
        BSymbol annTypeSymbol = symResolver.lookupSymbolInMainSpace(pkgEnv, Names.fromString(STRAND_DATA));
        BStructureTypeSymbol bStructSymbol = (BStructureTypeSymbol) annTypeSymbol.type.tsymbol;
        strandDataRecord.setBType(bStructSymbol.type);
        strandDataRecord.typeChecked = true;

        BLangRecordLiteral.BLangRecordKeyValueField threadFieldKeyValue =
                (BLangRecordLiteral.BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
        strandDataRecord.fields.add(threadFieldKeyValue);

        BLangLiteral threadKey = (BLangLiteral) TreeBuilder.createLiteralExpression();
        threadKey.value = THREAD;
        threadKey.setBType(symTable.stringType);
        threadKey.typeChecked = true;

        BLangLiteral threadValue = (BLangLiteral) TreeBuilder.createLiteralExpression();
        threadValue.setBType(symTable.stringType);
        threadValue.value = "any";
        threadValue.pos = position;
        threadValue.typeChecked = true;

        threadFieldKeyValue.key = new BLangRecordLiteral.BLangRecordKey(threadKey);
        BSymbol fieldSymbol = symResolver.resolveStructField(position, pkgEnv, Names.fromString(THREAD),
                bStructSymbol);
        threadFieldKeyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        threadFieldKeyValue.valueExpr = threadValue;

        symResolver.populateAnnotationAttachmentSymbol(annotAttachment, env, constantValueResolver);
        return annotAttachment;
    }

    private class LocationData {
        public PackageID pkgID;
        public BSymbol owner;
        public Location pos;
        public BLangBlockFunctionBody body;

        public LocationData(PackageID pkgID, BSymbol owner, Location pos, BLangBlockFunctionBody body) {
            this.pkgID = pkgID;
            this.owner = owner;
            this.pos = pos;
            this.body = body;
        }

        @Override
        public String toString() {
            return "LocationData{" +
                    "pkgID=" + pkgID +
                    ", owner=" + owner +
                    ", pos=" + pos +
                    ", body=" + body +
                    '}';
        }
    }
}
