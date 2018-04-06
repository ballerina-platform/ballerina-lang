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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Desugar annotations into executable entries.
 *
 * @since 0.965.0
 */
public class AnnotationDesugar {

    private static final String ANNOTATION_DATA = "$annotation_data";
    private static final String DOT = ".";

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

    protected void rewritePackageAnnotations(BLangPackage pkgNode) {
        BLangFunction initFunction = pkgNode.initFunction;

        // This is the variable which store all package level annotations.
        BLangVariable annotationMap = createGlobalAnnotationMapVar(pkgNode);

        // handle Service Annotations.
        for (BLangService service : pkgNode.services) {
            generateAnnotations(service, service.name.value, initFunction, annotationMap);
            for (BLangResource resource : service.resources) {
                String key = service.name.value + DOT + resource.name.value;
                generateAnnotations(resource, key, initFunction, annotationMap);
            }
        }
        // Handle Function Annotations.
        for (BLangFunction function : pkgNode.functions) {
            generateAnnotations(function, function.symbol.name.value, initFunction, annotationMap);
        }
        // Handle Connector Annotations.
        for (BLangConnector connector : pkgNode.connectors) {
            generateAnnotations(connector, connector.name.value, initFunction, annotationMap);
            for (BLangAction action : connector.actions) {
                String key = connector.name.value + DOT + action.name.value;
                generateAnnotations(connector, key, initFunction, annotationMap);
            }
        }
        // Handle Struct Annotations.
        for (BLangStruct struct : pkgNode.structs) {
            generateAnnotations(struct, struct.name.value, initFunction, annotationMap);
            for (BLangVariable field : struct.fields) {
                String key = struct.name.value + DOT + field.name.value;
                generateAnnotations(field, key, initFunction, annotationMap);
            }
        }

        for (BLangEndpoint globalEndpoint : pkgNode.globalEndpoints) {
            generateAnnotations(globalEndpoint, globalEndpoint.name.value, initFunction, annotationMap);
        }

        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(pkgNode.pos, symTable.nilType);
        pkgNode.initFunction.body.stmts.add(returnStmt);
    }

    private void generateAnnotations(AnnotatableNode node, String key, BLangFunction target, BLangVariable annMapVar) {
        if (node.getAnnotationAttachments().size() == 0) {
            return;
        }
        BLangVariable entryVar = createAnnotationMapEntryVar(key, annMapVar, target.body, target.symbol);
        int annCount = 0;
        for (AnnotationAttachmentNode attachment : node.getAnnotationAttachments()) {
            initAnnotation((BLangAnnotationAttachment) attachment, entryVar, target.body, target.symbol, annCount++);
        }
    }

    private BLangVariable createGlobalAnnotationMapVar(BLangPackage pkgNode) {
        DiagnosticPos pos = pkgNode.pos;
        BLangVariable annotationMap = ASTBuilderUtil.createVariable(pkgNode.pos, ANNOTATION_DATA, symTable.mapType);
        ASTBuilderUtil.defineVariable(annotationMap, pkgNode.symbol, names);
        pkgNode.addGlobalVariable(annotationMap);

        final BLangRecordLiteral recordLiteralNode = ASTBuilderUtil.createEmptyRecordLiteral(pos, symTable.mapType);
        final BLangAssignment annMapAssignment = ASTBuilderUtil.createAssignmentStmt(pos, pkgNode.initFunction.body);
        annMapAssignment.expr = recordLiteralNode;
        annMapAssignment.setVariable(ASTBuilderUtil.createVariableRef(pos, annotationMap.symbol));
        return annotationMap;
    }

    private BLangVariable createAnnotationMapEntryVar(String key, BLangVariable annotationMapVar,
                                                      BLangBlockStmt target, BSymbol parentSymbol) {
        // create: map key = {};
        final BLangRecordLiteral recordLiteralNode =
                ASTBuilderUtil.createEmptyRecordLiteral(target.pos, symTable.mapType);

        BLangVariable entryVariable = ASTBuilderUtil.createVariable(target.pos, key, recordLiteralNode.type);
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

    private void initAnnotation(BLangAnnotationAttachment attachment, BLangVariable annotationMapEntryVar,
                                BLangBlockStmt target, BSymbol parentSymbol, int index) {
        BLangVariable annotationVar = null;
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
}
