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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.FoldingRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor class for folding ranges.
 */
public class FoldingRangeFinder extends NodeVisitor {

    private static final String COMMENT = "comment";
    private static final String REGION = "region";

    private final List<FoldingRange> foldingRanges;
    private final List<ImportDeclarationNode> imports;
    private final boolean lineFoldingOnly;

    FoldingRangeFinder(boolean lineFoldingOnly) {
        this.foldingRanges = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.lineFoldingOnly = lineFoldingOnly;
    }

    public void visit(ImportDeclarationNode importDeclarationNode) {
        imports.add(importDeclarationNode);
    }

    public void visit(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        LineRange lineRange = recordTypeDescriptorNode.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line() - 1,
                lineRange.startLine().offset(), lineRange.endLine().offset(), REGION));
    }

    public void visit(IfElseStatementNode ifElseStatementNode) {
        LineRange lineRange = ifElseStatementNode.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line() - 1,
                lineRange.startLine().offset(), lineRange.endLine().offset(), REGION));
        visitSyntaxNode(ifElseStatementNode);
    }

    public void visit(FunctionBodyBlockNode functionBodyBlockNode) {
        LineRange lineRange = functionBodyBlockNode.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line() - 1,
                lineRange.startLine().offset(), lineRange.endLine().offset(), REGION));
        visitSyntaxNode(functionBodyBlockNode);
    }

    public void visit(ClassDefinitionNode classDefinitionNode) {
        foldingRanges.add(createFoldingRange(classDefinitionNode.openBrace().lineRange().startLine().line(),
                classDefinitionNode.closeBrace().lineRange().endLine().line() - 1,
                classDefinitionNode.openBrace().lineRange().startLine().offset(),
                classDefinitionNode.closeBrace().lineRange().endLine().offset(), REGION));
        visitSyntaxNode(classDefinitionNode);
    }

    public void visit(MethodDeclarationNode methodDeclarationNode) {
        int functionKeyWordLine = methodDeclarationNode.functionKeyword().lineRange().startLine().line();
        int semicolonLine = methodDeclarationNode.semicolon().lineRange().startLine().line();
        foldingRanges.add(createFoldingRange(functionKeyWordLine, semicolonLine,
                methodDeclarationNode.functionKeyword().lineRange().startLine().offset(),
                methodDeclarationNode.semicolon().lineRange().startLine().offset(), REGION));
        visitSyntaxNode(methodDeclarationNode);
    }

    public void visit(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        foldingRanges.add(createFoldingRange(objectTypeDescriptorNode.openBrace().lineRange().startLine().line(),
                objectTypeDescriptorNode.closeBrace().lineRange().endLine().line() - 1,
                objectTypeDescriptorNode.openBrace().lineRange().startLine().offset(),
                objectTypeDescriptorNode.closeBrace().lineRange().endLine().offset(), REGION));
        visitSyntaxNode(objectTypeDescriptorNode);
    }

    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        foldingRanges.add(createFoldingRange(serviceDeclarationNode.openBraceToken().lineRange().startLine().line(),
                serviceDeclarationNode.closeBraceToken().lineRange().endLine().line() - 1,
                serviceDeclarationNode.openBraceToken().lineRange().startLine().offset(),
                serviceDeclarationNode.closeBraceToken().lineRange().startLine().offset(), REGION));
        visitSyntaxNode(serviceDeclarationNode);
    }

    public void visit(MetadataNode metadataNode) {
        LineRange lineRange = metadataNode.lineRange();
        foldingRanges.add(createFoldingRange(lineRange.startLine().line(), lineRange.endLine().line(),
                lineRange.startLine().offset(), lineRange.endLine().offset(), COMMENT));
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
                    lastImport.semicolon().lineRange().endLine().offset(), FoldingRangeFinder.REGION));
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
}
