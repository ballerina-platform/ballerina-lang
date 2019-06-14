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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.BLangBuiltInMethod;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
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

/**
 * Desugar annotations into executable entries.
 *
 * @since 0.965.0
 */
public class AnnotationDesugar {

    public static final String ANNOTATION_DATA = "$annotation_data";
    private static final String ANNOT_FUNC = "$annot_func$";
    private static final String LOCAL_ANNOT_MAP = "$local_annot_map$";
    private static final String DOT = ".";
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

        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(pkgNode.pos, symTable.nilType);
        pkgNode.initFunction.body.stmts.add(returnStmt);
    }

    private void defineTypeAnnotations(BLangPackage pkgNode, SymbolEnv env, BLangFunction initFunction) {
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            PackageID pkgID = typeDef.symbol.pkgID;
            BSymbol owner = typeDef.symbol.owner;

            BLangLambdaFunction lambdaFunction = defineAnnotations(typeDef, typeDef.pos, pkgNode, env, pkgID, owner);
            if (lambdaFunction != null) {
                addInvocationToGlobalAnnotMap(typeDef.name.value, lambdaFunction, initFunction.body, pkgID, owner);
            }
        }
    }

    private void defineServiceAnnotations(BLangPackage pkgNode, SymbolEnv env, BLangFunction initFunction) {
        for (BLangService service : pkgNode.services) {
            PackageID pkgID = service.symbol.pkgID;
            BSymbol owner = service.symbol.owner;

            BLangLambdaFunction lambdaFunction = defineAnnotations(service, service.pos, pkgNode, env, pkgID, owner);
            if (lambdaFunction != null) {
                BLangBlockStmt target = (BLangBlockStmt) TreeBuilder.createBlockNode();
                target.pos = initFunction.body.pos;

                addLambdaToGlobalAnnotMap(service.serviceTypeDefinition.name.value, lambdaFunction, target);

                int index = calculateIndex(initFunction.body.stmts, service);
                for (BLangStatement stmt : target.stmts) {
                    initFunction.body.stmts.add(index++, stmt);
                }
            }
        }
    }

    private void defineFunctionAnnotations(BLangPackage pkgNode, SymbolEnv env, BLangFunction initFunction) {
        BLangFunction[] functions = pkgNode.functions.toArray(new BLangFunction[pkgNode.functions.size()]);
        for (BLangFunction function : functions) {
            PackageID pkgID = function.symbol.pkgID;
            BSymbol owner = function.symbol.owner;

            if (!(function.attachedFunction || function.attachedOuterFunction)) {
                // Temporarily avoid sending module level function annotations to the runtime
                continue;
            }

            BLangLambdaFunction lambdaFunction = defineAnnotations(function, pkgNode, env, pkgID, owner);
            if (lambdaFunction != null) {
                BLangBlockStmt target = (BLangBlockStmt) TreeBuilder.createBlockNode();
                target.pos = initFunction.body.pos;
//                String identifier = (function.attachedFunction || function.attachedOuterFunction) ?
//                        function.symbol.name.value : function.name.value;
                String identifier = function.symbol.name.value;

                if (function.receiver.type instanceof BServiceType) {
                    addLambdaToGlobalAnnotMap(identifier, lambdaFunction, target);
                } else {
                    addInvocationToGlobalAnnotMap(identifier, lambdaFunction, target, pkgID, owner);
                }

                int index = calculateIndex(initFunction.body.stmts, function.receiver.type.tsymbol);
                for (BLangStatement stmt : target.stmts) {
                    initFunction.body.stmts.add(index++, stmt);
                }
            }
        }
    }

    private BLangLambdaFunction defineAnnotations(AnnotatableNode node, DiagnosticPos pos, BLangPackage pkgNode,
                                                  SymbolEnv env, PackageID pkgID, BSymbol owner) {
        return defineAnnotations(node.getAnnotationAttachments().stream()
                                         .map(annotAttachment -> (BLangAnnotationAttachment) annotAttachment)
                                         .collect(Collectors.toList()), pos, pkgNode, env, pkgID, owner);
    }

    private BLangLambdaFunction defineAnnotations(List<BLangAnnotationAttachment> annAttachments, DiagnosticPos pos,
                                                  BLangPackage pkgNode, SymbolEnv env, PackageID pkgID, BSymbol owner) {
        if (annAttachments.isEmpty()) {
            return null;
        }

        BLangFunction function = defineFunction(pkgNode, pos, pkgID, owner);
        BLangSimpleVariable localAnnotMap = defineLocalAnnotMap(function, pkgID, owner);
        addAnnotsToFunctionBody(annAttachments, function, localAnnotMap, pkgNode);
        return addReturnAndDefineLambda(function, localAnnotMap, pkgNode, env, pkgID, owner);
    }

    private BLangLambdaFunction defineAnnotations(BLangFunction bLangFunction, BLangPackage pkgNode, SymbolEnv env,
                                                  PackageID pkgID, BSymbol owner) {
        BLangFunction function = null;
        BLangSimpleVariable localAnnotMap = null;
        BLangLambdaFunction lambdaFunction = null;

        boolean annotFunctionDefined = false;

        if (!bLangFunction.annAttachments.isEmpty()) {
            function = defineFunction(pkgNode, bLangFunction.pos, pkgID, owner);
            localAnnotMap = defineLocalAnnotMap(function, pkgID, owner);
            addAnnotsToFunctionBody(bLangFunction.annAttachments, function, localAnnotMap, pkgNode);
            annotFunctionDefined = true;
        }

        for (BLangSimpleVariable param : bLangFunction.getParameters()) {
            BLangLambdaFunction paramAnnotLambda = defineAnnotations(param.annAttachments, param.pos, pkgNode, env,
                                                                     pkgID, owner);
            if (paramAnnotLambda != null) {
                if (!annotFunctionDefined) {
                    function = defineFunction(pkgNode, bLangFunction.pos, pkgID, owner);
                    localAnnotMap = defineLocalAnnotMap(function, pkgID, owner);
                    addAnnotsToFunctionBody(bLangFunction.annAttachments, function, localAnnotMap, pkgNode);
                    annotFunctionDefined = true;
                }
                addInvocationToMap(localAnnotMap, PARAM + DOT + param.name.value, paramAnnotLambda,
                                   function.body, pkgID, owner);
            }
        }

        if (!bLangFunction.returnTypeAnnAttachments.isEmpty()) {
            if (!annotFunctionDefined) {
                function = defineFunction(pkgNode, bLangFunction.pos, pkgID, owner);
                localAnnotMap = defineLocalAnnotMap(function, pkgID, owner);
                annotFunctionDefined = true;
            }

            BLangFunction retFunction = defineFunction(pkgNode, bLangFunction.pos, pkgID, owner);
            BLangSimpleVariable retLocalAnnotMap = defineLocalAnnotMap(retFunction, pkgID, owner);
            addAnnotsToFunctionBody(bLangFunction.returnTypeAnnAttachments, retFunction, retLocalAnnotMap, pkgNode);
            BLangLambdaFunction returnAnnotLambda = addReturnAndDefineLambda(retFunction, retLocalAnnotMap, pkgNode,
                                                                             env, pkgID, owner);
            addInvocationToMap(localAnnotMap, RETURNS, returnAnnotLambda, function.body, pkgID, owner);
        }

        if (annotFunctionDefined) {
            lambdaFunction = addReturnAndDefineLambda(function, localAnnotMap, pkgNode, env, pkgID, owner);
        }

        return lambdaFunction;
    }

    private BLangFunction defineFunction(BLangPackage pkgNode, DiagnosticPos pos, PackageID pkgID, BSymbol owner) {
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

        function.body = ASTBuilderUtil.createBlockStmt(pos, new ArrayList<>());

        BInvokableSymbol functionSymbol = new BInvokableSymbol(SymTag.INVOKABLE, Flags.asMask(function.flagSet),
                                                               new Name(funcName), pkgID, function.type, owner);
        functionSymbol.bodyExist = true;
        functionSymbol.kind = SymbolKind.FUNCTION;

        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.scope = new Scope(functionSymbol);
        function.symbol = functionSymbol;
        pkgNode.functions.add(function);
        pkgNode.topLevelNodes.add(function);
        return function;
    }

    private BLangLambdaFunction addReturnAndDefineLambda(BLangFunction function, BLangSimpleVariable localAnnotMap,
                                                         BLangPackage pkgNode, SymbolEnv env, PackageID pkgID,
                                                         BSymbol owner) {
        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(function.pos, function.body);
        returnStmt.expr = ASTBuilderUtil.createVariableRef(function.pos, localAnnotMap.symbol);

        BInvokableSymbol lambdaFunctionSymbol = createInvokableSymbol(function, pkgID, owner);
        BLangLambdaFunction lambdaFunction = desugar.createLambdaFunction(function, lambdaFunctionSymbol);
        lambdaFunction.cachedEnv = env.createClone();

        pkgNode.lambdaFunctions.add(lambdaFunction);
        return lambdaFunction;
    }

    private BLangSimpleVariable defineLocalAnnotMap(BLangFunction function, PackageID pkgID, BSymbol owner) {
        BLangSimpleVariable localAnnotMap = ASTBuilderUtil.createVariable(function.pos, LOCAL_ANNOT_MAP,
                                                                          symTable.mapType,
                                                                          ASTBuilderUtil.createEmptyRecordLiteral(
                                                                                  function.pos, symTable.mapType),
                                                                          new BVarSymbol(0,
                                                                                         names.fromString(
                                                                                                 LOCAL_ANNOT_MAP),
                                                                                         pkgID, symTable.mapType,
                                                                                         owner));
        ASTBuilderUtil.defineVariable(localAnnotMap, function.symbol, names);
        BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(function.pos, function.body);
        variableDef.var = localAnnotMap;
        return localAnnotMap;
    }

    private void addAnnotsToFunctionBody(List<BLangAnnotationAttachment> nodeAttachments, BLangFunction function,
                                         BLangSimpleVariable localAnnotMap, BLangPackage pkgNode) {
        Map<BAnnotationSymbol, List<BLangAnnotationAttachment>> attachments = new HashMap<>();

        for (AnnotationAttachmentNode attachment : nodeAttachments) {
            BLangAnnotationAttachment annotationAttachment = (BLangAnnotationAttachment) attachment;
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

                if (attachPoint.source) {
                    // Avoid defining annotation values for source only annotations.
                    continue;
                }

                attachments.put(annotationSymbol,
                                new ArrayList<BLangAnnotationAttachment>() {{
                                    add(annotationAttachment);
                                }});
            }
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
                // $local_annot_map$["v1"] = true;
                addTrueAnnot(attachments.get(annotationSymbol).get(0), localAnnotMap, function.body);
            } else if (attachedTypeSymbol.type.tag != TypeTags.ARRAY) {
                // annotation FooRecord v1 on type; OR annotation map<anydata> v1 on type;
                // @v1 {
                //     value: 1
                // }
                // type X record {
                //     int i;
                // };
                // FooRecord r = { value: 1 };
                // $local_annot_map$["v1"] = r;
                addSingleAnnot(attachments.get(annotationSymbol).get(0), localAnnotMap, function.body, function.symbol,
                               pkgNode);
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
                // FooRecord r1 = { value: 1 };
                // FooRecord r2 = { value: 2 };
                // $local_annot_map$["v1"] = [r1, r2];
                addAnnotArray(function.pos, annotationSymbol.bvmAlias(), attachedTypeSymbol.type,
                              attachments.get(annotationSymbol), localAnnotMap, function.body, function.symbol,
                              pkgNode);
            }
        }
    }

    private BInvokableSymbol createInvokableSymbol(BLangFunction function, PackageID pkgID, BSymbol owner) {
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                                                                       new Name(function.name.value),
                                                                       pkgID, function.type, owner, true);
        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = new Scope(functionSymbol);
        functionSymbol.type = new BInvokableType(Collections.emptyList(),
                                                 new BMapType(TypeTags.MAP, symTable.anyType, null), null);
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

    private void addTrueAnnot(BLangAnnotationAttachment attachment, BLangSimpleVariable localMapVar,
                              BLangBlockStmt target) {
        // Handle scenarios where type is a subtype of `true` explicitly or implicitly (by omission).
        // create: $local_annot_map$["v1"] = true;
        BLangExpression expression = ASTBuilderUtil.wrapToConversionExpr(symTable.trueType,
                                                                         ASTBuilderUtil.createLiteral(
                                                                                 target.pos, symTable.booleanType,
                                                                                 Boolean.TRUE),
                                                                         symTable, types);
        addAnnotValueAssignmentToMap(localMapVar, attachment.annotationSymbol.bvmAlias(), target, expression);
    }

    private void addSingleAnnot(BLangAnnotationAttachment attachment, BLangSimpleVariable localMapVar,
                                BLangBlockStmt target, BSymbol parentSymbol, BLangPackage pkgNode) {
        // Handle scenarios where type is a subtype of `map<any|error>` or `record{any|error...;}`.
        // FooRecord r1 = { value: 1 };
        BLangExpression expression = defineMappingAndGetRef(attachment.annotationSymbol.attachedType.type,
                                                            Symbols.isFlagOn(attachment.annotationSymbol.flags,
                                                                             Flags.CONSTANT),
                                                            attachment.pos, attachment.annotationName.value,
                                                            attachment.expr, target, parentSymbol, pkgNode);
        // create: $local_annot_map$["v1"] = r1;
        addAnnotValueAssignmentToMap(localMapVar, attachment.annotationSymbol.bvmAlias(), target, expression);
    }

    private void addAnnotArray(DiagnosticPos pos, String name, BType annotType,
                               List<BLangAnnotationAttachment> attachments, BLangSimpleVariable localMapVar,
                               BLangBlockStmt target, BSymbol parentSymbol, BLangPackage pkgNode) {
        // Handle scenarios where type is a subtype of `map<any|error>[]` or `record{any|error...;}[]`.
        BLangSimpleVariable annotationArrayVar = ASTBuilderUtil.createVariable(pos, name, annotType);
        annotationArrayVar.expr = ASTBuilderUtil.createEmptyArrayLiteral(pos, (BArrayType) annotType);
        ASTBuilderUtil.defineVariable(annotationArrayVar, parentSymbol, names);
        ASTBuilderUtil.createVariableDefStmt(pos, target).var = annotationArrayVar;
        BLangExpression array = ASTBuilderUtil.createVariableRef(target.pos, annotationArrayVar.symbol);

        addAnnotValuesToArray(annotationArrayVar, name, ((BArrayType) annotType).eType, attachments, target,
                              parentSymbol, pkgNode);

        // create: $local_annot_map$["v1"] = r1;
        addAnnotValueAssignmentToMap(localMapVar, name, target, array);
    }

    private void addAnnotValuesToArray(BLangSimpleVariable annotationArrayVar, String name, BType annotType,
                                       List<BLangAnnotationAttachment> attachments,
                                       BLangBlockStmt target, BSymbol parentSymbol, BLangPackage pkgNode) {
        long annotCount = 0;
        boolean isConst = Symbols.isFlagOn(attachments.get(0).annotationSymbol.flags, Flags.CONSTANT);
        for (BLangAnnotationAttachment attachment : attachments) {
            // FooRecord r$0 = { value: 1 };
            BLangExpression expression = defineMappingAndGetRef(annotType, isConst,
                                                                attachment.pos, name + "$" + annotCount,
                                                                attachment.expr, target, parentSymbol, pkgNode);

            BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
            assignmentStmt.expr = expression;

            // fooArray[intCount] = r$0;
            BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
            indexAccessNode.pos = target.pos;
            indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.intType, annotCount);
            indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationArrayVar.symbol);
            indexAccessNode.type = annotType;
            assignmentStmt.varRef = indexAccessNode;
            annotCount++;
        }
    }

    private BLangExpression defineMappingAndGetRef(BType annotType, boolean isConst, DiagnosticPos pos, String name,
                                                   BLangExpression expr, BLangBlockStmt target, BSymbol parentSymbol,
                                                   BLangPackage pkgNode) {
        return isConst ? defineConstAndGetRef(annotType, pos, name, expr, target, pkgNode) :
                defineVariableAndGetRef(annotType, pos, name, expr, target, parentSymbol);
    }

    private BLangExpression defineVariableAndGetRef(BType annotType, DiagnosticPos pos, String name,
                                                   BLangExpression expr, BLangBlockStmt target, BSymbol parentSymbol) {
        // create: AttachedType annotationVar = { annotation-expression }
        BLangSimpleVariable annotationVar = ASTBuilderUtil.createVariable(pos, name, annotType);
        annotationVar.expr = expr;
        ASTBuilderUtil.defineVariable(annotationVar, parentSymbol, names);
        ASTBuilderUtil.createVariableDefStmt(pos, target).var = annotationVar;
        return ASTBuilderUtil.createVariableRef(target.pos, annotationVar.symbol);
    }

    private BLangExpression defineConstAndGetRef(BType annotType, DiagnosticPos pos, String name,
                                                 BLangExpression expr, BLangBlockStmt target,
                                                 BLangPackage pkgNode) {
        // create: const AttachedType annotConst = { annotation-expression }
        BLangConstant annotConst = ASTBuilderUtil.createConstant(pos, name, annotType, expr);
        ASTBuilderUtil.defineConstant(annotConst, name, pkgNode.symbol, names, symTable);
        annotConst.symbol.type = annotConst.type;
        annotConst.symbol.literalValue = annotConst.value;
        annotConst.symbol.literalValueType = annotConst.type;
        annotConst.symbol.literalValueTypeTag = annotConst.symbol.literalValueType.tag;

        pkgNode.topLevelNodes.add(annotConst);
        pkgNode.constants.add(annotConst);
        return ASTBuilderUtil.createVariableRef(target.pos, annotConst.symbol);
    }

    private void addInvocationToGlobalAnnotMap(String identifier, BLangLambdaFunction lambdaFunction,
                                               BLangBlockStmt target, PackageID packageID, BSymbol owner) {
        addInvocationToMap(annotationMap, identifier, lambdaFunction, target, packageID, owner);
    }

    private void addLambdaToGlobalAnnotMap(String identifier, BLangLambdaFunction lambdaFunction,
                                           BLangBlockStmt target) {
        addLambdaToMap(annotationMap, identifier, lambdaFunction, target);
    }

    private void addInvocationToMap(BLangSimpleVariable map, String identifier, BLangLambdaFunction lambdaFunction,
                                    BLangBlockStmt target, PackageID packageID, BSymbol owner) {
        // create: $annotation_data["identifier"] = $annot_func$.call();
        BLangInvocation annotFuncInvocation = getInvocation(lambdaFunction, packageID, owner);
        addAnnotValueAssignmentToMap(map, identifier, target, annotFuncInvocation);
    }

    private void addLambdaToMap(BLangSimpleVariable map, String identifier, BLangLambdaFunction lambdaFunction,
                                BLangBlockStmt target) {
        // create: $annotation_data["identifier"] = $annot_func$;
        addAnnotValueAssignmentToMap(map, identifier, target,
                                     ASTBuilderUtil.createVariableRef(lambdaFunction.pos,
                                                                      lambdaFunction.function.symbol));
    }

    private void addAnnotValueAssignmentToMap(BLangSimpleVariable mapVar, String identifier, BLangBlockStmt target,
                                              BLangExpression expression) {
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
        assignmentStmt.expr = expression;

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = target.pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.stringType, identifier);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, mapVar.symbol);
        indexAccessNode.type = ((BMapType) mapVar.type).constraint;
        assignmentStmt.varRef = indexAccessNode;
    }

    private BLangInvocation getInvocation(BLangLambdaFunction lambdaFunction, PackageID packageID, BSymbol owner) {
        BLangInvocation funcInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        funcInvocation.builtinMethodInvocation = true;
        funcInvocation.builtInMethod = BLangBuiltInMethod.CALL;
        funcInvocation.type = symTable.mapType;
        funcInvocation.expr = ASTBuilderUtil.createVariableRef(lambdaFunction.pos, lambdaFunction.function.symbol);
        BSymbol varSymbol = new BInvokableSymbol(SymTag.VARIABLE, 0, lambdaFunction.function.symbol.name,
                                                 packageID, lambdaFunction.function.type, owner);
        varSymbol.kind = SymbolKind.FUNCTION;
        funcInvocation.symbol = varSymbol;
        funcInvocation.name = ASTBuilderUtil.createIdentifier(lambdaFunction.pos, BLangBuiltInMethod.CALL.getName());
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
}
