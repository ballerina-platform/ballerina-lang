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
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Desugar annotations into executable entries.
 *
 * @since 0.965.0
 */
public class AnnotationDesugar {

    private static final String ANNOTATION_DATA = "$annotation_data";
    private static final String DOT = ".";
    private BLangSimpleVariable annotationMap;

    private static final CompilerContext.Key<AnnotationDesugar> ANNOTATION_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
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
        this.symTable = SymbolTable.getInstance(context);
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

    protected void rewritePackageAnnotations(BLangPackage pkgNode) {
        BLangFunction initFunction = pkgNode.initFunction;

        BLangBlockStmt blockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockStmt.pos = initFunction.pos;

        for (BLangVariable variable : pkgNode.globalVars) {
            generateAnnotations(variable, variable.symbol.name.value, initFunction, initFunction.body, annotationMap);
        }

        int index = calculateIndex(initFunction.body.stmts);

        for (BLangStatement stmt : blockStmt.stmts) {
            stmt.pos = pkgNode.pos;
            initFunction.body.stmts.add(index++, stmt);
        }

        // Handle service annotations
        for (BLangService service : pkgNode.services) {
            generateAnnotations(pkgNode, service, service.name.value, initFunction, annotationMap);
        }

        // Handle Function Annotations.
        handleFunctionAnnotations(pkgNode, initFunction, annotationMap);

        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(pkgNode.pos, symTable.nilType);
        pkgNode.initFunction.body.stmts.add(returnStmt);
    }

    private int calculateIndex(List<BLangStatement> stmts) {
        for (int i = 0; i < stmts.size(); i++) {
            BLangStatement stmt = stmts.get(i);
            if ((stmt.getKind() == NodeKind.ASSIGNMENT) &&
                    (((BLangAssignment) stmt).expr.getKind() == NodeKind.SERVICE_CONSTRUCTOR)) {
                return i;
            }
        }
        return stmts.size();
    }

    private void handleFunctionAnnotations(BLangPackage pkgNode, BLangFunction initFunction,
                                           BLangSimpleVariable annotationMap) {
        for (BLangFunction function : pkgNode.functions) {
            if (function.attachedFunction && function.receiver.type instanceof BServiceType) {
                BLangBlockStmt blockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
                generateAnnotations(function, function.symbol.name.value, initFunction, blockStmt, annotationMap);
                int index = calculateIndex(initFunction.body.stmts, function.receiver.type.tsymbol);

                for (BLangStatement stmt : blockStmt.stmts) {
                    stmt.pos = pkgNode.pos;
                    initFunction.body.stmts.add(index++, stmt);
                }
            } else {
                generateAnnotations(function, function.symbol.name.value, initFunction, initFunction.body,
                                    annotationMap);
            }
        }

        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            generateAnnotations(typeDef, typeDef.name.value, initFunction, initFunction.body, annotationMap);
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            if (typeDef.symbol.type.tag == TypeTags.OBJECT) {
                BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDef.typeNode;
                for (BLangSimpleVariable field : objectTypeNode.fields) {
                    String key = typeDef.name.value + DOT + field.name.value;
                    generateAnnotations(field, key, initFunction, initFunction.body, annotationMap);
                }
            } else if (typeDef.symbol.type.tag == TypeTags.RECORD) {
                BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
                for (BLangSimpleVariable field : recordTypeNode.fields) {
                    String key = typeDef.name.value + DOT + field.name.value;
                    generateAnnotations(field, key, initFunction, initFunction.body, annotationMap);
                }
            }
        }
    }

    private void generateAnnotations(BLangPackage pkgNode, BLangService service, String key, BLangFunction initFunction,
                                     BLangSimpleVariable  annMapVar) {
        if (service.getAnnotationAttachments().size() == 0) {
            return;
        }

        BLangBlockStmt blockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();

        BLangSimpleVariable entryVar = createAnnotationMapEntryVar(key, annMapVar, blockStmt, initFunction.symbol);
        int annCount = 0;
        for (AnnotationAttachmentNode attachment : service.getAnnotationAttachments()) {
            initAnnotation((BLangAnnotationAttachment) attachment, entryVar, blockStmt, initFunction.symbol,
                           annCount++);
        }

        int index = calculateIndex(initFunction.body.stmts, service);

        for (BLangStatement stmt : blockStmt.stmts) {
            stmt.pos = pkgNode.pos;
            initFunction.body.stmts.add(index++, stmt);
        }
    }

    private void generateAnnotations(AnnotatableNode node, String key, BLangFunction target,
                                     BLangBlockStmt bLangBlockStmt,
                                     BLangSimpleVariable annMapVar) {
        if (node.getAnnotationAttachments().size() == 0) {
            return;
        }
        BLangSimpleVariable entryVar = createAnnotationMapEntryVar(key, annMapVar, bLangBlockStmt, target.symbol);
        int annCount = 0;
        for (AnnotationAttachmentNode attachment : node.getAnnotationAttachments()) {
            initAnnotation((BLangAnnotationAttachment) attachment, entryVar, bLangBlockStmt, target.symbol, annCount++);
        }
    }

    private BLangSimpleVariable createGlobalAnnotationMapVar(BLangPackage pkgNode) {
        BLangSimpleVariable annotationMap = ASTBuilderUtil.createVariable(pkgNode.pos, ANNOTATION_DATA,
                symTable.mapType, ASTBuilderUtil.createEmptyRecordLiteral(pkgNode.pos, symTable.mapType), null);
        ASTBuilderUtil.defineVariable(annotationMap, pkgNode.symbol, names);
        pkgNode.globalVars.add(0, annotationMap); // TODO fix this
        pkgNode.topLevelNodes.add(0, annotationMap);
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
        if (attachment.annotationSymbol.attachedType != null) {
            // create: AttachedType annotationVar = { annotation-expression }
            annotationVar = ASTBuilderUtil.createVariable(attachment.pos,
                    attachment.annotationName.value, attachment.annotationSymbol.attachedType.type);
            annotationVar.expr = attachment.expr;
            ASTBuilderUtil.defineVariable(annotationVar, parentSymbol, names);
            ASTBuilderUtil.createVariableDefStmt(attachment.pos, target).var = annotationVar;
        }

        // create: annotationMapEntryVar["name$index"] = annotationVar;
        BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(target.pos, target);
        if (annotationVar != null) {
            assignmentStmt.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationVar.symbol);
        } else {
            assignmentStmt.expr = ASTBuilderUtil.createLiteral(target.pos, symTable.nilType, null);
        }
        BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = target.pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createLiteral(target.pos, symTable.stringType,
                attachment.annotationSymbol.bvmAlias() + "$" + index);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(target.pos, annotationMapEntryVar.symbol);
        indexAccessNode.type = annotationMapEntryVar.symbol.type;
        assignmentStmt.varRef = indexAccessNode;
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
