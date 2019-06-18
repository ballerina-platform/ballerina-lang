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
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * Desugar annotations into executable entries.
 *
 * @since 0.965.0
 */
public class AnnotationDesugar {

    private static final String ANNOTATION_DATA = "$annotation_data";
    public static final String BUILTIN_PKG_KEY = "ballerina" + "/" + "builtin";
    public static final String DEFAULTABLE_ARGS = "defaultableArgs";
    public static final String DEFAULTABLE_FIELD = "argsData";
    public static final String ARG_NAMES = "args";
    private static final String DOT = ".";
    private BLangSimpleVariable annotationMap;
    private SymbolResolver symResolver;

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

    protected void rewritePackageAnnotations(BLangPackage pkgNode) {
        BLangFunction initFunction = pkgNode.initFunction;

        BLangBlockStmt blockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();

        // Handle service annotations
        for (BLangService service : pkgNode.services) {
            generateAnnotations(service, service.name.value, initFunction, blockStmt, annotationMap);
        }

        // Handle Function Annotations.
        handleFunctionAnnotations(pkgNode, initFunction, blockStmt, annotationMap);

        for (BLangVariable variable : pkgNode.globalVars) {
            generateAnnotations(variable, variable.symbol.name.value, initFunction, blockStmt, annotationMap);
        }

        int index = calculateIndex(initFunction.body.stmts);

        for (BLangStatement stmt : blockStmt.stmts) {
            stmt.pos = pkgNode.pos;
            initFunction.body.stmts.add(index++, stmt);
        }

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
                                           BLangBlockStmt bLangBlockStmt,
                                           BLangSimpleVariable annotationMap) {
        for (BLangFunction function : pkgNode.functions) {
            generateAnnotations(function, function.symbol.name.value, initFunction, bLangBlockStmt, annotationMap);
            if (function.symbol.name.getValue().equals("main")) {
                addVarArgsAnnotation(function);
            }
        }

        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            generateAnnotations(typeDef, typeDef.name.value, initFunction, bLangBlockStmt, annotationMap);
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            if (typeDef.symbol.type.tag == TypeTags.OBJECT) {
                BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDef.typeNode;
                for (BLangSimpleVariable field : objectTypeNode.fields) {
                    String key = typeDef.name.value + DOT + field.name.value;
                    generateAnnotations(field, key, initFunction, bLangBlockStmt, annotationMap);
                }
            } else if (typeDef.symbol.type.tag == TypeTags.RECORD) {
                BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
                for (BLangSimpleVariable field : recordTypeNode.fields) {
                    String key = typeDef.name.value + DOT + field.name.value;
                    generateAnnotations(field, key, initFunction, bLangBlockStmt, annotationMap);
                }
            }
        }
    }

    private void addVarArgsAnnotation(BLangFunction mainFunc) {
        DiagnosticPos pos = mainFunc.pos;
        // Create Annotation Attachment.
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        mainFunc.addAnnotationAttachment(annoAttachment);
        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get(mainFunc.symbol.getEnclosingSymbol());
        BSymbol annSymbol = symResolver.lookupSymbolInPackage(mainFunc.pos, pkgEnv, names.fromString
                (BUILTIN_PKG_KEY), names.fromString(DEFAULTABLE_ARGS), SymTag.ANNOTATION);
        if (annSymbol instanceof BAnnotationSymbol) {
            annoAttachment.annotationSymbol = (BAnnotationSymbol) annSymbol;
        }
        IdentifierNode identifierNode = TreeBuilder.createIdentifierNode();
        if (identifierNode instanceof BLangIdentifier) {
            annoAttachment.annotationName = (BLangIdentifier) identifierNode;
        }
        annoAttachment.annotationName.value = DEFAULTABLE_ARGS;
        annoAttachment.pos = pos;
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createArrayLiteralNode();

        annoAttachment.expr = literalNode;
        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue(BUILTIN_PKG_KEY);
        annoAttachment.pkgAlias = pkgAlias;
        annoAttachment.attachPoints.add(AttachPoint.FUNCTION);
        literalNode.pos = pos;
        BStructureTypeSymbol bStructSymbol = null;
        BSymbol annTypeSymbol = symResolver.lookupSymbolInPackage(mainFunc.pos, pkgEnv, names.fromString
                (BUILTIN_PKG_KEY), names.fromString(ARG_NAMES), SymTag.STRUCT);
        if (annTypeSymbol instanceof BStructureTypeSymbol) {
            bStructSymbol = (BStructureTypeSymbol) annTypeSymbol;
            literalNode.type = bStructSymbol.type;
        }

        //Add Root Descriptor
        BLangRecordLiteral.BLangRecordKeyValue descriptorKeyValue = (BLangRecordLiteral.BLangRecordKeyValue)
                TreeBuilder.createRecordKeyValue();
        literalNode.keyValuePairs.add(descriptorKeyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = DEFAULTABLE_FIELD;
        keyLiteral.type = symTable.arrayType;
        ((BArrayType) keyLiteral.type).eType = symTable.stringType;

        BLangArrayLiteral valueLiteral = (BLangArrayLiteral) TreeBuilder.createLiteralExpression();

        for (BVarSymbol varSymbol : mainFunc.originalFuncSymbol.defaultableParams) {
            BLangLiteral str = (BLangLiteral) TreeBuilder.createLiteralExpression();
            str.value = varSymbol.name;
            str.type = symTable.stringType;
            valueLiteral.exprs.add(str);
        }

        descriptorKeyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        BSymbol fieldSymbol = symResolver.resolveStructField(mainFunc.pos, pkgEnv,
                names.fromString(DEFAULTABLE_FIELD), bStructSymbol);
        if (fieldSymbol instanceof BVarSymbol) {
            descriptorKeyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        }
        if (valueLiteral != null) {
            descriptorKeyValue.valueExpr = valueLiteral;
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
}
