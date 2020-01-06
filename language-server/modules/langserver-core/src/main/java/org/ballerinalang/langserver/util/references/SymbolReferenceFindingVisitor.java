/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util.references;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Node Visitor to find the symbol references in different compilation units in multiple packages.
 *
 * @since 0.990.4
 */
public class SymbolReferenceFindingVisitor extends LSNodeVisitor {

    private LSContext lsContext;
    private SymbolReferencesModel symbolReferences;
    private String tokenName;
    private int cursorLine;
    private int cursorCol;
    private boolean currentCUnitMode;
    private String pkgName;
    private List<TopLevelNode> topLevelNodes = new ArrayList<>();
    private List<BLangFunction> workerLambdas = new ArrayList<>();
    private List<BLangTypeDefinition> anonTypeDefinitions = new ArrayList<>();
    private HashMap<DiagnosticPos, BSymbol> workerVarDefPositionMap = new HashMap<>();
    private HashMap<String, BSymbol> workerNamesMap = new HashMap<>();

    SymbolReferenceFindingVisitor(LSContext lsContext, String pkgName, boolean currentCUnitMode) {
        this.lsContext = lsContext;
        this.symbolReferences = lsContext.get(NodeContextKeys.REFERENCES_KEY);
        this.tokenName = lsContext.get(NodeContextKeys.NODE_NAME_KEY);
        TextDocumentPositionParams position = lsContext.get(DocumentServiceKeys.POSITION_KEY);
        if (position == null) {
            throw new IllegalStateException("Position information not available in the Operation Context");
        }
        this.cursorLine = position.getPosition().getLine();
        this.cursorCol = position.getPosition().getCharacter();
        this.currentCUnitMode = currentCUnitMode;
        this.pkgName = pkgName;
    }

    SymbolReferenceFindingVisitor(LSContext lsContext, String pkgName) {
        this(lsContext, pkgName, false);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        String currentPkgName = this.lsContext.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String currentCUnitName = this.lsContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);

        // Avoid visiting the current compilation unit if the mode is not current cUnit mode
        if (currentPkgName.equals(this.pkgName) && currentCUnitName.equals(compUnit.name) && !this.currentCUnitMode) {
            return;
        }
        this.topLevelNodes = compUnit.getTopLevelNodes();
        List<TopLevelNode> filteredNodes = topLevelNodes.stream().filter(topLevelNode -> {
            if (topLevelNode instanceof BLangFunction) {
                if (((BLangFunction) topLevelNode).flagSet.contains(Flag.WORKER)) {
                    workerLambdas.add((BLangFunction) topLevelNode);
                    return false;
                }
                return !((BLangFunction) topLevelNode).flagSet.contains(Flag.LAMBDA);
            } else if (topLevelNode instanceof BLangTypeDefinition
                    && ((BLangTypeDefinition) topLevelNode).flagSet.contains(Flag.ANONYMOUS)) {
                anonTypeDefinitions.add((BLangTypeDefinition) topLevelNode);
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        filteredNodes.forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        if (xmlnsNode.prefix.value.equals(this.tokenName)) {
            DiagnosticPos pos = xmlnsNode.getPrefix().getPosition();
            this.addSymbol(xmlnsNode, xmlnsNode.symbol, true, pos);
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        if (constant.name.value.equals(this.tokenName)) {
            DiagnosticPos pos = (DiagnosticPos) constant.getName().getPosition();
            this.addSymbol(constant, constant.symbol, true, pos);
        }
        this.acceptNode(constant.typeNode);
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (serviceNode.name.value.equals(this.tokenName)) {
            Optional<BVarSymbol> serviceVarSymbol = this.getServiceVarSymbol();
            if (!serviceVarSymbol.isPresent()) {
                return;
            }
            DiagnosticPos pos = serviceNode.getName().getPosition();
            this.addSymbol(serviceNode, serviceVarSymbol.get(), true, pos);
        }
        serviceNode.annAttachments.forEach(this::acceptNode);
        if (serviceNode.attachedExprs != null) {
            serviceNode.attachedExprs.forEach(this::acceptNode);
        }
        BLangType typeNode = serviceNode.getTypeDefinition().typeNode;
        if (typeNode instanceof BLangObjectTypeNode) {
            ((BLangObjectTypeNode) typeNode).functions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        boolean isWorker = funcNode.flagSet.contains(Flag.WORKER);
        String funcName = isWorker ? funcNode.defaultWorkerName.value : funcNode.name.value;
        if (funcName.equals(this.tokenName)) {
            this.addBLangFunctionSymbol(funcNode);
        }
        funcNode.annAttachments.forEach(this::acceptNode);
        funcNode.requiredParams.forEach(this::acceptNode);
        this.acceptNode(funcNode.restParam);
        funcNode.externalAnnAttachments.forEach(this::acceptNode);
        funcNode.returnTypeAnnAttachments.forEach(this::acceptNode);
        this.acceptNode(funcNode.returnTypeNode);
        if (!isWorker && funcNode.body != null) {
            // Fill the worker varDefs in the current function scope
            this.fillVisibleWorkerVarDefMaps(funcNode.body.stmts);
        }
        this.acceptNode(funcNode.body);
        if (!isWorker) {
            // Clear the worker varDefs in the current function scope
            this.workerVarDefPositionMap.clear();
            this.workerNamesMap.clear();
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.flagSet.contains(Flag.SERVICE) || typeDefinition.name.value.contains("$")) {
            // skip if service type definition or anon type
            return;
        }
        if (typeDefinition.name.value.equals(this.tokenName)) {
            DiagnosticPos pos = typeDefinition.getName().getPosition();
            this.addSymbol(typeDefinition, typeDefinition.symbol, true, pos);
        }
        typeDefinition.annAttachments.forEach(this::acceptNode);

        // Visit the type node
        this.acceptNode(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.getStatements().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangIf ifNode) {
        // Visit the expression
        this.acceptNode(ifNode.expr);
        // Visit the body
        this.acceptNode(ifNode.body);
        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.collection);
        this.acceptNode((BLangNode) foreach.variableDefinitionNode);
        this.acceptNode(foreach.body);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.acceptNode(whileNode.expr);
        this.acceptNode(whileNode.body);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.acceptNode(transactionNode.retryCount);
        this.acceptNode(transactionNode.transactionBody);
        this.acceptNode(transactionNode.onRetryBody);
        this.acceptNode(transactionNode.committedBody);
        this.acceptNode(transactionNode.abortedBody);
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.acceptNode(lockNode.body);
    }

    @Override
    public void visit(BLangMatch matchNode) {
        this.acceptNode(matchNode.expr);
        matchNode.patternClauses.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause staticBindingPatternClause) {
        this.acceptNode(staticBindingPatternClause.literal);
        this.acceptNode(staticBindingPatternClause.body);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause structuredBindingPatternClause) {
        super.visit(structuredBindingPatternClause);
        this.acceptNode(structuredBindingPatternClause.bindingPatternVariable);
        this.acceptNode(structuredBindingPatternClause.body);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.variableList
                .forEach(variableKeyValue -> this.acceptNode(variableKeyValue.valueBindingPattern));
        if (bLangRecordVariable.restParam instanceof BLangNode) {
            this.acceptNode((BLangNode) bLangRecordVariable.restParam);
        }
        this.acceptNode(bLangRecordVariable.expr);
        this.acceptNode(bLangRecordVariable.typeNode);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        BLangType typeNode = varNode.typeNode;
        if (varNode.flagSet.contains(Flag.SERVICE)) {
            // Skip the anon service symbol generated for the BLangService,
            // which will be visited from BLangService visitor
            return;
        }
        if (varNode.name.value.equals(this.tokenName)) {
            DiagnosticPos pos = varNode.name.getPosition();
            this.addSymbol(varNode, varNode.symbol, true, pos);
        } else {
            this.acceptNode(typeNode);
        }
        varNode.annAttachments.forEach(this::acceptNode);
        this.acceptNode(varNode.expr);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        this.acceptNode(bLangErrorVariable.typeNode);
        this.acceptNode(bLangErrorVariable.reason);
        for (BLangErrorVariable.BLangErrorDetailEntry bLangErrorDetailEntry : bLangErrorVariable.detail) {
            this.acceptNode(bLangErrorDetailEntry.valueBindingPattern);
        }
        this.acceptNode(bLangErrorVariable.restDetail);
        this.acceptNode(bLangErrorVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        bLangTupleVariable.memberVariables.forEach(this::acceptNode);
        this.acceptNode(bLangTupleVariable.getRestVariable());
        this.acceptNode(bLangTupleVariable.typeNode);
        this.acceptNode(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangSimpleVariable variable = varDefNode.var;
        BLangType typeNode = variable.typeNode;
        if (varDefNode.isWorker) {
            if (varDefNode.var.expr instanceof BLangLambdaFunction
                    && ((BLangLambdaFunction) varDefNode.var.expr).function.flagSet.contains(Flag.WORKER)) {
                return;
            }
            Optional<BLangFunction> workerFunction = this.getWorkerFunctionFromPosition(variable.pos);
            workerFunction.ifPresent(this::acceptNode);
            return;
        } else if (variable.name.value.equals(this.tokenName)) {
            DiagnosticPos pos = variable.getName().getPosition();
            this.addSymbol(variable, variable.symbol, true, pos);
        } else {
            // In the foreach's variable definition node, type becomes null and will be handled by the acceptNode
            this.acceptNode(typeNode);
        }

        // Visit the expression
        this.acceptNode(varDefNode.var.expr);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        this.acceptNode(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        this.acceptNode(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.varRef.expressions.forEach(this::acceptNode);
        this.acceptNode(stmt.expr);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        this.acceptNode(stmt.varRef);
        this.acceptNode(stmt.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.acceptNode(panicNode.expr);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        this.acceptNode(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        this.acceptNode(assignNode.varRef);
        // Visit the expression
        this.acceptNode(assignNode.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.varRef);
        this.acceptNode(compoundAssignNode.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.getVariableName().value.equals(this.tokenName)) {
            DiagnosticPos pos = varRefExpr.getVariableName().getPosition();
            this.addSymbol(varRefExpr, varRefExpr.symbol, false, pos);
        } else if (varRefExpr.pkgAlias != null && varRefExpr.pkgAlias.value.equals(this.tokenName)) {
            DiagnosticPos pos = varRefExpr.pkgAlias.getPosition();
            this.addSymbol(varRefExpr, varRefExpr.pkgSymbol, false, pos);
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        this.acceptNode(indexAccessExpr.expr);
        if (!(indexAccessExpr.indexExpr instanceof BLangLiteral)) {
            // Visit the index expression only if it's not a simple literal since there is no use otherwise
            this.acceptNode(indexAccessExpr.indexExpr);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // Ex: e.name
        // e is expr and name is the symbol of fieldAccessExpr
        this.acceptNode(fieldAccessExpr.expr);
        if (fieldAccessExpr.field.value.equals(this.tokenName)) {
            DiagnosticPos symbolPos = fieldAccessExpr.getFieldName().getPosition();
            this.addSymbol(fieldAccessExpr, fieldAccessExpr.symbol, false, symbolPos);
        }
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        this.acceptNode(xmlAttributeAccessExpr.expr);
        this.acceptNode(xmlAttributeAccessExpr.indexExpr);
        /*
        Example:
        xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
        x1@[ns0:foo1] = "bar1";
        */
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.expressions.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.recordRefFields.forEach(varRefKeyVal -> this.acceptNode(varRefKeyVal.variableReference));
        if (varRefExpr.restParam instanceof BLangSimpleVarRef) {
            this.acceptNode((BLangSimpleVarRef) varRefExpr.restParam);
        }
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        this.acceptNode(arrayType.elemtype);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // TODO: Complete
        super.visit(builtInRefType);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        this.acceptNode(constrainedType.type);
        this.acceptNode(constrainedType.constraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        // For the inline type Descriptors, following logic applies. Check test case defTypeDesc13 
        Optional<BLangTypeDefinition> anonType = this.getAnonTypeFromPosition(userDefinedType.pos);
        if (anonType.isPresent()) {
            this.acceptNode(anonType.get().typeNode);
            return;
        }
        if (!this.isMatchingUserDefinedType(userDefinedType)) {
            return;
        }
        DiagnosticPos position = userDefinedType.getTypeName().getPosition();
        this.addSymbol(userDefinedType, userDefinedType.type.tsymbol, false, position);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        functionTypeNode.params.forEach(this::acceptNode);
        this.acceptNode(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        unionTypeNode.getMemberTypeNodes().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        objectTypeNode.typeRefs.forEach(this::addObjectReferenceType);
        objectTypeNode.fields.forEach(this::acceptNode);
        objectTypeNode.functions.forEach(this::acceptNode);
        this.acceptNode(objectTypeNode.initFunction);
        
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        // Type name is handled at the BLangTypeDefinition visitor and here we visit the fields
        recordTypeNode.fields.forEach(this::acceptNode);
        this.acceptNode(recordTypeNode.restFieldType);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        // TODO: Complete
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        tupleTypeNode.memberTypeNodes.forEach(this::acceptNode);
        this.acceptNode(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        this.acceptNode(errorType.reasonType);
        this.acceptNode(errorType.detailType);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        this.acceptNode(typeTestExpr.expr);
        this.acceptNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        this.acceptNode(ternaryExpr.expr);
        this.acceptNode(ternaryExpr.thenExpr);
        this.acceptNode(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        BLangFunction funcNode = bLangLambdaFunction.function;
        funcNode.annAttachments.forEach(this::acceptNode);
        funcNode.requiredParams.forEach(this::acceptNode);
        if (funcNode.restParam != null) {
            this.acceptNode(funcNode.restParam);
            this.acceptNode(funcNode.restParam.typeNode);
        }
        funcNode.externalAnnAttachments.forEach(this::acceptNode);
        funcNode.returnTypeAnnAttachments.forEach(this::acceptNode);
        this.acceptNode(funcNode.returnTypeNode);
        this.acceptNode(funcNode.body);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        this.acceptNode(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // Ex: int test = args.length() - args is the expression here
        this.acceptNode(invocationExpr.expr);
        if (invocationExpr.getName().getValue().equals(this.tokenName)) {
            // Ex: int test = returnIntFunc() - returnInt() or e.getName() is a BLangInvocation and name is getName
            DiagnosticPos symbolPos = (DiagnosticPos) invocationExpr.getName().getPosition();
            this.addSymbol(invocationExpr, invocationExpr.symbol, false, symbolPos);
        }
        invocationExpr.requiredArgs.forEach(this::acceptNode);
        invocationExpr.argExprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        this.acceptNode(groupExpr.expression);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        /*
        Temporarily disable the check since the BLangPackageBuilder cannot handle the positions correctly for bellow
         return arr[0] + arr[1] + arr[2];
         */
        // Visit left and right expressions
        this.acceptNode(binaryExpr.lhsExpr);
        this.acceptNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        this.acceptNode(checkedExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        this.acceptNode(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.exprList.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> {
            this.acceptNode(bLangRecordKeyValue.key.expr);
            this.acceptNode(bLangRecordKeyValue.valueExpr);
        });
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.acceptNode(returnNode.expr);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        this.acceptNode(conversionExpr.typeNode);
        this.acceptNode(conversionExpr.expr);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        if (typeInit.initInvocation != null && typeInit.initInvocation.name.value.equals(this.tokenName)) {
            // This logic get executed when hover over the new keyword in the below example
            // new(arg1, arg2)
            BSymbol symbol = typeInit.initInvocation.symbol;
            if (symbol == null) {
                symbol = typeInit.type.tsymbol;
            }
            this.addSymbol(typeInit.initInvocation, symbol, false, typeInit.initInvocation.name.pos);
        } else if (typeInit.userDefinedType != null) {
            this.acceptNode(typeInit.userDefinedType);
        }
        typeInit.argsExpr.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        this.acceptNode(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        annotationNode.annAttachments.forEach(this::acceptNode);
        if (annotationNode.name.value.equals(this.tokenName)) {
            DiagnosticPos pos = (DiagnosticPos) annotationNode.getName().getPosition();
            this.addSymbol(annotationNode, annotationNode.symbol, true, pos);
        }
        this.acceptNode(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (annAttachmentNode.annotationName.value.equals(this.tokenName)) {
            DiagnosticPos pos = (DiagnosticPos) annAttachmentNode.getAnnotationName().getPosition();
            this.addSymbol(annAttachmentNode, annAttachmentNode.annotationSymbol, false, pos);
        }
        this.acceptNode(annAttachmentNode.expr);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        this.acceptNode(unaryExpr.expr);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        this.acceptNode(annotAccessExpr.expr);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        this.acceptNode(workerSendNode.expr);
        if (workerSendNode.workerIdentifier.value.equals(this.tokenName)) {
            DiagnosticPos pos = workerSendNode.getWorkerName().getPosition();
            this.addSymbol(workerSendNode, workerNamesMap.get(this.tokenName), false, pos);
        }
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        this.acceptNode(syncSendExpr.expr);
        if (syncSendExpr.workerIdentifier.value.equals(this.tokenName)) {
            DiagnosticPos pos = (DiagnosticPos) syncSendExpr.getWorkerName().getPosition();
            this.addSymbol(syncSendExpr, workerNamesMap.get(this.tokenName), false, pos);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        if (workerReceiveNode.workerIdentifier.value.equals(this.tokenName)) {
            DiagnosticPos pos = workerReceiveNode.getWorkerName().getPosition();
            this.addSymbol(workerReceiveNode, workerNamesMap.get(this.tokenName), false, pos);
        }
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        this.acceptNode(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        this.acceptNode(accessExpr.typeNode);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.params.forEach(this::acceptNode);
        this.acceptNode(bLangArrowFunction.expression);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        this.acceptNode(elvisExpr.lhsExpr);
        this.acceptNode(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        this.acceptNode(trapExpr.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        waitForAllExpr.getKeyValuePairs().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        this.acceptNode(waitKeyValue.keyExpr);
        this.acceptNode(waitKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        this.acceptNode(stmt.varRef);
        this.acceptNode(stmt.expr);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        this.acceptNode(varRefExpr.typeNode);
        this.acceptNode(varRefExpr.reason);
        varRefExpr.detail.forEach(bLangNamedArgsExpression -> this.acceptNode(bLangNamedArgsExpression.expr));
        this.acceptNode(varRefExpr.restVar);
    }

    private void acceptNode(BLangNode node) {
        if (node == null) {
            return;
        }
        node.accept(this);
    }

    private boolean isMatchingUserDefinedType(BLangUserDefinedType bType) {
        return bType.typeName.value.equals(this.tokenName);
    }

    private SymbolReferencesModel.Reference getSymbolReference(DiagnosticPos position, BSymbol symbol,
                                                               BLangNode bLangNode) {
        return new SymbolReferencesModel.Reference(position, symbol, bLangNode);
    }
    
    private void addSymbol(BLangNode bLangNode, BSymbol bSymbol, boolean isDefinition, DiagnosticPos position) {
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = this.symbolReferences.getReferenceAtCursor();
        // Here, tsymbol check has been added in order to support the finite types
        // TODO: Handle finite type. After the fix check if it falsely capture symbols in other files with same name
        if (bSymbol == null || (!this.currentCUnitMode && symbolAtCursor.isPresent()
                && (symbolAtCursor.get().getSymbol() != bSymbol))) {
            return;
        }
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(position);
        BSymbol originalSymbol = (bSymbol instanceof BVarSymbol && ((BVarSymbol) bSymbol).originalSymbol != null)
                ? ((BVarSymbol) bSymbol).originalSymbol
                : bSymbol;
        SymbolReferencesModel.Reference ref = this.getSymbolReference(zeroBasedPos, originalSymbol, bLangNode);
        if (this.currentCUnitMode && this.cursorLine == zeroBasedPos.sLine && this.cursorCol >= zeroBasedPos.sCol
                && this.cursorCol <= zeroBasedPos.eCol) {
            // This is the symbol at current cursor position
            this.symbolReferences.setReferenceAtCursor(ref);
            if (isDefinition) {
                this.symbolReferences.addDefinition(ref);
            }
            return;
        }

        if (isDefinition) {
            this.symbolReferences.addDefinition(ref);
            return;
        }

        this.symbolReferences.addReference(ref);
    }
    
    private Optional<BVarSymbol> getServiceVarSymbol() {
        return this.topLevelNodes.stream().filter(node -> node instanceof BLangSimpleVariable
                && this.tokenName.equals(((BLangSimpleVariable) node).name.value))
                .map(topLevelNode -> ((BLangSimpleVariable) topLevelNode).symbol)
                .findAny();
    }
    
    private void addBLangFunctionSymbol(BLangFunction funcNode) {
        boolean isDefinition = !funcNode.flagSet.contains(Flag.INTERFACE);
        DiagnosticPos pos = funcNode.getName().getPosition();

        BSymbol symbol = funcNode.flagSet.contains(Flag.WORKER)
                ? this.workerVarDefPositionMap.get(funcNode.pos) : funcNode.symbol;
        this.addSymbol(funcNode, symbol, isDefinition, pos);
    }
    
    private Optional<BLangFunction> getWorkerFunctionFromPosition(DiagnosticPos position) {
        return this.workerLambdas.stream().filter(function -> function.getPosition() == position).findAny();
    }
    
    private Optional<BLangTypeDefinition> getAnonTypeFromPosition(DiagnosticPos position) {
        return this.anonTypeDefinitions.stream().filter(anonTypeDef -> anonTypeDef.getPosition() == position).findAny();
    }
    
    private void fillVisibleWorkerVarDefMaps(List<BLangStatement> statements) {
        statements.forEach(bLangStatement -> {
            if (bLangStatement instanceof BLangSimpleVariableDef && bLangStatement.getWS() == null) {
                BLangSimpleVariable variable = ((BLangSimpleVariableDef) bLangStatement).var;
                this.workerVarDefPositionMap.put(bLangStatement.pos, variable.symbol);
                this.workerNamesMap.put(variable.name.value, variable.symbol);
            }
        });
    }
    
    private void addObjectReferenceType(BLangType bLangType) {
        if (!(bLangType instanceof BLangUserDefinedType)
                || !((BLangUserDefinedType) bLangType).typeName.getValue().equals(this.tokenName)
                || !(bLangType.type instanceof BObjectType)) {
            return;
        }
        DiagnosticPos diagnosticPos = bLangType.pos;
        BObjectType objectType = (BObjectType) bLangType.type;
        DiagnosticPos pos = new DiagnosticPos(diagnosticPos.src, diagnosticPos.sLine, diagnosticPos.eLine,
                diagnosticPos.sCol, diagnosticPos.eCol);
        this.addSymbol(bLangType, objectType.tsymbol, false, pos);
    }
}
