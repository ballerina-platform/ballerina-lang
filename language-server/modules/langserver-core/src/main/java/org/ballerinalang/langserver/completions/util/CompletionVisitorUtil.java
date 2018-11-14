/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;


/**
 * Utility methods for Completion Tree Visiting.
 * 
 * @since 0.985.0
 */
public class CompletionVisitorUtil {

    private CompletionVisitorUtil() {
    }

    /**
     * Check whether the cursor is located within the given node's block scope.
     *
     * Note: This method should only be used to check and terminate the visitor when the content within the block
     *       is empty.
     *
     * @param nodePosition      Position of the current node
     * @param symbolEnv         Symbol Environment
     * @param lsContext         Language Server Operation Context
     * @param treeVisitor       Completion tree visitor instance
     * @return {@link Boolean}  Whether the cursor within the block scope
     */
    public static boolean isCursorWithinBlock(DiagnosticPos nodePosition, @Nonnull SymbolEnv symbolEnv,
                                              LSContext lsContext, TreeVisitor treeVisitor) {
        DiagnosticPos zeroBasedPosition = CommonUtil.toZeroBasedPosition(nodePosition);
        int line = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int nodeSLine = zeroBasedPosition.sLine;
        int nodeELine = zeroBasedPosition.eLine;

        if ((nodeSLine <= line && nodeELine >= line)) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = new HashMap<>();
            if (symbolEnv.scope != null) {
                visibleSymbolEntries.putAll(treeVisitor.resolveAllVisibleSymbols(symbolEnv));
            }
            treeVisitor.populateSymbols(visibleSymbolEntries, symbolEnv);
            treeVisitor.forceTerminateVisitor();
            return true;
        }

        return false;
    }

    /**
     * Check whether the cursor resides within the given node type's parameter context.
     * Node name is used to identify the correct node
     *
     * @param nodeName              Name of the node
     * @param nodeType              Node type (Function, Resource, Action or Connector)
     * @param env                   Symbol Environment
     * @param lsContext             Language Server Operation Context
     * @param treeVisitor           Completion tree visitor instance
     * @return {@link Boolean}      Whether the cursor is within the parameter context
     */
    public static boolean isWithinParameterContext(String nodeName, String nodeType, SymbolEnv env,
                                                   LSContext lsContext, TreeVisitor treeVisitor) {
        ParserRuleContext parserRuleContext = lsContext.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        TokenStream tokenStream = lsContext.get(CompletionKeys.TOKEN_STREAM_KEY);
        String terminalToken = "";

        // If the parser rule context is not parameter context or parameter list context, we skipp the calculation
        if (!(parserRuleContext instanceof BallerinaParser.ParameterContext
                || parserRuleContext instanceof BallerinaParser.ParameterListContext)) {
            return false;
        }

        int startTokenIndex = parserRuleContext.getStart().getTokenIndex();
        ArrayList<String> terminalKeywords = new ArrayList<>(
                Arrays.asList(UtilSymbolKeys.ACTION_KEYWORD_KEY, UtilSymbolKeys.CONNECTOR_KEYWORD_KEY,
                        UtilSymbolKeys.FUNCTION_KEYWORD_KEY, UtilSymbolKeys.RESOURCE_KEYWORD_KEY)
        );
        ArrayList<Token> filteredTokens = new ArrayList<>();
        Token openBracket = null;
        boolean isWithinParams = false;

        // Find the index of the closing bracket
        while (true) {
            if (startTokenIndex > tokenStream.size()) {
                // In the ideal case, should not reach this point
                startTokenIndex = -1;
                break;
            }
            Token token = tokenStream.get(startTokenIndex);
            String tokenString = token.getText();
            if (tokenString.equals(")")) {
                break;
            }
            startTokenIndex++;
        }

        // Backtrack the token stream to find a terminal token
        while (true) {
            if (startTokenIndex < 0) {
                break;
            }
            Token token = tokenStream.get(startTokenIndex);
            String tokenString = token.getText();
            if (terminalKeywords.contains(tokenString)) {
                terminalToken = tokenString;
                break;
            }
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                filteredTokens.add(token);
            }
            startTokenIndex--;
        }

        Collections.reverse(filteredTokens);

        /*
        This particular logic identifies a matching pair of closing and opening bracket and then check whether the
        cursor is within those bracket pair
         */
        if (nodeName.equals(filteredTokens.get(0).getText()) && terminalToken.equals(nodeType)) {
            String tokenText;
            for (Token token : filteredTokens) {
                tokenText = token.getText();
                if (tokenText.equals("(")) {
                    openBracket = token;
                } else if (tokenText.equals(")") && openBracket != null) {
                    Position cursorPos = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
                    int openBLine = openBracket.getLine() - 1;
                    int openBCol = openBracket.getCharPositionInLine();
                    int closeBLine = token.getLine() - 1;
                    int closeBCol = token.getCharPositionInLine();
                    int cursorLine = cursorPos.getLine();
                    int cursorCol = cursorPos.getCharacter();

                    isWithinParams =  (cursorLine > openBLine && cursorLine < closeBLine)
                            || (cursorLine == openBLine && cursorCol > openBCol && cursorLine < closeBLine)
                            || (cursorLine > openBLine && cursorCol < closeBCol && cursorLine == closeBLine)
                            || (cursorLine == openBLine && cursorLine == closeBLine && cursorCol >= openBCol
                            && cursorCol <= closeBCol);
                    if (isWithinParams) {
                        break;
                    } else {
                        openBracket = null;
                    }
                }
            }
        }

        if (isWithinParams) {
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(env), env);
            treeVisitor.forceTerminateVisitor();
        }

        return isWithinParams;
    }

    /**
     * Generate a variable Definition.
     *
     * @param var                           BLang Variable
     * @return {@link BLangVariableDef}     Generated BLang Variable Definition
     */
    public static BLangVariableDef createVarDef(BLangVariable var) {
        BLangVariableDef varDefNode = new BLangVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    /**
     * Generate a Block statement from a given set of statements.
     *
     * @param statements                Statements to be populated
     * @return {@link BLangBlockStmt}   Generated block statement  
     */
    public static BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    /**
     * Check whether the cursor is at the resource identifier.
     * 
     * @param bLangResource     Resource to be consider
     * @param context           Language Server Operation Context
     * @param treeVisitor       Completion Tree Visitor instance
     * @return {@link Boolean}  Whether the cursor is at the resource identifier or not
     */
    public static boolean isCursorAtResourceIdentifier(BLangResource bLangResource, LSContext context,
                                                       TreeVisitor treeVisitor) {
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        DiagnosticPos zeroBasedPo = CommonUtil.toZeroBasedPosition(bLangResource.getPosition());
        int line = position.getLine();
        int nodeSLine = zeroBasedPo.sLine;
        boolean status = line == nodeSLine;
        if (status) {
            treeVisitor.forceTerminateVisitor();
        }

        return status;
    }
    
    public static List<BLangNode> getObjectItemsOrdered(BLangObjectTypeNode objectTypeNode) {
        List<BLangNode> nodes = new ArrayList<>();

        nodes.addAll(objectTypeNode.getFields().stream()
                .map(field -> (BLangNode) field)
                .collect(Collectors.toList()));

        nodes.addAll(objectTypeNode.getFunctions().stream()
                .map(function -> (BLangNode) function)
                .collect(Collectors.toList()));
        
        if (objectTypeNode.initFunction != null) {
            nodes.add(objectTypeNode.initFunction);
        }

        nodes.sort(Comparator.comparing(node -> node.getPosition().getStartLine()));

        return nodes;
    }
}
