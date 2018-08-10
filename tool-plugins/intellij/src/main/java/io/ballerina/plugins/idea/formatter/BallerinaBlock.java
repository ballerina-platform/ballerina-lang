/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a code block.
 */
public class BallerinaBlock extends AbstractBlock {

    @NotNull
    private final ASTNode myNode;
    @Nullable
    private final Alignment myAlignment;
    @Nullable
    private final Indent myIndent;
    @Nullable
    private final Wrap myWrap;
    @NotNull
    private final CodeStyleSettings mySettings;
    @NotNull
    private final SpacingBuilder mySpacingBuilder;
    @Nullable
    private List<Block> mySubBlocks;
    @NotNull
    private Map<ASTNode, Alignment> myAlignmentMap;

    protected BallerinaBlock
            (@NotNull ASTNode node, @Nullable Alignment alignment, @Nullable Indent indent, @Nullable Wrap wrap,
             @NotNull CodeStyleSettings settings, @NotNull SpacingBuilder spacingBuilder,
             @NotNull Map<ASTNode, Alignment> alignmentMap) {
        super(node, wrap, alignment);

        this.myNode = node;
        this.myAlignment = alignment;
        this.myIndent = indent;
        this.myWrap = wrap;
        this.mySettings = settings;
        this.mySpacingBuilder = spacingBuilder;
        this.myAlignmentMap = alignmentMap;
    }

    @Override
    protected List<Block> buildChildren() {
        return new LinkedList<>();
    }

    @NotNull
    @Override
    public ASTNode getNode() {
        return myNode;
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        return myNode.getTextRange();
    }

    @Nullable
    @Override
    public Wrap getWrap() {
        return myWrap;
    }

    @Override
    public Indent getIndent() {
        return myIndent;
    }

    @Override
    @Nullable
    public Alignment getAlignment() {
        return myAlignment;
    }

    @NotNull
    @Override
    public List<Block> getSubBlocks() {
        if (mySubBlocks == null) {
            mySubBlocks = buildSubBlocks();
        }
        return mySubBlocks;
    }

    @NotNull
    private List<Block> buildSubBlocks() {
        List<Block> blocks = ContainerUtil.newArrayList();
        for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            IElementType childType = child.getElementType();
            if (child.getTextRange().getLength() == 0) {
                continue;
            }
            if (childType == TokenType.WHITE_SPACE) {
                continue;
            }
            Alignment alignment = getAlignment(child);
            Indent indent = calculateIndent(child);
            Wrap wrap = createWrap(child);
            blocks.add(new BallerinaBlock(child, alignment, indent, wrap, mySettings, mySpacingBuilder,
                    myAlignmentMap));
        }
        return blocks;
    }

    private Alignment getAlignment(ASTNode child) {
        Alignment alignment = null;
        IElementType childElementType = child.getElementType();
        IElementType parentElementType = myNode.getElementType();
        if ((childElementType == BallerinaTypes.PARAMETER || childElementType == BallerinaTypes.DEFAULTABLE_PARAMETER
                || childElementType == BallerinaTypes.REST_PARAMETER)
                && parentElementType == BallerinaTypes.FORMAL_PARAMETER_LIST) {
            if (myAlignmentMap.containsKey(myNode)) {
                alignment = myAlignmentMap.get(myNode);
            } else {
                alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
                myAlignmentMap.put(myNode, alignment);
            }
        } else if (childElementType == BallerinaTypes.ENDPOINT_PARAMETER
                && parentElementType == BallerinaTypes.RESOURCE_PARAMETER_LIST) {
            if (myAlignmentMap.containsKey(myNode)) {
                alignment = myAlignmentMap.get(myNode);
            } else {
                alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
                myAlignmentMap.put(myNode, alignment);
            }
        } else if (childElementType == BallerinaTypes.PARAMETER
                && parentElementType == BallerinaTypes.PARAMETER_LIST) {
            ASTNode treeParent = myNode.getTreeParent().getTreeParent();
            if (myAlignmentMap.containsKey(treeParent)) {
                alignment = myAlignmentMap.get(treeParent);
            } else {
                alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
                myAlignmentMap.put(treeParent, alignment);
            }
        } else if ((childElementType == BallerinaTypes.OBJECT_PARAMETER
                || childElementType == BallerinaTypes.OBJECT_DEFAULTABLE_PARAMETER
                || childElementType == BallerinaTypes.REST_PARAMETER)
                && parentElementType == BallerinaTypes.OBJECT_PARAMETER_LIST) {
            ASTNode treeParent = myNode.getTreeParent();
            if (myAlignmentMap.containsKey(treeParent)) {
                alignment = myAlignmentMap.get(treeParent);
            } else {
                alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
                myAlignmentMap.put(treeParent, alignment);
            }
        } else if (childElementType == BallerinaTypes.QUESTION_MARK
                && parentElementType == BallerinaTypes.TERNARY_EXPRESSION) {
            alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
            myAlignmentMap.put(myNode, alignment);
        } else if (childElementType == BallerinaTypes.COLON
                && parentElementType == BallerinaTypes.TERNARY_EXPRESSION) {
            if (myAlignmentMap.containsKey(myNode)) {
                alignment = myAlignmentMap.get(myNode);
            }
        } else if (childElementType == BallerinaTypes.TRANSACTION_PROPERTY_INIT_STATEMENT
                && parentElementType == BallerinaTypes.TRANSACTION_PROPERTY_INIT_STATEMENT_LIST) {
            ASTNode treeParent = myNode.getTreeParent().getTreeParent();
            if (myAlignmentMap.containsKey(treeParent)) {
                alignment = myAlignmentMap.get(treeParent);
            } else {
                alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT);
                myAlignmentMap.put(treeParent, alignment);
            }
        }
        return alignment;
    }

    @NotNull
    private Indent calculateIndent(@NotNull ASTNode child) {
        IElementType childElementType = child.getElementType();
        IElementType parentElementType = myNode.getElementType();
        // Todo - Refactor into separate helper methods
        if (childElementType == BallerinaTypes.BLOCK) {
            return Indent.getNormalIndent();
        } else if (/*parentElementType == BallerinaTypes.RECORD_KEY_VALUE
                ||*/ parentElementType == BallerinaTypes.RECORD_LITERAL_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.LINE_COMMENT
                && (parentElementType == BallerinaTypes.CALLABLE_UNIT_BODY
                || parentElementType == BallerinaTypes.IF_CLAUSE || parentElementType == BallerinaTypes.ELSE_IF_CLAUSE
                || parentElementType == BallerinaTypes.ELSE_CLAUSE
                || parentElementType == BallerinaTypes.WORKER_BODY
                || parentElementType == BallerinaTypes.FORK_JOIN_STATEMENT
                || parentElementType == BallerinaTypes.JOIN_CLAUSE_BODY
                || parentElementType == BallerinaTypes.TIMEOUT_CLAUSE_BODY
                || parentElementType == BallerinaTypes.WHILE_STATEMENT_BODY
                || parentElementType == BallerinaTypes.MATCH_STATEMENT_BODY
                || parentElementType == BallerinaTypes.NAMED_PATTERN
                || parentElementType == BallerinaTypes.UNNAMED_PATTERN
                || parentElementType == BallerinaTypes.RECORD_LITERAL
                || parentElementType == BallerinaTypes.RECORD_FIELD_DEFINITION_LIST
                || parentElementType == BallerinaTypes.FOREACH_STATEMENT
                || parentElementType == BallerinaTypes.LOCK_STATEMENT
                || parentElementType == BallerinaTypes.OBJECT_TYPE_NAME
                || parentElementType == BallerinaTypes.OBJECT_FIELD_DEFINITION
                || parentElementType == BallerinaTypes.TRY_CATCH_STATEMENT
                || parentElementType == BallerinaTypes.CATCH_CLAUSE
                || parentElementType == BallerinaTypes.FINALLY_CLAUSE
                || parentElementType == BallerinaTypes.SERVICE_BODY
                || parentElementType == BallerinaTypes.ARRAY_LITERAL
        )) {
            return Indent.getNormalIndent();
        } else if (parentElementType == BallerinaTypes.CALLABLE_UNIT_SIGNATURE ||
                parentElementType == BallerinaTypes.OBJECT_CALLABLE_UNIT_SIGNATURE) {
            return Indent.getIndent(Indent.Type.NORMAL, true, true);
        } else if (parentElementType == BallerinaTypes.OBJECT_INITIALIZER_PARAMETER_LIST) {
            return Indent.getIndent(Indent.Type.NORMAL, true, true);
        } else if (childElementType == BallerinaTypes.RETURN_PARAMETER) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.RETURN_TYPE) {
            return Indent.getIndent(Indent.Type.NORMAL, true, true);
            //        } else if (childElementType == BallerinaTypes.TUPLE_TYPE_NAME) {
            //            return Indent.getIndent(Indent.Type.NORMAL, true, true);
        } else if ((childElementType == BallerinaTypes.TUPLE_TYPE_NAME
                || childElementType == BallerinaTypes.UNION_TYPE_NAME)
                && (parentElementType == BallerinaTypes.RETURN_TYPE)) {
            return Indent.getIndent(Indent.Type.NORMAL, true, true);
        } else if (parentElementType == BallerinaTypes.MATCH_PATTERN_CLAUSE) {
            return Indent.getNormalIndent();
        } else if (parentElementType == BallerinaTypes.SERVICE_BODY && (childElementType == BallerinaTypes.ENDPOINT_TYPE
                || childElementType == BallerinaTypes.VARIABLE_DEFINITION_STATEMENT
                || childElementType == BallerinaTypes.RESOURCE_DEFINITION)) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.FIELD_DEFINITION ||
                childElementType == BallerinaTypes.RECORD_REST_FIELD_DEFINITION) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.INVOCATION_ARG_LIST) {
            return Indent.getIndent(Indent.Type.NORMAL, true, true);
        } else if (childElementType == BallerinaTypes.FORK_STATEMENT_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.JOIN_CLAUSE_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.TIMEOUT_CLAUSE_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.MATCH_STATEMENT_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.MATCH_EXPRESSION_PATTERN_CLAUSE) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.EXPRESSION_LIST &&
                parentElementType == BallerinaTypes.ARRAY_LITERAL) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.OBJECT_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.FOREVER_STATEMENT_BODY) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.BINARY_ADD_SUB_EXPRESSION
                || childElementType == BallerinaTypes.BINARY_DIV_MUL_MOD_EXPRESSION
                || childElementType == BallerinaTypes.BINARY_AND_EXPRESSION
                || childElementType == BallerinaTypes.BINARY_OR_EXPRESSION
                || childElementType == BallerinaTypes.BITWISE_EXPRESSION
                || childElementType == BallerinaTypes.BITWISE_SHIFT_EXPRESSION
                || childElementType == BallerinaTypes.BINARY_COMPARE_EXPRESSION
                ) {
            if (!(parentElementType == BallerinaTypes.BINARY_ADD_SUB_EXPRESSION
                    || parentElementType == BallerinaTypes.BINARY_DIV_MUL_MOD_EXPRESSION
                    || parentElementType == BallerinaTypes.BINARY_AND_EXPRESSION
                    || parentElementType == BallerinaTypes.BINARY_OR_EXPRESSION
                    || parentElementType == BallerinaTypes.BITWISE_EXPRESSION
                    || parentElementType == BallerinaTypes.BITWISE_SHIFT_EXPRESSION
                    || parentElementType == BallerinaTypes.BINARY_COMPARE_EXPRESSION
                    || parentElementType == BallerinaTypes.UNARY_EXPRESSION
            )) {
                return Indent.getIndent(Indent.Type.NORMAL, true, true);
            }
        } else if (childElementType == BallerinaTypes.VARIABLE_REFERENCE_EXPRESSION &&
                (parentElementType == BallerinaTypes.ASSIGNMENT_STATEMENT ||
                        parentElementType == BallerinaTypes.VARIABLE_DEFINITION_STATEMENT)) {
            return Indent.getNormalIndent();
        } else if ((childElementType == BallerinaTypes.TABLE_COLUMN_DEFINITION
                || childElementType == BallerinaTypes.TABLE_DATA_ARRAY)
                && parentElementType == BallerinaTypes.TABLE_LITERAL) {
            return Indent.getNormalIndent();
        } else if (childElementType == BallerinaTypes.TABLE_DATA_LIST
                && (parentElementType == BallerinaTypes.TABLE_DATA_ARRAY)) {
            return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
    }

    private Wrap createWrap(ASTNode child) {
        return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, false);
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return mySpacingBuilder.getSpacing(this, child1, child2);
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        Indent childIndent = Indent.getNoneIndent();
        if (myNode.getElementType() == BallerinaTypes.CALLABLE_UNIT_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.SERVICE_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.RECORD_LITERAL) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.RECORD_LITERAL_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.WORKER_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.WHILE_STATEMENT_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.FORK_JOIN_STATEMENT) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.JOIN_CLAUSE_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.TIMEOUT_CLAUSE_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.MATCH_STATEMENT_BODY) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.MATCH_EXPRESSION) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.NAMED_PATTERN) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.UNNAMED_PATTERN) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.FOREACH_STATEMENT) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.IF_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.ELSE_IF_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.ELSE_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.TRANSACTION_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.ON_RETRY_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.TRY_CATCH_STATEMENT) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.CATCH_CLAUSES) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.CATCH_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.FINALLY_CLAUSE) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.LOCK_STATEMENT) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.OBJECT_TYPE_NAME) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.OBJECT_FIELD_DEFINITION) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.FOREVER_STATEMENT) {
            childIndent = Indent.getNormalIndent();
        } else if (myNode.getElementType() == BallerinaTypes.RECORD_TYPE_NAME) {
            childIndent = Indent.getNormalIndent();
        }
        return new ChildAttributes(childIndent, null);
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }
}
