/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.ballerinalang.plugins.idea;

import com.intellij.core.CoreASTFactory;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.PsiCoreCommentImpl;
import com.intellij.psi.tree.IElementType;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
import org.ballerinalang.plugins.idea.psi.FloatingPointLiteral;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.QuotedLiteralString;
import org.jetbrains.annotations.NotNull;

/**
 * Ballerina AST factory which is responsible for creating leaf elements in the PSI tree.
 */
public class BallerinaASTFactory extends CoreASTFactory {
    /**
     * Create an internal parse tree node. FileElement for root or a parse tree CompositeElement (not
     * PSI) for the token.
     * The FileElement is a parse tree node, which is converted to a PsiFile
     * by {@link ParserDefinition#createFile}.
     */
    @NotNull
    @Override
    public CompositeElement createComposite(IElementType type) {
        return super.createComposite(type);
    }

    /**
     * Create a parse tree (AST) leaf node from a token. Doubles as a PSI leaf node.
     * Does not see whitespace tokens.  Default impl makes {@link LeafPsiElement}
     * or {@link PsiCoreCommentImpl} depending on {@link ParserDefinition#getCommentTokens()}.
     */
    @NotNull
    @Override
    public LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        if (type == BallerinaTypes.QUOTED_STRING) {
            return new QuotedLiteralString(type, text);
        }
        if (type == BallerinaTypes.FLOATING_POINT) {
            return new FloatingPointLiteral(type, text);
        }
        if (type instanceof TokenIElementType &&
                ((TokenIElementType) type).getANTLRTokenType() == BallerinaLexer.Identifier) {
            // found an ID node; here we do not distinguish between definitions and references
            // because we have no context information here. All we know is that
            // we have an identifier node that will be connected somewhere in a tree.
            //
            // You can only rename, find usages, etc... on leaves implementing PsiNamedElement
            //
            // TODO: try not to create one for IDs under def subtree roots like vardef, function
            return new IdentifierPSINode(type, text);
        }
        return super.createLeaf(type, text);
    }
}
