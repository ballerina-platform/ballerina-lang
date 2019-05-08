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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
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
import java.util.stream.Collectors;

/**
 * Desugar annotations into executable entries.
 *
 * @since 0.965.0
 */
public class AnnotationDesugar {

    private static final String ANNOTATION_DATA = "$annotation_data";
    private static final String ANNOT_FUNC = "$annot_func$";
    private static final String LOCAL_ANNOT_MAP = "$local_annot_map$";
    private static final String DOT = ".";
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

    protected void rewritePackageAnnotations(BLangPackage pkgNode, SymbolEnv env) {
        BLangFunction initFunction = pkgNode.initFunction;

        handleTypeAnnotations(pkgNode, initFunction, env);

//        // Handle service annotations
//        for (BLangService service : pkgNode.services) {
//            generateAnnotations(service, service.name.value, initFunction, annotationMap);
//        }
//
//        // Handle Function Annotations.
//        handleFunctionAnnotations(pkgNode, initFunction, annotationMap);
//
//        for (BLangVariable variable : pkgNode.globalVars) {
//            generateAnnotations(variable, variable.symbol.name.value, initFunction, annotationMap);
//        }

        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(pkgNode.pos, symTable.nilType);
        pkgNode.initFunction.body.stmts.add(returnStmt);
    }

    private void handleTypeAnnotations(BLangPackage pkgNode, BLangFunction initFunction, SymbolEnv env) {
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            BLangLambdaFunction lambdaFunction = generateAnnots(typeDef, typeDef.pos, typeDef.symbol.pkgID,
                                                                typeDef.symbol.owner, pkgNode, env);
            if (lambdaFunction != null) {
                addInvocationToGlobalAnnotMap(typeDef.name.value, lambdaFunction, initFunction, typeDef.symbol.pkgID,
                                              typeDef.symbol.owner);
            }

            if (typeDef.symbol.type.tag != TypeTags.OBJECT) {
                continue;
            }

            BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDef.typeNode;
            for (BLangFunction function : objectTypeNode.functions) {
                String key = typeDef.name.value + DOT + function.name.value;
                generateAnnotations(function, key, initFunction, annotationMap);
            }
        }
    }



    private BLangLambdaFunction generateAnnots(AnnotatableNode node, DiagnosticPos pos, PackageID pkgID, BSymbol owner,
                                               BLangPackage pkgNode, SymbolEnv env) {
        if (node.getAnnotationAttachments().size() == 0) {
            return null;
        }

        // If annotations exist, create a lambda function as follows:
        //
        // annotation v1 on type;
        // annotation Foo v2 on type;
        // annotation Foo[] v3 on type;
        //
        // int intVal = 32;
        //
        // @v1
        // @v2 {
        //     fooVal: 2
        // }
        // @v3 {
        //     fooVal: 31
        // }
        // @v3 {
        //     fooVal: intVal
        // }
        // type Bar record {
        //
        // };
        //
        // function $annot_func$ = function() returns map<any> {
        //    map<any> $local_annot_map$ = {};
        //
        //    $local_annot_map$["v1"] = true;
        //
        //    Foo r1$0 = { i: 2 };
        //    $local_annot_map$["v2"] = r1$0;
        //
        //    Foo r1$1 = { i: 31 };
        //    Foo r1$2 = { i: intVal };
        //    $local_annot_map$["v3"] = [r1$1, r1$2];
        //
        //    return $local_annot_map$;
        // }

        String funcName = ANNOT_FUNC + annotFuncCount++;
        BLangFunction function = ASTBuilderUtil.createFunction(pos, funcName);
        function.type = new BInvokableType(Collections.emptyList(), symTable.mapType, null);
        BLangBlockStmt functionBlock = createAnonymousFunctionBlock(pos, function);

        BInvokableSymbol functionSymbol = new BInvokableSymbol(SymTag.INVOKABLE, Flags.asMask(function.flagSet),
                                                               new Name(funcName), pkgID, function.type, owner);
        functionSymbol.bodyExist = true;
        functionSymbol.kind = SymbolKind.FUNCTION;

        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.scope = new Scope(functionSymbol);
        function.symbol = functionSymbol;

        BLangSimpleVariable localAnnotMap = ASTBuilderUtil.createVariable(pos, LOCAL_ANNOT_MAP,
                                                                          symTable.mapType,
                                                                          ASTBuilderUtil.createEmptyRecordLiteral(
                                                                                  pos, symTable.mapType),
                                                                          new BVarSymbol(0,
                                                                                         names.fromString(
                                                                                                 LOCAL_ANNOT_MAP),
                                                                                         pkgID, symTable.mapType,
                                                                                         owner));
        ASTBuilderUtil.defineVariable(localAnnotMap, function.symbol, names);
        BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(pos, functionBlock);
        variableDef.var = localAnnotMap;

        Map<BAnnotationSymbol, List<BLangAnnotationAttachment>> attachments = new HashMap<>();

        for (AnnotationAttachmentNode attachment : node.getAnnotationAttachments()) {
            BLangAnnotationAttachment annotationAttachment = (BLangAnnotationAttachment) attachment;
            if (attachments.containsKey(annotationAttachment.annotationSymbol)) {
                attachments.get(annotationAttachment.annotationSymbol).add(annotationAttachment);
            } else {
                attachments.put(annotationAttachment.annotationSymbol,
                                new ArrayList<BLangAnnotationAttachment>() {{ add(annotationAttachment); }});
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
                addTrueAnnot(attachments.get(annotationSymbol).get(0), localAnnotMap, functionBlock);
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
                addSingleAnnot(attachments.get(annotationSymbol).get(0), localAnnotMap, functionBlock, function.symbol);
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
                addAnnotArray(pos, annotationSymbol.bvmAlias(), attachedTypeSymbol.type,
                              attachments.get(annotationSymbol), localAnnotMap, functionBlock, function.symbol);
            }
        }

        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(pos, functionBlock);
        returnStmt.expr = ASTBuilderUtil.createVariableRef(pos, localAnnotMap.symbol);

        BInvokableSymbol lambdaFunctionSymbol = createInvokableSymbol(function, pkgID, owner);
        BLangLambdaFunction lambdaFunction = desugar.createLambdaFunction(function, lambdaFunctionSymbol);
        lambdaFunction.cachedEnv = env.createClone();

        pkgNode.lambdaFunctions.add(lambdaFunction);
        pkgNode.functions.add(function);
        pkgNode.topLevelNodes.add(function);
        return lambdaFunction;
    }

    private BLangBlockStmt createAnonymousFunctionBlock(DiagnosticPos pos, BLangFunction function) {
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

        BLangBlockStmt functionBlock = ASTBuilderUtil.createBlockStmt(pos, new ArrayList<>());
        function.body = functionBlock;
        return functionBlock;
    }

    private BInvokableSymbol createInvokableSymbol(BLangFunction function, PackageID pkgID, BSymbol owner) {
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                                                                       new Name(function.name.value),
                                                                       pkgID, function.type, owner,  true);
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

    private void handleFunctionAnnotations(BLangPackage pkgNode, BLangFunction initFunction,
                                           BLangSimpleVariable annotationMap) {
        for (BLangFunction function : pkgNode.functions) {
            generateAnnotations(function, function.symbol.name.value, initFunction, annotationMap);
        }
    }

    private void generateAnnotations(AnnotatableNode node, String key, BLangFunction target,
                                     BLangSimpleVariable annMapVar) {
        if (node.getAnnotationAttachments().size() == 0) {
            return;
        }
        BLangSimpleVariable entryVar = createAnnotationMapEntryVar(key, annMapVar, target.body, target.symbol);
        int annCount = 0;
        for (AnnotationAttachmentNode attachment : node.getAnnotationAttachments()) {
            initAnnotation((BLangAnnotationAttachment) attachment, entryVar, target.body, target.symbol, annCount++);
        }
    }

    private BLangSimpleVariable createGlobalAnnotationMapVar(BLangPackage pkgNode) {
        BLangSimpleVariable annotationMap = ASTBuilderUtil.createVariable(pkgNode.pos, ANNOTATION_DATA,
                symTable.mapType, ASTBuilderUtil.createEmptyRecordLiteral(pkgNode.pos, symTable.mapType), null);
        ASTBuilderUtil.defineVariable(annotationMap, pkgNode.symbol, names);
        pkgNode.addGlobalVariable(annotationMap);
        return annotationMap;
    }

    private BLangSimpleVariable createAnnotationMapEntryVar(String key, BLangSimpleVariable annotationMapVar,
                                                            BLangBlockStmt target, BSymbol parentSymbol) {
        // create: map key = {};
        final BLangRecordLiteral recordLiteralNode =
                ASTBuilderUtil.createEmptyRecordLiteral(target.pos, symTable.mapType);

        BLangSimpleVariable entryVariable = ASTBuilderUtil.createVariable(target.pos, key, recordLiteralNode.type);
        entryVariable.expr = recordLiteralNode;
        ASTBuilderUtil.defineVariable(entryVariable, parentSymbol, names);
        ASTBuilderUtil.createVariableDefStmt(target.pos, target).var = entryVariable;

        // create: annotationMapVar["key"] = key;
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
        assignmentStmt.expr = ASTBuilderUtil.createVariableRef(target.pos, entryVariable.symbol);

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = target.pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.stringType, key);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationMapVar.symbol);
        indexAccessNode.type = recordLiteralNode.type;
        assignmentStmt.varRef = indexAccessNode;
        return entryVariable;
    }

    private void initAnnotation(BLangAnnotationAttachment attachment, BLangSimpleVariable annotationMapEntryVar,
                                BLangBlockStmt target, BSymbol parentSymbol, int index) {
        BLangSimpleVariable annotationVar = null;
        if (attachment.annotationSymbol.attachedType != null &&
                !types.isAssignable(attachment.annotationSymbol.attachedType.type, symTable.trueType)) {
            // create: AttachedType annotationVar = { annotation-expression }
            BType annotType = attachment.annotationSymbol.attachedType.type;
            annotationVar = ASTBuilderUtil.createVariable(attachment.pos,
                                                          attachment.annotationName.value,
                                                          annotType.tag == TypeTags.ARRAY ?
                                                                  ((BArrayType) annotType).eType : annotType);
            annotationVar.expr = attachment.expr;
            ASTBuilderUtil.defineVariable(annotationVar, parentSymbol, names);
            ASTBuilderUtil.createVariableDefStmt(attachment.pos, target).var = annotationVar;
        }


        BLangExpression annotationVarExpr;
        if (annotationVar != null) {
            annotationVarExpr = ASTBuilderUtil.createVariableRef(target.pos, annotationVar.symbol);
        } else {
            // Handle scenarios where type is a subtype of `true` explicitly or implicitly (by omission).
            annotationVarExpr = ASTBuilderUtil.wrapToConversionExpr(symTable.trueType,
                                                                    ASTBuilderUtil.createLiteral(target.pos,
                                                                                                 symTable.booleanType,
                                                                                                 Boolean.TRUE),
                                                                    symTable, types);
        }

        // create: annotationMapEntryVar["name$index"] = annotationVar;
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
//        assignmentStmt.expr = desugar.visitUtilMethodInvocation(annotationVarExpr.pos, BLangBuiltInMethod.FREEZE,
//                                                                Lists.of(annotationVarExpr));
        assignmentStmt.expr = annotationVarExpr;

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = target.pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.stringType,
                attachment.annotationSymbol.bvmAlias() + "$" + index);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationMapEntryVar.symbol);
        indexAccessNode.type = annotationMapEntryVar.symbol.type;
        assignmentStmt.varRef = indexAccessNode;
    }

    private void addTrueAnnot(BLangAnnotationAttachment attachment, BLangSimpleVariable localMapVar,
                              BLangBlockStmt target) {
        // Handle scenarios where type is a subtype of `true` explicitly or implicitly (by omission).
        // create: $local_annot_map$["v1"] = true;
        BLangExpression expression = ASTBuilderUtil.wrapToConversionExpr(symTable.trueType,
                                                                         ASTBuilderUtil.createLiteral(target.pos,
                                                                              symTable.booleanType, Boolean.TRUE),
                                                                         symTable, types);
        addAnnotValueAssignmentToLocalMap(attachment.annotationSymbol.bvmAlias(), localMapVar, target, expression);
    }

    private void addSingleAnnot(BLangAnnotationAttachment attachment, BLangSimpleVariable localMapVar,
                                BLangBlockStmt target, BSymbol parentSymbol) {
        // Handle scenarios where type is a subtype of `map<any|error>` or `record{any|error...;}`.
        // FooRecord r1 = { value: 1 };
        BLangExpression expression = defineMappingAndGetRef(attachment.annotationSymbol.attachedType.type,
                                                            attachment.pos, attachment.annotationName.value,
                                                            attachment.expr, target, parentSymbol);
        // create: $local_annot_map$["v1"] = r1;
        addAnnotValueAssignmentToLocalMap(attachment.annotationSymbol.bvmAlias(), localMapVar, target, expression);
    }

    private void addAnnotArray(DiagnosticPos pos, String name, BType annotType,
                               List<BLangAnnotationAttachment> attachments, BLangSimpleVariable localMapVar,
                               BLangBlockStmt target, BSymbol parentSymbol) {
        // Handle scenarios where type is a subtype of `map<any|error>[]` or `record{any|error...;}[]`.
        BLangSimpleVariable annotationArrayVar = ASTBuilderUtil.createVariable(pos, name, annotType);
        annotationArrayVar.expr = ASTBuilderUtil.createEmptyArrayLiteral(pos, (BArrayType) annotType);
        ASTBuilderUtil.defineVariable(annotationArrayVar, parentSymbol, names);
        ASTBuilderUtil.createVariableDefStmt(pos, target).var = annotationArrayVar;
        BLangExpression array = ASTBuilderUtil.createVariableRef(target.pos, annotationArrayVar.symbol);

        addAnnotValuesToArray(annotationArrayVar, name, ((BArrayType) annotType).eType, attachments, target,
                              parentSymbol);

        // create: $local_annot_map$["v1"] = r1;
        addAnnotValueAssignmentToLocalMap(name, localMapVar, target, array);
    }

    private void addAnnotValuesToArray(BLangSimpleVariable annotationArrayVar, String name, BType annotType,
                                       List<BLangAnnotationAttachment> attachments,
                                       BLangBlockStmt target, BSymbol parentSymbol) {
        long annotCount = 0;
        for (BLangAnnotationAttachment attachment : attachments) {
            // FooRecord r$0 = { value: 1 };
            BLangExpression expression = defineMappingAndGetRef(annotType, attachment.pos, name + "$" + annotCount,
                                                                attachment.expr, target, parentSymbol);

            BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
            assignmentStmt.expr = expression;

            // fooArray[intCount] = r$0;
            BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
            indexAccessNode.pos = target.pos;
            indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.intType, annotCount);
            indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationArrayVar.symbol);
            indexAccessNode.type = annotationArrayVar.symbol.type;
            assignmentStmt.varRef = indexAccessNode;
            annotCount++;
        }
    }

    private void addAnnotValueAssignmentToLocalMap(String name,
                                                   BLangSimpleVariable localMapVar,
                                                   BLangBlockStmt target,
                                                   BLangExpression expression) {
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
        assignmentStmt.expr = expression;

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = target.pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.stringType, name);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, localMapVar.symbol);
        indexAccessNode.type = localMapVar.symbol.type;
        assignmentStmt.varRef = indexAccessNode;
    }

    private BLangExpression defineMappingAndGetRef(BType annotType, DiagnosticPos pos, String name,
                                                   BLangExpression expr, BLangBlockStmt target, BSymbol parentSymbol) {
        // create: AttachedType annotationVar = { annotation-expression }
        BLangSimpleVariable annotationVar = ASTBuilderUtil.createVariable(pos, name, annotType);
        annotationVar.expr = expr;
        ASTBuilderUtil.defineVariable(annotationVar, parentSymbol, names);
        ASTBuilderUtil.createVariableDefStmt(pos, target).var = annotationVar;
        return ASTBuilderUtil.createVariableRef(target.pos, annotationVar.symbol);
    }

    private void addInvocationToGlobalAnnotMap(String identifier, BLangLambdaFunction lambdaFunction,
                                               BLangFunction initFunction, PackageID packageID, BSymbol owner) {
        BLangInvocation annotFuncInvocation = getInvocation(lambdaFunction, packageID, owner);

        // create: $annotation_data["identifier"] = $annot_func$.call();
        BLangBlockStmt target = initFunction.body;
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(initFunction.pos, target);
        assignmentStmt.expr = annotFuncInvocation;

        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = target.pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.stringType, identifier);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationMap.symbol);
        indexAccessNode.type = annotFuncInvocation.type;
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
}
