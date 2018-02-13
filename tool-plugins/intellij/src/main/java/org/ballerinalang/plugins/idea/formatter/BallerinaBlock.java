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

package org.ballerinalang.plugins.idea.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.plugins.idea.BallerinaTypes.ACTION_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANNOTATION_ATTACHMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANNOTATION_ATTRIBUTE_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ANNOTATION_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CATCH_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CATCH_CLAUSES;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CODE_BLOCK_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.COMMENT_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CONNECTOR_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.CONNECTOR_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ELSE_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ELSE_IF_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ENDPOINT_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.ENUM_FIELD_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.EXPRESSION_LIST;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FAILED_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FINALLY_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FOREACH_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FORK_JOIN_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FUNCTION_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.FUNCTION_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.IF_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.IF_ELSE_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.JOIN_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RECORD_KEY_VALUE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RECORD_LITERAL;
import static org.ballerinalang.plugins.idea.BallerinaTypes.RESOURCE_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SERVICE_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.SERVICE_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.STRUCT_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.STRUCT_DEFINITION;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TIMEOUT_CLAUSE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRANSACTION_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.TRY_CATCH_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.VARIABLE_REFERENCE;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WHILE_STATEMENT;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WORKER_BODY;
import static org.ballerinalang.plugins.idea.BallerinaTypes.WORKER_DECLARATION;

/**
 * Represents a code block.
 */
public class BallerinaBlock extends AbstractBlock {

    private SpacingBuilder spacingBuilder;

    @NotNull
    private final ASTNode node;
    @Nullable
    private final Alignment alignment;
    @Nullable
    private final Indent indent;
    @Nullable
    private final Wrap wrap;
    @NotNull
    private final CodeStyleSettings mySettings;
    @NotNull
    private final SpacingBuilder mySpacingBuilder;
    @Nullable
    private List<Block> mySubBlocks;


    protected BallerinaBlock(@NotNull ASTNode node, @Nullable Alignment alignment, @Nullable Indent indent, @Nullable
            Wrap wrap, @NotNull CodeStyleSettings settings, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);

        this.node = node;
        this.alignment = alignment;
        this.indent = indent;
        this.wrap = wrap;
        this.mySettings = settings;
        this.mySpacingBuilder = spacingBuilder;
        this.spacingBuilder = spacingBuilder;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = node.getFirstChildNode();
        IElementType parentElementType = node.getElementType();

        while (child != null) {
            IElementType childElementType = child.getElementType();
            if (childElementType != TokenType.WHITE_SPACE) {

                Indent indent = Indent.getNoneIndent();

                if (isInsideADefinitionElement(childElementType)) {
                    if (child.getFirstChildNode() != null && child.getLastChildNode() != null) {
                        indent = Indent.getSpaceIndent(4);
                    }
                } else if (childElementType == COMMENT_STATEMENT) {
                    if (isADefinitionElement(parentElementType) || isACodeBlock(parentElementType)) {
                        indent = Indent.getSpaceIndent(4);
                    }
                } else if (childElementType == ANNOTATION_ATTRIBUTE_LIST) {
                    if (parentElementType == ANNOTATION_ATTACHMENT) {
                        indent = Indent.getSpaceIndent(4);
                    }
                } else if (childElementType == WORKER_DECLARATION) {
                    if (parentElementType == FORK_JOIN_STATEMENT) {
                        indent = Indent.getSpaceIndent(4);
                    }
                } else if (childElementType == WORKER_BODY) {
                    if (parentElementType == WORKER_DECLARATION) {
                        indent = Indent.getSpaceIndent(4);
                    }
                } else if (childElementType == RECORD_KEY_VALUE) {
                    if (parentElementType == RECORD_LITERAL) {
                        indent = Indent.getSpaceIndent(4);
                    }
                } else if (childElementType == CODE_BLOCK_BODY || childElementType == ENUM_FIELD_LIST) {
                    indent = Indent.getSpaceIndent(4);
                } else if (childElementType == EXPRESSION_LIST) {
                    if (parentElementType == VARIABLE_REFERENCE) {
                        indent = Indent.getSpaceIndent(4);
                    }
                }

                // If the child node text is empty, the IDEA core will throw an exception.
                if (!child.getText().isEmpty()) {
                    Block block = new BallerinaBlock(
                            child,
                            Alignment.createAlignment(),
                            indent,
                            null,
                            mySettings,
                            spacingBuilder
                    );
                    blocks.add(block);
                }
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    private static boolean isADefinitionElement(@NotNull final IElementType parentElementType) {
        if (parentElementType == FUNCTION_DEFINITION || parentElementType == SERVICE_DEFINITION
                || parentElementType == RESOURCE_DEFINITION || parentElementType == CONNECTOR_DEFINITION
                || parentElementType == ACTION_DEFINITION || parentElementType == STRUCT_DEFINITION) {
            return true;
        }
        return false;
    }

    private static boolean isInsideADefinitionElement(@NotNull final IElementType childElementType) {
        if (childElementType == FUNCTION_BODY || childElementType == CONNECTOR_BODY
                || childElementType == SERVICE_BODY || childElementType == STRUCT_BODY
                || childElementType == ANNOTATION_BODY || childElementType == ENDPOINT_BODY) {
            return true;
        }
        return false;
    }

    private static boolean isACodeBlock(@NotNull final IElementType parentElementType) {
        if (parentElementType == IF_ELSE_STATEMENT || parentElementType == FOREACH_STATEMENT
                || parentElementType == WHILE_STATEMENT || parentElementType == WORKER_DECLARATION
                || parentElementType == FORK_JOIN_STATEMENT || parentElementType == TRANSACTION_STATEMENT
                || parentElementType == IF_CLAUSE || parentElementType == ELSE_IF_CLAUSE
                || parentElementType == ELSE_CLAUSE || parentElementType == TRY_CATCH_STATEMENT
                || parentElementType == CATCH_CLAUSE || parentElementType == CATCH_CLAUSES
                || parentElementType == FINALLY_CLAUSE || parentElementType == JOIN_CLAUSE
                || parentElementType == TIMEOUT_CLAUSE || parentElementType == TRANSACTION_STATEMENT
                || parentElementType == FAILED_CLAUSE) {
            return true;
        }
        return false;
    }

    @Override
    public Indent getIndent() {
        return indent;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return node.getFirstChildNode() == null;
    }
}
