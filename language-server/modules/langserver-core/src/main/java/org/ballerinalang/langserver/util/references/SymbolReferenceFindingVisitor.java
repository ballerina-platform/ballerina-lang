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
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
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
    private boolean terminateVisitor = false;
    private SymbolReferencesModel symbolReferences;
//    private List<Integer> referenceLines;
    private String tokenName;
    private String cUnitName;
    private int cursorLine;
    private int cursorCol;
    private boolean currentCUnitMode;
    private String pkgName;

    public SymbolReferenceFindingVisitor(LSContext lsContext, String pkgName, Position pos, boolean currentCUnitMode) {
        this.lsContext = lsContext;
        this.symbolReferences = lsContext.get(NodeContextKeys.REFERENCES_KEY);
        this.tokenName = lsContext.get(NodeContextKeys.NODE_NAME_KEY);
        this.cursorLine = pos.getLine();
        this.cursorCol = pos.getCharacter();
        this.currentCUnitMode = currentCUnitMode;
        this.pkgName = pkgName;
    }

    public SymbolReferenceFindingVisitor(LSContext lsContext, String pkgName, Position pos) {
        this(lsContext, pkgName, pos, false);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        this.cUnitName = compUnit.name;
        String currentPkgName = this.lsContext.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String currentCUnitName = this.lsContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);

        // Avoid visiting the current compilation unit if the mode is not current cUnit mode
        if (currentPkgName.equals(this.pkgName) && currentCUnitName.equals(this.cUnitName) && !this.currentCUnitMode) {
            return;
        }
        compUnit.getTopLevelNodes().forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        if (xmlnsNode.prefix.value.equals(this.tokenName)) {
            this.addSymbol(xmlnsNode.symbol, true, xmlnsNode.pos);
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        if (constant.name.value.equals(this.tokenName)) {
            List<Whitespace> wsList = new ArrayList<>(constant.getWS());
            DiagnosticPos varPos = constant.getPosition();
            int sSol = varPos.sCol + this.getCharLengthBeforeToken(this.tokenName, wsList)
                    + this.getTypeLengthWithWS(constant.typeNode, true);
            int eCol = sSol + this.tokenName.length();
            DiagnosticPos pos = new DiagnosticPos(varPos.src, varPos.sLine, varPos.eLine, sSol, eCol);
            this.addSymbol(constant.symbol, true, pos);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        List<Whitespace> wsList = new ArrayList<>(serviceNode.getWS());
        if (serviceNode.name.value.equals(this.tokenName)) {
            DiagnosticPos servicePos = serviceNode.pos;
            int sCol = servicePos.sCol + wsList.get(1).toString().length();
            int eCol = sCol + this.tokenName.length();
            DiagnosticPos pos = new DiagnosticPos(servicePos.src, servicePos.sLine, servicePos.eLine, sCol, eCol);
            this.addSymbol(serviceNode.symbol, true, pos);
        }
        if (serviceNode.attachedExprs != null) {
            serviceNode.attachedExprs.forEach(this::acceptNode);
        }
        serviceNode.resourceFunctions.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        List<Whitespace> wsList = new ArrayList<>(funcNode.getWS());
        if (funcNode.getName().value.equals(this.tokenName)) {
            DiagnosticPos funcPos = funcNode.pos;
            int sCol = funcPos.sCol + this.getCharLengthBeforeToken(this.tokenName, wsList);
            int eCol = sCol + this.tokenName.length();
            DiagnosticPos pos = new DiagnosticPos(funcPos.src, funcPos.sLine, funcPos.sLine, sCol, eCol);
            this.addSymbol(funcNode.symbol, true, pos);
        }

        funcNode.defaultableParams.forEach(this::acceptNode);
        funcNode.requiredParams.forEach(this::acceptNode);
        this.acceptNode(funcNode.returnTypeNode);
        this.acceptNode(funcNode.body);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.flagSet.contains(Flag.SERVICE)) {
            return;
        }
        List<Whitespace> wsList = new ArrayList<>(typeDefinition.getWS());
        if (typeDefinition.name.value.equals(this.tokenName)) {
            DiagnosticPos typeDefPos = typeDefinition.pos;
            int sCol = typeDefPos.sCol + this.getCharLengthBeforeToken(this.tokenName, wsList);
            int eCol = sCol + this.tokenName.length();
            DiagnosticPos pos = new DiagnosticPos(typeDefPos.src, typeDefPos.sLine, typeDefPos.sLine, sCol, eCol);
            this.addSymbol(typeDefinition.symbol, true, pos);
        }

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
        // todo: support the literal visit when the constant support implemented in compiler
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
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        BLangType typeNode = varNode.typeNode;
        if (varNode.name.value.equals(this.tokenName)) {
            List<Whitespace> wsList = new ArrayList<>(varNode.getWS());
            DiagnosticPos varPos = varNode.getPosition();
            DiagnosticPos pos;
            // Type node is null when the variable is for type binding patterns
            if (typeNode != null) {
                boolean isNested = varNode.flagSet.contains(Flag.PUBLIC);
                int sSol = varPos.sCol + this.getCharLengthBeforeToken(this.tokenName, wsList);
                sSol += this.getTypeLengthWithWS(typeNode, isNested);
                int eCol = sSol + this.tokenName.length();
                pos = new DiagnosticPos(varPos.src, varPos.sLine, varPos.eLine, sSol, eCol);
            } else {
                pos = varPos;
            }
            this.addSymbol(varNode.symbol, true, pos);
        } else {
            this.acceptNode(typeNode);
        }
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        bLangTupleVariable.memberVariables.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangSimpleVariable variable = varDefNode.var;
        BLangType typeNode = variable.typeNode;
        if (variable.name.value.equals(this.tokenName)) {
            List<Whitespace> wsList = new ArrayList<>(varDefNode.getWS());
            DiagnosticPos varPos = varDefNode.getPosition();
            int sSol = varPos.sCol + this.getCharLengthBeforeToken(this.tokenName, wsList)
                    + this.getTypeLengthWithWS(variable.typeNode, false);
            int eCol = sSol + this.tokenName.length();
            DiagnosticPos pos = new DiagnosticPos(varPos.src, varPos.sLine, varPos.eLine, sSol, eCol);
            this.addSymbol(variable.symbol, true, pos);
        } else {
            this.acceptNode(typeNode);
        }

        // Visit the expression
        if (varDefNode.var.expr != null) {
            this.acceptNode(varDefNode.var.expr);
        }
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
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        super.visit(bLangErrorVariableDef);
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
            this.addSymbol(varRefExpr.symbol, false, varRefExpr.pos);
        } else if (varRefExpr.pkgAlias.value.equals(this.tokenName)) {
            this.addSymbol(varRefExpr.pkgSymbol, false, varRefExpr.pos);
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
        this.acceptNode(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        this.acceptNode(xmlAttributeAccessExpr.expr);
        // todo: visit the index expression
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
        // TODO: Complete
        super.visit(arrayType);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // TODO: Complete
        super.visit(builtInRefType);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        // TODO: Complete
        super.visit(constrainedType);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (!this.isMatchingUserDefinedType(userDefinedType)) {
            return;
        }
        this.addSymbol(userDefinedType.type.tsymbol, false, this.getTypeNamePosition(userDefinedType, this.tokenName));
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // TODO: Complete
        super.visit(functionTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        unionTypeNode.getMemberTypeNodes().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        // TODO: Complete
        super.visit(objectTypeNode);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        // TODO: Complete
        super.visit(recordTypeNode);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        // TODO: Complete
        super.visit(finiteTypeNode);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        // TODO: Complete
        tupleTypeNode.memberTypeNodes.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        // TODO: Complete
        super.visit(errorType);
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
        this.acceptNode(bLangLambdaFunction.function.body);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        this.acceptNode(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.getName().getValue().equals(this.tokenName)) {
            // Ex: int test = returnIntFunc() - returnInt() is a BLangInvocation
            DiagnosticPos invocationPos = invocationExpr.pos;
            DiagnosticPos pos = new DiagnosticPos(invocationPos.src, invocationPos.sLine, invocationPos.eLine,
                    invocationPos.sCol, invocationPos.sCol + this.tokenName.length());
            this.addSymbol(invocationExpr.symbol, false, pos);
        }
        if (invocationExpr.expr != null) {
            // Ex: int test = args.length() - args is the expression here
            this.acceptNode(invocationExpr.expr);
        }
        invocationExpr.argExprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.getExpressions().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        /*
        Temporarily disable the check since the BLangPackageBuilder cannot handle the positions correctly for bellow
         return arr[0] + arr[1] + arr[2];
         */
//        if (!this.isWithinNodeRange(binaryExpr)) {
//            return;
//        }
        // Visit left and right expressions
        this.acceptNode(binaryExpr.lhsExpr);
        this.acceptNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        this.acceptNode(checkedExpr.expr);
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(bLangRecordKeyValue -> this.acceptNode(bLangRecordKeyValue.valueExpr));
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.acceptNode(returnNode.expr);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        this.acceptNode(conversionExpr.expr);
    }

    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }

    private boolean isMatchingUserDefinedType(BLangUserDefinedType bType) {
        return bType.typeName.value.equals(this.tokenName);
    }

    private SymbolReferencesModel.Reference getSymbolReference(BSymbol symbol, DiagnosticPos position) {
        String pkgName = symbol.pkgID.nameComps.stream().map(Name::getValue).collect(Collectors.joining("."));
        return new SymbolReferencesModel.Reference(position, symbol, this.cUnitName, pkgName);
    }

    private void addSymbol(BSymbol bSymbol, boolean isDefinition, DiagnosticPos position) {
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = this.symbolReferences.getSymbolAtCursor();
        if (!this.currentCUnitMode && symbolAtCursor.isPresent() && (symbolAtCursor.get().getSymbol() != bSymbol
                && symbolAtCursor.get().getSymbol() != bSymbol.type.tsymbol)) {
            return;
        }
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(position);
        BSymbol originalSymbol = (bSymbol instanceof BVarSymbol && ((BVarSymbol) bSymbol).originalSymbol != null)
                ? ((BVarSymbol) bSymbol).originalSymbol
                : bSymbol;
        SymbolReferencesModel.Reference ref = this.getSymbolReference(originalSymbol, zeroBasedPos);
        if (this.currentCUnitMode && this.cursorLine == zeroBasedPos.sLine && this.cursorCol >= zeroBasedPos.sCol
                && this.cursorCol <= zeroBasedPos.eCol) {
            // This is the symbol at current cursor position
            this.symbolReferences.setSymbolAtCursor(ref);
            return;
        }

        if (isDefinition) {
            this.symbolReferences.addDefinition(ref);
            return;
        }

        this.symbolReferences.addReference(ref);
    }

    private int getCharLengthBeforeToken(String tokenName, List<Whitespace> whitespaces) {
        int length = 0;
        for (int i = 0; i < whitespaces.size(); i++) {
            String previous = whitespaces.get(i).getPrevious();
            if (previous.equals(tokenName) || i != 0) {
                length += whitespaces.get(i).getWs().length();
            }
            if (!previous.equals(tokenName)) {
                length += previous.length();
            } else {
                break;
            }
        }

        return length;
    }

    private int getTypeLengthWithWS(BLangType typeNode, boolean nested) {
        List<Whitespace> whitespaces = new ArrayList<>(typeNode.getWS());
        int length = 0;

        if (typeNode instanceof BLangUnionTypeNode) {
            length = getUnionTypeLength((BLangUnionTypeNode) typeNode, nested);
        } else if (typeNode instanceof BLangTupleTypeNode) {
            length += this.getTupleTypeLength((BLangTupleTypeNode) typeNode, nested);
        } else {
            int startCounter = 0;
            if (!nested) {
                length += whitespaces.get(0).getPrevious().length();
                startCounter++;
            }
            for (int i = startCounter; i < whitespaces.size(); i++) {
                Whitespace whitespace = whitespaces.get(i);
                length += whitespace.getPrevious().length() + whitespace.getWs().length();
            }
        }

        return length;
    }

    private int getTupleTypeLength(BLangTupleTypeNode tupleType, boolean nested) {
        int length = 0;
        List<BLangType> memberTypeNodes = tupleType.memberTypeNodes;
        List<Whitespace> tupleWSList = new ArrayList<>(tupleType.getWS());
        int startCounter = 0;
        if (!nested) {
            length += tupleWSList.get(0).getPrevious().length();
            startCounter++;
        }
        for (int i = startCounter; i < tupleWSList.size(); i++) {
            Whitespace whitespace = tupleWSList.get(i);
            length += whitespace.getPrevious().length() + whitespace.getWs().length();
        }

        for (BLangType memberTypeNode : memberTypeNodes) {
            length += this.getTypeLengthWithWS(memberTypeNode, true);
        }

        return length;
    }

    private int getUnionTypeLength(BLangUnionTypeNode unionTypeNode, boolean nested) {
        List<BLangType> memberTypeNodes = unionTypeNode.getMemberTypeNodes();
        List<Whitespace> firstTypeWs = new ArrayList<>(memberTypeNodes.get(0).getWS());
        List<Whitespace> unionWs = new ArrayList<>(unionTypeNode.getWS());
        int startCounter = 0;
        int length = 0;
        if (!nested) {
            length = firstTypeWs.get(0).getPrevious().length();
            startCounter++;
        }
        for (int i = startCounter; i < memberTypeNodes.size(); i++) {
            List<Whitespace> typeWs = new ArrayList<>(memberTypeNodes.get(i).getWS());
            for (Whitespace ws : typeWs) {
                length += ws.getPrevious().length() + ws.getWs().length();
            }
        }

        for (Whitespace ws : unionWs) {
            length += ws.getPrevious().length() + ws.getWs().length();
        }

        return length;
    }

    private DiagnosticPos getTypeNamePosition(BLangUserDefinedType userDefinedType, String typeName) {
        DiagnosticPos typePos = userDefinedType.pos;
        List<Whitespace> whitespaces = new ArrayList<>(userDefinedType.getWS());
        int offset = 0;
        if (whitespaces.size() > 1) {
            // handles the case - http:Response, otherwise the case is Foo where no package specified
            offset = whitespaces.get(0).getPrevious().length();
            for (int i = 1; i < whitespaces.size(); i++) {
                Whitespace ws = whitespaces.get(i);
                offset += ws.getWs().length() + ws.getPrevious().length();
                if (ws.getPrevious().equals(typeName)) {
                    break;
                }
            }
        }
        int sCol = typePos.sCol + offset;
        int eCol = sCol + typeName.length();

        return new DiagnosticPos(typePos.src, typePos.sLine, typePos.eLine, sCol, eCol);
    }
}
