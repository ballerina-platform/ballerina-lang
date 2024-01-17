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
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to be extended by any parser error handler class.
 *
 * @since 2.0.0
 */
public abstract class AbstractParserErrorHandler {

    protected final AbstractTokenReader tokenReader;
    private ArrayDeque<ParserRuleContext> ctxStack = new ArrayDeque<>();
    private int previousTokenIndex;
    private int itterCount;

    /**
     * Limit for the distance to travel, to determine a successful lookahead.
     */
    protected static final int LOOKAHEAD_LIMIT = 4;

    /**
     * Limit for the number of times parser tries to recover staying on the same token index.
     * This will prevent parser going on infinite loops.
     */
    private static final int RESOLUTION_ITTER_LIMIT = 7;

    /**
     * Limit for the number of times parser tries to insert tokens staying on the same token index in completion mode.
     * This will prevent parser going on infinite loops.
     */
    private static final int COMPLETION_ITTER_LIMIT = 15;

    public AbstractParserErrorHandler(AbstractTokenReader tokenReader) {
        this.tokenReader = tokenReader;
        this.previousTokenIndex = -1;
        this.itterCount = 0;
    }

    /*
     * Abstract methods, to be implemented by the classes that extends this abstract error handler
     */
    protected abstract boolean hasAlternativePaths(ParserRuleContext context);

    protected abstract Result seekMatch(ParserRuleContext context, int lookahead, int currentDepth,
                                        boolean isEntryPoint);

    protected abstract ParserRuleContext getNextRule(ParserRuleContext context, int nextLookahead);

    protected abstract SyntaxKind getExpectedTokenKind(ParserRuleContext context);

    protected abstract Solution getInsertSolution(ParserRuleContext context);

    /*
     * -------------- Error recovering --------------
     */

    /**
     * Recover from current context. Returns the action needs to be taken with respect
     * to the next token, in order to recover. This method will search for the most
     * optimal action, that will result the parser to proceed the farthest distance.
     *
     * @param currentCtx   Current parser context
     * @param nextToken    Next token of the input where the error occurred
     * @param isCompletion Whether in recovery point is a completion
     * @return The action needs to be taken for the next token, in order to recover
     */
    public Solution recover(ParserRuleContext currentCtx, STToken nextToken, boolean isCompletion) {
        // Assumption: always comes here after a peek()

        int currentTokenIndex = this.tokenReader.getCurrentTokenIndex();
        if (currentTokenIndex == this.previousTokenIndex) {
            itterCount++;
        } else {
            itterCount = 0;
            previousTokenIndex = currentTokenIndex;
        }

        Solution fix = null;
        if (isCompletion && itterCount < COMPLETION_ITTER_LIMIT) {
            fix = getCompletion(currentCtx, nextToken);
        } else if (itterCount < RESOLUTION_ITTER_LIMIT) {
            fix = getResolution(currentCtx, nextToken);
        }

        if (fix != null) {
            applyFix(currentCtx, fix);
            return fix;
        }

        // Fail safe. This means we can't find a path to recover.
        assert isCompletion ? itterCount != COMPLETION_ITTER_LIMIT : itterCount != RESOLUTION_ITTER_LIMIT
                : "fail safe reached";
        return getFailSafeSolution(currentCtx, nextToken);
    }

    private Solution getResolution(ParserRuleContext currentCtx, STToken nextToken) {
        Result bestMatch = seekMatch(currentCtx);
        validateSolution(bestMatch, currentCtx, nextToken);

        Solution sol = null;
        if (bestMatch.matches > 0) {
            sol = bestMatch.solution;
        }
        
        return sol;
    }

    private Solution getFailSafeSolution(ParserRuleContext currentCtx, STToken nextToken) {
        Solution sol = new Solution(Action.REMOVE, currentCtx, nextToken.kind, nextToken.toString());
        sol.removedToken = consumeInvalidToken();
        return sol;
    }

    private void validateSolution(Result bestMatch, ParserRuleContext currentCtx, STNode nextToken) {
        Solution sol = bestMatch.solution;
        if (sol == null || sol.action == Action.REMOVE) {
            return;
        }

        // Special case the `KEEP` of `DOCUMENTATION_STRING` since we are skipping them in errorHandler.
        // This will avoid `KEEP` of invalid `DOCUMENTATION_STRING` tokens.
        // It is assumed that recover will not be invoked on a valid `DOCUMENTATION_STRING` token.
        if (sol.action == Action.KEEP && nextToken.kind == SyntaxKind.DOCUMENTATION_STRING) {
            bestMatch.solution = new Solution(Action.REMOVE, currentCtx, SyntaxKind.DOCUMENTATION_STRING,
                    currentCtx.toString());
        }
        
        // If best match is INSERT fix followed by immediate REMOVE fix,
        // then it could be repeated next time coming to the error handler.
        // Therefore in such case, set the solution to immediate REMOVE fix.
        if (sol.action != Action.INSERT || bestMatch.fixesSize() < 2) {
            return;
        }

        Solution firstFix = bestMatch.popFix();
        Solution secondFix = bestMatch.peekFix();
        bestMatch.pushFix(firstFix);

        if (secondFix.action == Action.REMOVE && secondFix.depth == 1) {
            bestMatch.solution = secondFix;
        }
    }

    private Solution getCompletion(ParserRuleContext context, STToken nextToken) {
        ArrayDeque<ParserRuleContext> tempCtxStack = this.ctxStack;
        this.ctxStack = getCtxStackSnapshot();

        Solution sol;
        try {
            sol = getInsertSolution(context);
        } catch (IllegalStateException exception) {
            assert false : "Oh no, something went bad with parser error handler: \n" +
                    "getCompletion caught " + exception;
            sol = getResolution(context, nextToken);
        }

        this.ctxStack = tempCtxStack;
        return sol;
    }
    
    /**
     * Remove the invalid token. This method assumes that the next immediate token
     * of the token input stream is the culprit.
     *
     * @return the invalid token
     */
    public STToken consumeInvalidToken() {
        return this.tokenReader.read();
    }

    /**
     * Apply the fix to the current context.
     *
     * @param currentCtx Current context
     * @param fix        Fix to apply
     */
    private void applyFix(ParserRuleContext currentCtx, Solution fix) {
        if (fix.action == Action.REMOVE) {
            fix.removedToken = consumeInvalidToken();
            fix.recoveredNode = this.tokenReader.peek();
            fix.tokenKind = this.tokenReader.peek().kind;
        } else if (fix.action == Action.INSERT) {
            fix.recoveredNode = handleMissingToken(currentCtx, fix);
        }
    }

    /**
     * Handle a missing token scenario.
     *
     * @param currentCtx Current context
     * @param fix        Solution to recover from the missing token
     */
    private STNode handleMissingToken(ParserRuleContext currentCtx, Solution fix) {
        return SyntaxErrors.createMissingTokenWithDiagnostics(fix.tokenKind, fix.ctx);
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
        ArrayDeque<ParserRuleContext> tempCtxStack = this.ctxStack;
        Result bestMatch;
        try {
            bestMatch = seekMatchInSubTree(currentCtx, 1, 0, true);
        } catch (IllegalStateException exception) {
            // This is a fail-safe mechanism to avoid parser being crashed in the production.
            // We catch the exception and since we don't have any other path, return the solution as a REMOVE.
            // We should never reach here. If we do, please open an issue.
            assert false : "Oh no, something went bad with parser error handler: \n" +
                    "seekMatch caught " + exception;
            bestMatch = new Result(new ArrayDeque<>(), LOOKAHEAD_LIMIT - 1);
            bestMatch.solution = new Solution(Action.REMOVE, currentCtx, SyntaxKind.NONE, currentCtx.toString());
        } finally {
            this.ctxStack = tempCtxStack;
        }

        return bestMatch;
    }

    /**
     * Search for a solution in a sub-tree/sub-path. This will take a snapshot of the current context stack
     * and will operate on top of it, so that the original state of the parser will not be disturbed. On return
     * the previous state of the parser contexts will be restored.
     *
     * @param currentCtx   Current context
     * @param lookahead    Position of the next token to consider, from the position of the original error.
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

    /**
     * Pushes a context to the context stack.
     *
     * @param context context to push
     */
    public void startContext(ParserRuleContext context) {
        this.ctxStack.push(context);
    }

    /**
     * Removes the head of the context stack.
     */
    public void endContext() {
        this.ctxStack.pop();
    }

    /**
     * Replaces the head of the context stack.
     *
     * @param context context to replace
     */
    public void switchContext(ParserRuleContext context) {
        this.ctxStack.pop();
        this.ctxStack.push(context);
    }

    /**
     * Returns the head of the context stack.
     *
     * @return head of the stack
     */
    protected ParserRuleContext getParentContext() {
        return this.ctxStack.peek();
    }

    /**
     * Returns the second element of the context stack.
     *
     * @return second element of the stack
     */
    protected ParserRuleContext getGrandParentContext() {
        ParserRuleContext parent = this.ctxStack.pop();
        ParserRuleContext grandParent = this.ctxStack.peek();
        this.ctxStack.push(parent);
        return grandParent;
    }

    /**
     * Returns <code>true</code> if the context stack contains a particular context.
     *
     * @param context context to be checked for containment in the stack
     * @return <code>true</code> if the stack contains the specified context
     */
    protected boolean hasAncestorContext(ParserRuleContext context) {
        return this.ctxStack.contains(context);
    }

    /**
     * Returns the context stack.
     *
     * @return context stack
     */
    protected ArrayDeque<ParserRuleContext> getContextStack() {
        return this.ctxStack;
    }

    /**
     * Search for matching token sequences within the given alternative paths, and find the most optimal solution.
     *
     * @param lookahead        Position of the next token to consider, relative to the position of the original error
     * @param currentDepth     Amount of distance traveled so far
     * @param currentMatches   Matching tokens found so far
     * @param alternativeRules Alternative rules
     * @param isEntryPoint     Flag indicating whether this is the entry point to the error recovery
     * @return Recovery result
     */
    protected Result seekInAlternativesPaths(int lookahead, int currentDepth, int currentMatches,
                                             ParserRuleContext[] alternativeRules, boolean isEntryPoint) {
        @SuppressWarnings("unchecked")
        List<Result>[] results = new List[LOOKAHEAD_LIMIT];
        int bestMatchIndex = 0;

        // Visit all the alternative rules and get their results. Arrange them in way
        // such that results with the same number of matches are put together. This is
        // done so that we can easily pick the best, without iterating through them.
        for (ParserRuleContext rule : alternativeRules) {
            ArrayDeque<ParserRuleContext> tempCtxStack = this.ctxStack;
            Result result;
            try {
                result = seekMatchInSubTree(rule, lookahead, currentDepth, isEntryPoint);
            } catch (IllegalStateException exception) {
                // This is a fail-safe mechanism to avoid parser being crashed in the production.
                // We Catch the exception and simply ignore that path.
                // The best alternative path would get picked from the remaining contenders.
                // We should never reach here. If we do, please open an issue.
                assert false : "Oh no, something went bad with parser error handler: \n" +
                        "seekInAlternativesPaths caught " + exception;
                continue;
            } finally {
                this.ctxStack = tempCtxStack;
            }

            // exit early
            if (hasFoundBestAlternative(result)) {
                return getFinalResult(currentMatches, result);
            }

            List<Result> similarResutls = results[result.matches];
            if (similarResutls == null) {
                similarResutls = new ArrayList<>(LOOKAHEAD_LIMIT);
                results[result.matches] = similarResutls;
                if (bestMatchIndex < result.matches) {
                    bestMatchIndex = result.matches;
                }
            }
            similarResutls.add(result);
        }

        // If there is only one 'best' match, then return it. If there are more than one
        // 'best' match, then we need to do a tie-break. For that, pick the path with the
        // lowest number of removeFixes. If it again results in more than one match, then return
        // the based on the precedence (order of occurrence).
        List<Result> bestMatches = results[bestMatchIndex];
        Result bestMatch = bestMatches.get(0);
        Result currentMatch;
        for (int i = 1; i < bestMatches.size(); i++) {
            currentMatch = bestMatches.get(i);

            // If a tie is found, give priority to the one that 'insert'.
            // If that is also a tie, then give priority to the order.
            int currentMatchRemoveFixes = currentMatch.removeFixes;
            int bestMatchRemoveFixes = bestMatch.removeFixes;
            if (bestMatchRemoveFixes == 0) {
                break;
            }

            if (currentMatchRemoveFixes == bestMatchRemoveFixes) {
                Solution currentSol = bestMatch.peekFix();
                Solution foundSol = currentMatch.peekFix();
                if (currentSol.action == Action.REMOVE && foundSol.action == Action.INSERT) {
                    bestMatch = currentMatch;
                }
            } else if (currentMatchRemoveFixes < bestMatchRemoveFixes) {
                bestMatch = currentMatch;
            }
        }

        return getFinalResult(currentMatches, bestMatch);
    }

    private boolean hasFoundBestAlternative(Result result) {
        // If the best possible solution is found we can exit early. However, if that solution
        // is an REMOVE action, then we should not terminate, because there can be another
        // alternative that could give an equally good solution with an INSERT action. Since
        // INSERT action is given high priority, we should continue to search.
        if (result.matches < LOOKAHEAD_LIMIT - 1) {
            return false;
        }

        if (result.solution == null) {
            return true;
        }

        return result.solution.action != Action.REMOVE;
    }

    /**
     * Combine a given result with the current results, and get the final result.
     *
     * @param currentMatches Matches found so far
     * @param bestMatch      Result found in the sub-tree, that requires to be merged with the current results
     * @return Final result
     */
    protected Result getFinalResult(int currentMatches, Result bestMatch) {
        bestMatch.matches += currentMatches;
        return bestMatch;
    }

    /**
     * Fix the current error and continue. Returns the best path after fixing.
     *
     * @param currentCtx         Current parser context
     * @param lookahead          Position of the next token to consider, relative to the position of the original error
     * @param currentDepth       Amount of distance traveled so far
     * @param matchingRulesCount Matches found so far
     * @param isEntryPoint       Flag indicating whether this is an entry-point or not.
     * @return Recovery result
     */
    protected Result fixAndContinue(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                    int matchingRulesCount, boolean isEntryPoint) {
        Result fixedPathResult = fixAndContinue(currentCtx, lookahead, currentDepth);
        // Do not consider the current rule as match, since we had to fix it.
        // i.e: do not increment the match count by 1;

        if (isEntryPoint) {
            fixedPathResult.solution = fixedPathResult.peekFix();
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
     * <p>
     * Then decides the best action to perform (whether to insert or remove a token), using the result
     * of the above two steps, based on the following criteria:
     * <ol>
     * <li>
     * Pick the solution with the longest matching sequence.
     * </li>
     * <li>
     * If there's a tie, number of fixes will also be the same.
     * Hence, check for the solution which requires the lowest number of 'removeFixes'.
     * </li>
     * <li>
     * If there's a tie, then give priority for the 'insertion' as that doesn't require removing
     * an input a user has given.
     * </li>
     * </ol>
     *
     * @param currentCtx   Current parser context
     * @param lookahead    Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @return Recovery result
     */
    protected Result fixAndContinue(ParserRuleContext currentCtx, int lookahead, int currentDepth) {
        // NOTE: Below order is important. We have to visit the current context first, before
        // getting and visiting the nextContext. Because getting the next context is a stateful
        // operation, as it could update (push/pop) the current context stack.

        // Remove current token. That means continue with the NEXT token, with the CURRENT context
        Result deletionResult = seekMatchInSubTree(currentCtx, lookahead + 1, currentDepth + 1, false);

        // Insert the missing token. That means continue the CURRENT token, with the NEXT context.
        // At this point 'lookahead' refers to the next token position, since there is a missing
        // token at the current position. Hence we don't need to increment the 'lookahead' when
        // calling 'getNextRule'.
        ParserRuleContext nextCtx = getNextRule(currentCtx, lookahead);
        Result insertionResult = seekMatchInSubTree(nextCtx, lookahead, currentDepth + 1, false);

        Result fixedPathResult;
        Solution action;
        if (insertionResult.matches == 0 && deletionResult.matches == 0) {
            action = new Solution(Action.INSERT, currentCtx, getExpectedTokenKind(currentCtx),
                    currentCtx.toString(), currentDepth);
            insertionResult.pushFix(action);
            fixedPathResult = insertionResult;
        } else if (insertionResult.matches == deletionResult.matches) {
            if (insertionResult.removeFixes <= deletionResult.removeFixes + 1) {
                action = new Solution(Action.INSERT, currentCtx, getExpectedTokenKind(currentCtx),
                        currentCtx.toString(), currentDepth);
                insertionResult.pushFix(action);
                fixedPathResult = insertionResult;
            } else {
                STToken token = this.tokenReader.peek(lookahead);
                action = new Solution(Action.REMOVE, currentCtx, token.kind, token.toString(), currentDepth);
                deletionResult.pushFix(action);
                fixedPathResult = deletionResult;
            }
        } else if (insertionResult.matches > deletionResult.matches) {
            action = new Solution(Action.INSERT, currentCtx, getExpectedTokenKind(currentCtx), currentCtx.toString(),
                    currentDepth);
            insertionResult.pushFix(action);
            fixedPathResult = insertionResult;
        } else {
            STToken token = this.tokenReader.peek(lookahead);
            action = new Solution(Action.REMOVE, currentCtx, token.kind, token.toString(), currentDepth);
            deletionResult.pushFix(action);
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
        public STToken removedToken;
        public int depth;

        public Solution(Action action, ParserRuleContext ctx, SyntaxKind tokenKind, String tokenText) {
            this(action, ctx, tokenKind, tokenText, -1);
        }

        public Solution(Action action, ParserRuleContext ctx, SyntaxKind tokenKind, String tokenText, int depth) {
            this.action = action;
            this.ctx = ctx;
            this.tokenText = tokenText;
            this.tokenKind = tokenKind;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return action.toString() + "'" + tokenText + "'";
        }
    }

    /**
     * Represents the actions that can be taken to recover from a parser error.
     *
     * @since 1.2.0
     */
    protected enum Action {
        INSERT, REMOVE, KEEP
    }
}
