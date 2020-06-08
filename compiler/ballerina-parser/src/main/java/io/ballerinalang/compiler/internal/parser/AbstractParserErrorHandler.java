/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.diagnostics.DiagnosticCode;
import io.ballerinalang.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class to be extended by any parser error handler class.
 *
 * @since 2.0.0
 */
public abstract class AbstractParserErrorHandler {

    protected final AbstractTokenReader tokenReader;
    protected final BallerinaParserErrorListener errorListener;
    private ArrayDeque<ParserRuleContext> ctxStack = new ArrayDeque<>();

    /**
     * Limit for the distance to travel, to determine a successful lookahead.
     */
    protected int lookaheadLimit = 5;

    public AbstractParserErrorHandler(AbstractTokenReader tokenReader) {
        this.tokenReader = tokenReader;
        this.errorListener = new BallerinaParserErrorListener();
    }

    /*
     * Abstract methods, to be implemented by the classes that extends this abstract error handler
     */
    protected abstract boolean isProductionWithAlternatives(ParserRuleContext context);

    protected abstract Result seekMatch(ParserRuleContext context, int lookahead, int currentDepth,
                                        boolean isEntryPoint);

    protected abstract ParserRuleContext getNextRule(ParserRuleContext context, int nextLookahead);

    protected abstract SyntaxKind getExpectedTokenKind(ParserRuleContext context);

    /*
     * -------------- Error recovering --------------
     */

    /**
     * Recover from current context. Returns the action needs to be taken with respect
     * to the next token, in order to recover. This method will search for the most
     * optimal action, that will result the parser to proceed the farthest distance.
     *
     * @param nextToken Next token of the input where the error occurred
     * @param currentCtx Current parser context
     * @param args Arguments that requires to continue parsing from the given parser context
     * @return The action needs to be taken for the next token, in order to recover
     */
    public Solution recover(ParserRuleContext currentCtx, STToken nextToken, Object... args) {
        // Assumption: always comes here after a peek()

        if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
            SyntaxKind expectedTokenKind = getExpectedTokenKind(currentCtx);
            Solution fix = new Solution(Action.INSERT, currentCtx, expectedTokenKind, currentCtx.toString());
            applyFix(currentCtx, fix, args);
            return fix;
        }

        Result bestMatch = seekMatch(currentCtx);
        if (bestMatch.matches > 0) {
            Solution sol = bestMatch.solution;
            if (sol != null) {
                applyFix(currentCtx, sol, args);
                return sol;
            }

            // else fall through
        }

        // Fail safe. This means we can't find a path to recover.
        removeInvalidToken();
        Solution sol = new Solution(Action.REMOVE, currentCtx, nextToken.kind, nextToken.toString());
        return sol;
    }

    /**
     * Remove the invalid token. This method assumes that the next immediate token
     * of the token input stream is the culprit.
     */
    public void removeInvalidToken() {
        STToken invalidToken = this.tokenReader.read();
        // This means no match is found for the current token.
        // Then consume it and return an error node
        this.errorListener.reportInvalidToken(invalidToken);

        // TODO: add this error node to the tree
    }

    /**
     * Apply the fix to the current context.
     *
     * @param currentCtx Current context
     * @param fix Fix to apply
     * @param args Arguments that requires to continue parsing from the given parser context
     */
    private void applyFix(ParserRuleContext currentCtx, Solution fix, Object... args) {
        if (fix.action == Action.REMOVE) {
            removeInvalidToken();
        } else {
            fix.recoveredNode = handleMissingToken(currentCtx, fix);
        }
    }

    /**
     * Handle a missing token scenario.
     *
     * @param currentCtx Current context
     * @param fix Solution to recover from the missing token
     */
    private STNode handleMissingToken(ParserRuleContext currentCtx, Solution fix) {
        return createMissingTokenWithDiagnostics(fix.tokenKind);
    }

    /**
     * Get a snapshot of the current context stack.
     *
     * @return Snapshot of the current context stack
     */
    private ArrayDeque<ParserRuleContext> getCtxStackSnapshot() {
        // Using ArraDeque#clone() here since it has better performance, than manually
        // creating a clone. ArraDeque#clone() method internally copies the value array
        // and avoids all the checks that is there when calling add()/addAll() methods.
        // Therefore has better performance.
        return this.ctxStack.clone();
    }

    /*
     * seekMatch methods
     */

    /**
     * Start a fresh search for a way to recover with the next immediate token (peek(1), and the current context).
     *
     * @param currentCtx Current parser context
     * @return Recovery result
     */
    private Result seekMatch(ParserRuleContext currentCtx) {
        return seekMatchInSubTree(currentCtx, 1, 0, true);
    }

    /**
     * Search for a solution in a sub-tree/sub-path. This will take a snapshot of the current context stack
     * and will operate on top of it, so that the original state of the parser will not be disturbed. On return
     * the previous state of the parser contexts will be restored.
     *
     * @param currentCtx Current context
     * @param lookahead Position of the next token to consider, from the position of the original error.
     * @param currentDepth Amount of distance traveled so far.
     * @param isEntryPoint Flag indicating whether this is the entry point to the error recovery
     * @return Recovery result
     */
    protected Result seekMatchInSubTree(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                        boolean isEntryPoint) {
        ArrayDeque<ParserRuleContext> tempCtxStack = this.ctxStack;
        this.ctxStack = getCtxStackSnapshot();
        Result result = seekMatch(currentCtx, lookahead, currentDepth, isEntryPoint);
        this.ctxStack = tempCtxStack;
        return result;
    }

    public void startContext(ParserRuleContext context) {
        this.ctxStack.push(context);
    }

    public void endContext() {
        this.ctxStack.pop();
    }

    public void switchContext(ParserRuleContext context) {
        this.ctxStack.pop();
        this.ctxStack.push(context);
    }

    public void reportInvalidNode(STToken startingToken, String message) {
        this.errorListener.reportInvalidNodeError(startingToken, message);
    }

    public void reportMissingTokenError(String diagnosticCode) {
        // TODO Following way of getting the token is suboptimal
        // TODO Try this code and see; function (int s) return error? {}
        STToken currentToken = this.tokenReader.head();
        this.errorListener.reportMissingTokenError(currentToken, diagnosticCode);
    }

    public STNode addDiagnostics(STNode node, DiagnosticCode... diagnosticCodes) {
        Collection<STNodeDiagnostic> diagnosticsToAdd = new ArrayList<>();
        for (DiagnosticCode diagnosticCode : diagnosticCodes) {
            diagnosticsToAdd.add(new STNodeDiagnostic(diagnosticCode));
        }
        return addDiagnostics(node, diagnosticsToAdd);
    }

    private STNode addDiagnostics(STNode node, Collection<STNodeDiagnostic> diagnosticsToAdd) {
        if (diagnosticsToAdd.isEmpty()) {
            return node;
        }

        Collection<STNodeDiagnostic> newDiagnostics;
        Collection<STNodeDiagnostic> oldDiagnostics = node.diagnostics();
        if (oldDiagnostics.isEmpty()) {
            newDiagnostics = new ArrayList<>(diagnosticsToAdd);
        } else {
            // Merge all diagnostics
            newDiagnostics = new ArrayList<>(oldDiagnostics);
            newDiagnostics.addAll(diagnosticsToAdd);
        }
        return node.modifyWith(newDiagnostics);
    }

    public STToken createMissingToken(SyntaxKind expectedKind) {
        return STNodeFactory.createMissingToken(expectedKind);
    }

    public STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind) {
        return createMissingTokenWithDiagnostics(expectedKind, getErrorCode(expectedKind));
    }

    public STToken createMissingTokenWithDiagnostics(SyntaxKind expectedKind, DiagnosticCode diagnosticCode) {
        STNodeDiagnostic diagnostic = new STNodeDiagnostic(diagnosticCode);
        List<STNodeDiagnostic> diagnosticList = new ArrayList<>();
        diagnosticList.add(diagnostic);
        return STNodeFactory.createMissingToken(expectedKind, diagnosticList);
    }

    private DiagnosticCode getErrorCode(SyntaxKind expectedKind) {
        switch (expectedKind) {
            case SEMICOLON_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN;
            case COLON_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_COLON_TOKEN;
            case OPEN_PAREN_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_PAREN_TOKEN;
            case CLOSE_PAREN_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_PAREN_TOKEN;
            case OPEN_BRACE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_TOKEN;
            case CLOSE_BRACE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_TOKEN;
            case OPEN_BRACKET_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACKET_TOKEN;
            case CLOSE_BRACKET_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACKET_TOKEN;
            case EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_EQUAL_TOKEN;
            case COMMA_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_COMMA_TOKEN;
            case PLUS_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_PLUS_TOKEN;
            case SLASH_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_SLASH_TOKEN;
            case AT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_AT_TOKEN;
            case QUESTION_MARK_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_QUESTION_MARK_TOKEN;
            case GT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_GT_TOKEN;
            case GT_EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_GT_EQUAL_TOKEN;
            case LT_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_LT_TOKEN;
            case LT_EQUAL_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_LT_EQUAL_TOKEN;
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_RIGHT_DOUBLE_ARROW_TOKEN;
            case XML_COMMENT_END_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_XML_COMMENT_END_TOKEN;
            case XML_PI_END_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_XML_PI_END_TOKEN;
            case DOUBLE_QUOTE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_DOUBLE_QUOTE_TOKEN;
            case BACKTICK_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_BACKTICK_TOKEN;
            case OPEN_BRACE_PIPE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_OPEN_BRACE_PIPE_TOKEN;
            case CLOSE_BRACE_PIPE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_CLOSE_BRACE_PIPE_TOKEN;
            case ASTERISK_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_ASTERISK_TOKEN;
            case PIPE_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_ASTERISK_TOKEN;

            case DEFAULT_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_DEFAULT_KEYWORD;
            case TYPE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_TYPE_KEYWORD;
            case ON_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_ON_KEYWORD;
            case ANNOTATION_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_KEYWORD;
            case FUNCTION_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_FUNCTION_KEYWORD;
            case SOURCE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_SOURCE_KEYWORD;
            case ENUM_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_ENUM_KEYWORD;
            case FIELD_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_FIELD_KEYWORD;
            case VERSION_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_VERSION_KEYWORD;
            case OBJECT_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_OBJECT_KEYWORD;
            case RECORD_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_RECORD_KEYWORD;
            case SERVICE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_SERVICE_KEYWORD;
            case AS_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_AS_KEYWORD;
            case LET_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_LET_KEYWORD;
            case TABLE_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_TABLE_KEYWORD;
            case KEY_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_KEY_KEYWORD;
            case FROM_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_FROM_KEYWORD;
            case IN_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_IN_KEYWORD;
            case IF_KEYWORD:
                return DiagnosticErrorCode.ERROR_MISSING_IF_KEYWORD;

            case IDENTIFIER_TOKEN:
                return DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER;
            case DECIMAL_INTEGER_LITERAL:
                return DiagnosticErrorCode.ERROR_MISSING_DECIMAL_INTEGER_LITERAL;
            case TYPE_DESC:
                return DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC;
            default:
                throw new UnsupportedOperationException("Unsupported SyntaxKind: " + expectedKind);
        }
    }

    protected ParserRuleContext getParentContext() {
        return this.ctxStack.peek();
    }

    protected ParserRuleContext getGrandParentContext() {
        ParserRuleContext parent = this.ctxStack.pop();
        ParserRuleContext grandParent = this.ctxStack.peek();
        this.ctxStack.push(parent);
        return grandParent;
    }

    /**
     * Search for matching token sequences within the given alternative paths, and find the most optimal solution.
     *
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param alternativeRules Alternative rules
     * @param isEntryPoint Flag indicating whether this is the entry point to the error recovery
     * @return Recovery result
     */
    protected Result seekInAlternativesPaths(int lookahead, int currentDepth, int currentMatches,
                                             ParserRuleContext[] alternativeRules, boolean isEntryPoint) {
        @SuppressWarnings("unchecked")
        List<Result>[] results = new List[lookaheadLimit];
        int bestMatchIndex = 0;

        // Visit all the alternative rules and get their results. Arrange them in way
        // such that results with the same number of matches are put together. This is
        // done so that we can easily pick the best, without iterating through them.
        for (ParserRuleContext rule : alternativeRules) {
            Result result = seekMatchInSubTree(rule, lookahead, currentDepth, isEntryPoint);
            List<Result> similarResutls = results[result.matches];
            if (similarResutls == null) {
                similarResutls = new ArrayList<>(lookaheadLimit);
                results[result.matches] = similarResutls;
                if (bestMatchIndex < result.matches) {
                    bestMatchIndex = result.matches;
                }
            }
            similarResutls.add(result);
        }

        // This means there are no matches for any of the statements
        if (bestMatchIndex == 0) {
            return new Result(new ArrayDeque<>(), currentMatches);
        }

        // If there is only one 'best' match, then return it. If there are more than one
        // 'best' match, then we need to do a tie-break. For that, pick the path with the
        // lowest number of fixes. If it again results in more than one match, then return
        // the based on the precedence (order of occurrence).

        List<Result> bestMatches = results[bestMatchIndex];
        Result bestMatch = bestMatches.get(0);
        Result currentMatch;
        for (int i = 1; i < bestMatches.size(); i++) {
            currentMatch = bestMatches.get(i);

            // If a tie is found, give priority to the one that 'insert'.
            // If that is also a tie, then give priority to the order.
            int currentMatchFixesSize = currentMatch.fixes.size();
            int bestmatchFixesSize = bestMatch.fixes.size();
            if (currentMatchFixesSize == bestmatchFixesSize) {
                // If both are zero continue;
                if (bestmatchFixesSize == 0) {
                    continue;
                }
                Solution currentSol = bestMatch.fixes.peek();
                Solution foundSol = currentMatch.fixes.peek();
                if (currentSol.action == Action.REMOVE && foundSol.action == Action.INSERT) {
                    bestMatch = currentMatch;
                }
            }

            if (currentMatchFixesSize < bestmatchFixesSize) {
                bestMatch = currentMatch;
            }
        }

        return getFinalResult(currentMatches, bestMatch);
    }

    /**
     * Combine a given result with the current results, and get the final result.
     *
     * @param currentMatches Matches found so far
     * @param bestMatch Result found in the sub-tree, that requires to be merged with the current results
     * @return Final result
     */
    protected Result getFinalResult(int currentMatches, Result bestMatch) {
        bestMatch.matches += currentMatches;
        return bestMatch;
    }

    /**
     * Fix the current error and continue. Returns the best path after fixing.
     *
     * @param currentCtx Current parser context
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param matchingRulesCount Matches found so far
     * @param isEntryPoint Flag indicating whether this is an entry-point or not.
     * @return Recovery result
     */
    protected Result fixAndContinue(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                    int matchingRulesCount, boolean isEntryPoint) {
        Result fixedPathResult = fixAndContinue(currentCtx, lookahead, currentDepth + 1);
        // Do not consider the current rule as match, since we had to fix it.
        // i.e: do not increment the match count by 1;

        if (isEntryPoint) {
            fixedPathResult.solution = fixedPathResult.fixes.peek();
        } else {
            fixedPathResult.solution =
                    new Solution(Action.KEEP, currentCtx, getExpectedTokenKind(currentCtx), currentCtx.toString());
        }
        return getFinalResult(matchingRulesCount, fixedPathResult);
    }

    /**
     * <p>
     * Fix the error at the current position and continue forward to find the best path. This method
     * tries to fix the parser error using following steps:
     * <ol>
     * <li>
     * Insert a token and see how far the parser can proceed.
     * </li>
     * <li>
     * Delete a token and see how far the parser can proceed.
     * </li>
     * </ol>
     *
     * Then decides the best action to perform (whether to insert or remove a token), using the result
     * of the above two steps, based on the following criteria:
     * <ol>
     * <li>
     * Pick the solution with the longest matching sequence.
     * </li>
     * <li>
     * If there's a tie, then check for the solution which requires the lowest number of 'fixes'.
     * </li>
     * <li>
     * If there's a tie, then give priority for the 'insertion' as that doesn't require removing
     * an input a user has given.
     * </li>
     * </ol>
     *
     * @param currentCtx Current parser context
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @return Recovery result
     */
    protected Result fixAndContinue(ParserRuleContext currentCtx, int lookahead, int currentDepth) {
        // NOTE: Below order is important. We have to visit the current context first, before
        // getting and visiting the nextContext. Because getting the next context is a stateful
        // operation, as it could update (push/pop) the current context stack.

        // Remove current token. That means continue with the NEXT token, with the CURRENT context
        Result deletionResult = seekMatchInSubTree(currentCtx, lookahead + 1, currentDepth, false);

        // Insert the missing token. That means continue the CURRENT token, with the NEXT context.
        // At this point 'lookahead' refers to the next token position, since there is a missing
        // token at the current position. Hence we don't need to increment the 'lookahead' when
        // calling 'getNextRule'.
        ParserRuleContext nextCtx = getNextRule(currentCtx, lookahead);
        Result insertionResult = seekMatchInSubTree(nextCtx, lookahead, currentDepth, false);

        Result fixedPathResult;
        Solution action;
        if (insertionResult.matches == 0 && deletionResult.matches == 0) {
            fixedPathResult = insertionResult;
        } else if (insertionResult.matches == deletionResult.matches) {
            if (insertionResult.fixes.size() <= deletionResult.fixes.size()) {
                action = new Solution(Action.INSERT, currentCtx, getExpectedTokenKind(currentCtx),
                        currentCtx.toString());
                insertionResult.fixes.push(action);
                fixedPathResult = insertionResult;
            } else {
                STToken token = this.tokenReader.peek(lookahead);
                action = new Solution(Action.REMOVE, currentCtx, token.kind, token.toString());
                deletionResult.fixes.push(action);
                fixedPathResult = deletionResult;
            }
        } else if (insertionResult.matches > deletionResult.matches) {
            action = new Solution(Action.INSERT, currentCtx, getExpectedTokenKind(currentCtx), currentCtx.toString());
            insertionResult.fixes.push(action);
            fixedPathResult = insertionResult;
        } else {
            STToken token = this.tokenReader.peek(lookahead);
            action = new Solution(Action.REMOVE, currentCtx, token.kind, token.toString());
            deletionResult.fixes.push(action);
            fixedPathResult = deletionResult;
        }
        return fixedPathResult;
    }

    /**
     * Represents a solution/fix for a parser error. A {@link Solution} consists of the parser context where the error
     * was encountered, the enclosing parser context at the same point, the token with the error, and the {@link Action}
     * required to recover from the error.
     *
     * @since 1.2.0
     */
    public static class Solution {

        public ParserRuleContext ctx;
        public Action action;
        public String tokenText;
        public SyntaxKind tokenKind;
        public STNode recoveredNode;

        public Solution(Action action, ParserRuleContext ctx, SyntaxKind tokenKind, String tokenText) {
            this.action = action;
            this.ctx = ctx;
            this.tokenText = tokenText;
            this.tokenKind = tokenKind;
        }

        @Override
        public String toString() {
            return action.toString() + "'" + tokenText + "'";
        }
    }

    /**
     * Represent a result of a token-sequence-search in a sub-tree. The result will contain the fixes required to
     * traverse in that sub-tree, and the number of matching tokens it found, without the fixed tokens.
     */
    public static class Result {

        /**
         * Number of tokens with successful matches.
         */
        protected int matches;

        /**
         * List of solutions to recover from the error.
         */
        protected ArrayDeque<Solution> fixes;

        /**
         * Represent the end solution to be applied to the next immediate token, to recover from the error.
         * If the solution is to insert/remove next immediate token, then this is equivalent to the
         * <code>fixes.peek()</code>. Else, if the solution is to insert/remove a token that is not the
         * immediate next token, then this will have a solution with {@link Action#KEEP} as the action.
         */
        protected Solution solution;

        public Result(ArrayDeque<Solution> fixes, int matches) {
            this.fixes = fixes;
            this.matches = matches;
        }
    }

    /**
     * Represents the actions that can be taken to recover from a parser error.
     *
     * @since 1.2.0
     */
    enum Action {
        INSERT, REMOVE, KEEP;
    }
}
