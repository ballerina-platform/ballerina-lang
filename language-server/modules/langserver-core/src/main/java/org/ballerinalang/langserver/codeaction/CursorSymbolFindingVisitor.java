/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.SymbolReferenceFindingVisitor;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Node Visitor to find the symbol in different compilation units in multiple packages.
 * <p>
 * Intended to use of 'CodeAction' services.
 *
 * @since 1.2.0
 */
public class CursorSymbolFindingVisitor extends SymbolReferenceFindingVisitor {

    private Predicate<DiagnosticPos> isWithinNode;

    public CursorSymbolFindingVisitor(Token tokenAtCursor,
                                      LSContext lsContext, String pkgName, boolean currentCUnitMode) {
        super(lsContext, tokenAtCursor, pkgName, currentCUnitMode);
        this.lsContext = lsContext;

        Boolean bDoNotSkipNullSymbols = lsContext.get(ReferencesKeys.DO_NOT_SKIP_NULL_SYMBOLS);
        this.doNotSkipNullSymbols = (bDoNotSkipNullSymbols == null) ? false : bDoNotSkipNullSymbols;

        this.symbolReferences = new SymbolReferencesModel();
        lsContext.put(ReferencesKeys.REFERENCES_KEY, this.symbolReferences);

        this.tokenName = tokenAtCursor.text();
        TextDocumentPositionParams position = lsContext.get(DocumentServiceKeys.POSITION_KEY);
        if (position == null) {
            throw new IllegalStateException("Position information not available in the Operation Context");
        }
        this.cursorLine = position.getPosition().getLine();
        this.cursorCol = position.getPosition().getCharacter();
        this.currentCUnitMode = currentCUnitMode;
        this.pkgName = pkgName;
        this.isWithinNode = pos -> (
                // if node is single-line
                ((pos.sLine == pos.eLine && cursorLine == pos.sLine) &&
                        (cursorCol >= pos.sCol && cursorCol <= pos.eCol)) ||
                        // if node is multi-line
                        ((pos.sLine != pos.eLine) && (
                                cursorLine > pos.sLine && cursorLine < pos.eLine ||
                                        cursorLine == pos.eLine && cursorCol <= pos.eCol ||
                                        cursorLine == pos.sLine && cursorCol >= pos.sCol
                        ))
        );
    }

    @Override
    protected void addSymbol(BLangNode bLangNode, BSymbol bSymbol, boolean isDefinition, DiagnosticPos position) {
        SymbolReferencesModel.Reference symbolAtCursor = this.symbolReferences.getReferenceAtCursor();
        // Here, tsymbol check has been added in order to support the finite types
        // TODO: Handle finite type. After the fix check if it falsely capture symbols in other files with same name
        if (bSymbol == null && !this.doNotSkipNullSymbols) {
            return;
        }
        if (symbolAtCursor != null) {
            return;
        }
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(position);
        bSymbol = (bSymbol instanceof BVarSymbol && ((BVarSymbol) bSymbol).originalSymbol != null)
                ? ((BVarSymbol) bSymbol).originalSymbol
                : bSymbol;
        SymbolReferencesModel.Reference ref = this.getSymbolReference(zeroBasedPos, bSymbol, bLangNode);
        if (isWithinNode.test(zeroBasedPos)) {
            // This is the symbol at current cursor position
            this.symbolReferences.setReferenceAtCursor(ref);
        }
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
            Diagnostic.DiagnosticPosition position = topLevelNode.getPosition();
            DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(
                    new DiagnosticPos(null, position.getStartLine(), position.getEndLine(), position.getStartColumn(),
                                      position.getEndColumn()));
            boolean isLambda = false;
            if (topLevelNode instanceof BLangFunction) {
                BLangFunction func = (BLangFunction) topLevelNode;
                isLambda = func.flagSet.contains(Flag.LAMBDA);
            }
            return this.currentCUnitMode && !isLambda && isWithinNode.test(zeroBasedPos);
        }).collect(Collectors.toList());
        filteredNodes.forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(literalExpr.pos))) {
            this.addSymbol(literalExpr, literalExpr.type.tsymbol, false, literalExpr.pos);
        }
        super.visit(literalExpr);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
//       BLangStructLiteral
//       BLangMapLiteral
//       BLangTableLiteral
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(recordLiteral.pos))) {
            this.addSymbol(recordLiteral, recordLiteral.type.tsymbol, false, recordLiteral.pos);
        }
        super.visit(recordLiteral);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // Checking parent's position since this gives "`<a></a>`" position on "xml `<a></a>`"
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(xmlElementLiteral.parent.pos))) {
            this.addSymbol(xmlElementLiteral, xmlElementLiteral.type.tsymbol, false, xmlElementLiteral.parent.pos);
        }
        super.visit(xmlElementLiteral);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(xmlTextLiteral.parent.pos))) {
            this.addSymbol(xmlTextLiteral, xmlTextLiteral.type.tsymbol, false, xmlTextLiteral.parent.pos);
        }
        super.visit(xmlTextLiteral);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(xmlCommentLiteral.parent.pos))) {
            this.addSymbol(xmlCommentLiteral, xmlCommentLiteral.type.tsymbol, false, xmlCommentLiteral.parent.pos);
        }
        super.visit(xmlCommentLiteral);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(xmlProcInsLiteral.parent.pos))) {
            this.addSymbol(xmlProcInsLiteral, xmlProcInsLiteral.type.tsymbol, false, xmlProcInsLiteral.parent.pos);
        }
        super.visit(xmlProcInsLiteral);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(stringTemplateLiteral.pos))) {
            this.addSymbol(stringTemplateLiteral, stringTemplateLiteral.type.tsymbol, false, stringTemplateLiteral.pos);
        }
        super.visit(stringTemplateLiteral);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
//         BLangListConstructorExpr
//         BLangArrayLiteral
//         BLangJSONArrayLiteral
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(listConstructorExpr.pos))) {
            this.addSymbol(listConstructorExpr, listConstructorExpr.type.tsymbol, false, listConstructorExpr.pos);
        }
        super.visit(listConstructorExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(waitLiteral.pos))) {
            this.addSymbol(waitLiteral, waitLiteral.type.tsymbol, false, waitLiteral.pos);
        }
        super.visit(waitLiteral);
    }

    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(lambdaFunction.pos))) {
            this.addSymbol(lambdaFunction, lambdaFunction.type.tsymbol, false, lambdaFunction.pos);
        }
        super.visit(lambdaFunction);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(conversionExpr.pos))) {
            this.addSymbol(conversionExpr, conversionExpr.type.tsymbol, false, conversionExpr.pos);
        }
        super.visit(conversionExpr);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(queryExpr.pos))) {
            this.addSymbol(queryExpr, queryExpr.type.tsymbol, false, queryExpr.pos);
        }
        super.visit(queryExpr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (isWithinNode.test(CommonUtil.toZeroBasedPosition(binaryExpr.pos))) {
            this.addSymbol(binaryExpr, binaryExpr.type.tsymbol, false, binaryExpr.pos);
        }
        super.visit(binaryExpr);
    }
}
