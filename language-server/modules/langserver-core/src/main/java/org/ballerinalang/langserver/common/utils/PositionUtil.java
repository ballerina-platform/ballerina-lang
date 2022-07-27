/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Optional;

/**
 * Carries a set of utilities for position calculations.
 *
 * @since 2201.1.1
 */
public class PositionUtil {

    /**
     * Convert the syntax-node line range into a lsp4j range.
     *
     * @param lineRange - line range
     * @return {@link Range} converted range
     */
    public static Range toRange(LineRange lineRange) {
        return new Range(toPosition(lineRange.startLine()), toPosition(lineRange.endLine()));
    }

    /**
     * Convert the syntax-node line position into a lsp4j range.
     *
     * @param linePosition - line position.
     * @return {@link Range} converted range
     */
    public static Range toRange(LinePosition linePosition) {
        return new Range(toPosition(linePosition), toPosition(linePosition));
    }

    /**
     * Converts syntax-node line position into a lsp4j position.
     *
     * @param linePosition - line position
     * @return {@link Position} converted position
     */
    public static Position toPosition(LinePosition linePosition) {
        return new Position(linePosition.line(), linePosition.offset());
    }

    /**
     * Convert a given pair of start and end offsets to LSP Range.
     *
     * @param startOffset starting offset
     * @param endOffset   end offset
     * @param document    text document where the position resides
     * @return {@link Range} calculated range
     */
    public static Range toRange(int startOffset, int endOffset, TextDocument document) {
        LinePosition startPos = document.linePositionFrom(startOffset);
        LinePosition endPos = document.linePositionFrom(endOffset);

        return new Range(PositionUtil.toPosition(startPos), PositionUtil.toPosition(endPos));
    }

    /**
     * Check if the provided position is within the enclosing line range.
     *
     * @param pos       Position to be checked for inclusion
     * @param lineRange Enclosing line range in which the #position reside
     * @return True if the provided position resides within the line range
     */
    public static boolean isWithinLineRange(Position pos, LineRange lineRange) {
        int sLine = lineRange.startLine().line();
        int sCol = lineRange.startLine().offset();
        int eLine = lineRange.endLine().line();
        int eCol = lineRange.endLine().offset();
        return ((sLine == eLine && pos.getLine() == sLine) &&
                (pos.getCharacter() >= sCol && pos.getCharacter() <= eCol)
        ) || ((sLine != eLine) && (pos.getLine() > sLine && pos.getLine() < eLine ||
                pos.getLine() == eLine && pos.getCharacter() <= eCol ||
                pos.getLine() == sLine && pos.getCharacter() >= sCol
        ));
    }

    /**
     * Check if the provided line range is within the enclosing line range.
     *
     * @param lineRange      Line range to be checked for inclusion
     * @param enclosingRange Enclosing line range in which the #lineRange reside
     * @return True if the provided line range resides within the provided enclosing line range
     */
    public static boolean isWithinLineRange(LineRange lineRange, LineRange enclosingRange) {
        Position start = PositionUtil.toPosition(lineRange.startLine());
        Position end = PositionUtil.toPosition(lineRange.endLine());
        return PositionUtil.isWithinLineRange(start, enclosingRange) && PositionUtil.isWithinLineRange(end,
                enclosingRange);
    }

    /**
     * Returns whether the position is within the range.
     *
     * @param pos   position
     * @param range range
     * @return True if within range, False otherwise
     */
    public static boolean isWithinRange(Position pos, Range range) {
        int sLine = range.getStart().getLine();
        int sCol = range.getStart().getCharacter();
        int eLine = range.getEnd().getLine();
        int eCol = range.getEnd().getCharacter();
        return ((sLine == eLine && pos.getLine() == sLine) &&
                (pos.getCharacter() >= sCol && pos.getCharacter() <= eCol)
        ) || ((sLine != eLine) && (pos.getLine() > sLine && pos.getLine() < eLine ||
                pos.getLine() == eLine && pos.getCharacter() <= eCol ||
                pos.getLine() == sLine && pos.getCharacter() >= sCol
        ));
    }

    /**
     * Check if a given range resides inside the given inclusion range.
     *
     * @param range          range to be checked for inclusion
     * @param enclosingRange enclosing range which we check whether #range resides
     * @return True if within range, False otherwise
     */
    public static boolean isRangeWithinRange(Range range, Range enclosingRange) {
        return isWithinRange(range.getStart(), enclosingRange)
                && isWithinRange(range.getEnd(), enclosingRange);
    }

    /**
     * Check if a given offset is with in the range of a given node.
     *
     * @param node   Node
     * @param offset Offset
     * @return True if within range, False otherwise
     */
    public static boolean isWithInRange(Node node, int offset) {
        return node.textRange().startOffset() <= offset && offset <= node.textRange().endOffset();
    }

    /**
     * Find the token at position.
     *
     * @return Token at position
     */
    public static Optional<Token> findTokenAtPosition(DocumentServiceContext context, Position position) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            return Optional.empty();
        }
        TextDocument textDocument = document.get().textDocument();

        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        Token tokenAtPosition = ((ModulePartNode) document.get().syntaxTree().rootNode()).findToken(txtPos, true);
        return Optional.ofNullable(tokenAtPosition);
    }

    /**
     * Returns the position offset(character count) for the given position from the start of the Document.
     *
     * @param position   position
     * @param syntaxTree syntax tree
     * @return Position offset from the beginning of the document
     */
    public static int getPositionOffset(Position position, SyntaxTree syntaxTree) {
        LinePosition linePos = LinePosition.from(position.getLine(), position.getCharacter());
        return syntaxTree.textDocument().textPositionFrom(linePos);
    }

    /**
     * Returns ballerina text edit for a given lsp4j text edit.
     *
     * @param syntaxTree syntax tree
     * @param textEdit   lsp4j text edit
     * @return Ballerina text edit
     */
    public static TextEdit getTextEdit(SyntaxTree syntaxTree, org.eclipse.lsp4j.TextEdit textEdit) {
        TextDocument textDocument = syntaxTree.textDocument();
        Position startPos = textEdit.getRange().getStart();
        Position endPos = textEdit.getRange().getEnd();
        int start = textDocument.textPositionFrom(LinePosition.from(startPos.getLine(), startPos.getCharacter()));
        int end = textDocument.textPositionFrom(LinePosition.from(endPos.getLine(), endPos.getCharacter()));
        return TextEdit.from(TextRange.from(start, end - start), textEdit.getNewText());
    }
}
