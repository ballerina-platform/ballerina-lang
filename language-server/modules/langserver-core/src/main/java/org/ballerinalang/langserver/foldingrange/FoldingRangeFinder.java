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
package org.ballerinalang.langserver.foldingrange;

import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.ForkStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MatchClauseNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor class for folding ranges.
 */
public class FoldingRangeFinder extends NodeVisitor {

    private final List<FoldingRange> foldingRanges;
    private final List<ImportDeclarationNode> imports;
    private final boolean lineFoldingOnly;

    FoldingRangeFinder(boolean lineFoldingOnly) {
        this.foldingRanges = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.lineFoldingOnly = lineFoldingOnly;
    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {
        imports.add(importDeclarationNode);
    }

    @Override
    public void visit(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        appendNodeFoldingRanges(recordTypeDescriptorNode);
    }

    @Override
    public void visit(IfElseStatementNode ifElseStatementNode) {
        LineRange ifElseLineRange = ifElseStatementNode.lineRange();
        LineRange ifBodyLineRange = ifElseStatementNode.ifBody().lineRange();
        foldingRanges.removeIf(foldingRange -> ifElseLineRange.startLine().line() == foldingRange.getStartLine());
        foldingRanges.add(createFoldingRange(ifElseLineRange.startLine().line(), ifBodyLineRange.endLine().line() - 1,
                ifElseLineRange.startLine().offset(), ifBodyLineRange.endLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(ifElseStatementNode);
    }

    @Override
    public void visit(ElseBlockNode elseBlockNode) {
        appendNodeFoldingRanges(elseBlockNode);
        visitSyntaxNode(elseBlockNode);
    }

    @Override
    public void visit(FunctionBodyBlockNode functionBodyBlockNode) {
        appendNodeFoldingRanges(functionBodyBlockNode);
        visitSyntaxNode(functionBodyBlockNode);
    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {
        foldingRanges.add(createFoldingRange(classDefinitionNode.openBrace().lineRange().startLine().line(),
                classDefinitionNode.closeBrace().lineRange().endLine().line() - 1,
                classDefinitionNode.openBrace().lineRange().startLine().offset(),
                classDefinitionNode.closeBrace().lineRange().endLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(classDefinitionNode);
    }

    @Override
    public void visit(MethodDeclarationNode methodDeclarationNode) {
        int functionKeyWordLine = methodDeclarationNode.functionKeyword().lineRange().startLine().line();
        int semicolonLine = methodDeclarationNode.semicolon().lineRange().startLine().line();
        foldingRanges.add(createFoldingRange(functionKeyWordLine, semicolonLine,
                methodDeclarationNode.functionKeyword().lineRange().startLine().offset(),
                methodDeclarationNode.semicolon().lineRange().startLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(methodDeclarationNode);
    }

    @Override
    public void visit(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        foldingRanges.add(createFoldingRange(objectTypeDescriptorNode.openBrace().lineRange().startLine().line(),
                objectTypeDescriptorNode.closeBrace().lineRange().endLine().line() - 1,
                objectTypeDescriptorNode.openBrace().lineRange().startLine().offset(),
                objectTypeDescriptorNode.closeBrace().lineRange().endLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(objectTypeDescriptorNode);
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        foldingRanges.add(createFoldingRange(serviceDeclarationNode.openBraceToken().lineRange().startLine().line(),
                serviceDeclarationNode.closeBraceToken().lineRange().endLine().line() - 1,
                serviceDeclarationNode.openBraceToken().lineRange().startLine().offset(),
                serviceDeclarationNode.closeBraceToken().lineRange().startLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(serviceDeclarationNode);
    }

    @Override
    public void visit(MetadataNode metadataNode) {
        LineRange lineRange = metadataNode.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line(),
                lineRange.startLine().offset(), lineRange.endLine().offset(), FoldingRangeKind.Comment));
    }

    @Override
    public void visit(ForEachStatementNode forEachStatementNode) {
        appendBlockFoldingRanges(forEachStatementNode.blockStatement());
        visitSyntaxNode(forEachStatementNode);
    }

    @Override
    public void visit(WhileStatementNode whileStatementNode) {
        appendBlockFoldingRanges(whileStatementNode.whileBody());
        visitSyntaxNode(whileStatementNode);
    }

    @Override
    public void visit(EnumDeclarationNode enumDeclarationNode) {
        foldingRanges.add(createFoldingRange(enumDeclarationNode.openBraceToken().lineRange().startLine().line(),
                enumDeclarationNode.closeBraceToken().lineRange().endLine().line() - 1,
                enumDeclarationNode.openBraceToken().lineRange().startLine().offset(),
                enumDeclarationNode.closeBraceToken().lineRange().startLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(enumDeclarationNode);
    }

    @Override
    public void visit(TransactionStatementNode transactionStatementNode) {
        appendBlockFoldingRanges(transactionStatementNode.blockStatement());
        visitSyntaxNode(transactionStatementNode);
    }

    @Override
    public void visit(ExternalFunctionBodyNode externalFunctionBodyNode) {
        appendNodeFoldingRanges(externalFunctionBodyNode);
        visitSyntaxNode(externalFunctionBodyNode);
    }

    @Override
    public void visit(MatchStatementNode matchStatementNode) {
        foldingRanges.add(createFoldingRange(matchStatementNode.openBrace().lineRange().startLine().line(),
                matchStatementNode.closeBrace().lineRange().endLine().line() - 1,
                matchStatementNode.openBrace().lineRange().startLine().offset(),
                matchStatementNode.closeBrace().lineRange().startLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(matchStatementNode);
    }

    @Override
    public void visit(MatchClauseNode matchClauseNode) {
        appendBlockFoldingRanges(matchClauseNode.blockStatement());
        visitSyntaxNode(matchClauseNode);
    }

    @Override
    public void visit(ForkStatementNode forkStatementNode) {
        foldingRanges.add(createFoldingRange(forkStatementNode.openBraceToken().lineRange().startLine().line(),
                forkStatementNode.closeBraceToken().lineRange().endLine().line() - 1,
                forkStatementNode.openBraceToken().lineRange().startLine().offset(),
                forkStatementNode.closeBraceToken().lineRange().startLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(forkStatementNode);
    }

    @Override
    public void visit(RetryStatementNode retryStatementNode) {
        appendNodeFoldingRanges(retryStatementNode.retryBody());
        visitSyntaxNode(retryStatementNode);
    }

    @Override
    public void visit(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        appendBlockFoldingRanges(namedWorkerDeclarationNode.workerBody());
        visitSyntaxNode(namedWorkerDeclarationNode);
    }

    @Override
    public void visit(OnFailClauseNode onFailClauseNode) {
        appendBlockFoldingRanges(onFailClauseNode.blockStatement());
        visitSyntaxNode(onFailClauseNode);
    }

    @Override
    public void visit(DoStatementNode doStatementNode) {
        appendBlockFoldingRanges(doStatementNode.blockStatement());
        visitSyntaxNode(doStatementNode);
    }

    @Override
    public void visit(LockStatementNode lockStatementNode) {
        appendBlockFoldingRanges(lockStatementNode.blockStatement());
        visitSyntaxNode(lockStatementNode);
    }

    @Override
    public void visit(ReturnStatementNode returnStatementNode) {
        LineRange lineRange = returnStatementNode.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line(),
                lineRange.startLine().offset(), lineRange.endLine().offset(), FoldingRangeKind.Region));
        visitSyntaxNode(returnStatementNode);
    }

    public List<FoldingRange> getFoldingRange(Node node) {
        visitSyntaxNode(node);

        // Calculate folding range for imports
        int length = imports.size();
        if (length > 1) {
            ImportDeclarationNode firstImport = imports.get(0);
            ImportDeclarationNode lastImport = imports.get(length - 1);
            foldingRanges.add(createFoldingRange(firstImport.lineRange().startLine().line(),
                    lastImport.lineRange().endLine().line(),
                    firstImport.importKeyword().lineRange().startLine().offset(),
                    lastImport.semicolon().lineRange().endLine().offset(), FoldingRangeKind.Region));
        }
        return this.foldingRanges;
    }

    /**
     * Creates a FoldingRange instance.
     *
     * @param startLine      folding start line
     * @param endLine        folding end line
     * @param startCharacter folding start character
     * @param endCharacter   folding end character
     * @param kind           folding kind, i.e. comment or region
     * @return {@link FoldingRange}
     */
    public FoldingRange createFoldingRange(int startLine, int endLine, int startCharacter, int endCharacter,
                                           String kind) {
        FoldingRange foldingRange = new FoldingRange(startLine, endLine);
        foldingRange.setKind(kind);

        //TODO Add tests for character based folding
        if (!this.lineFoldingOnly) {
            foldingRange.setStartCharacter(startCharacter);
            foldingRange.setEndCharacter(endCharacter);
        }
        return foldingRange;
    }

    private void appendNodeFoldingRanges(Node node) {
        LineRange lineRange = node.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line() - 1,
                lineRange.startLine().offset(), lineRange.endLine().offset(), FoldingRangeKind.Region));
    }

    private void appendBlockFoldingRanges(BlockStatementNode blockStatementNode) {
        foldingRanges.add(createFoldingRange(blockStatementNode.openBraceToken().lineRange().startLine().line(),
                blockStatementNode.closeBraceToken().lineRange().endLine().line() - 1,
                blockStatementNode.openBraceToken().lineRange().startLine().offset(),
                blockStatementNode.closeBraceToken().lineRange().startLine().offset(), FoldingRangeKind.Region));
    }
}
