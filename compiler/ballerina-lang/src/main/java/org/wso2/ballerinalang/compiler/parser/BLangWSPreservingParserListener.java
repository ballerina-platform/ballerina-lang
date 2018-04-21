/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Implements the getWS to actually return the whitespaces (default implementation returns null).
 * 1) Identify the source ranges of each parser context, such that parent's range doesn't overlap the child context.
 *    (Note that, siblings' ranges still may overlap, therefor we have to de-duplicate in step 3)
 * 2) Get whitespace tokens for each context, combine adjacent, insert for non exiting to make sure there is exactly
 *    one whitespace for a non-whitespace token.
 * 3) Combine multiple rule contexts' whitespaces to a get node's white space.
 */
public class BLangWSPreservingParserListener extends BLangParserListener {

    private final Stack<Stack<TokenRange>> rangesOfRuleContext = new Stack<>();
    private Set<Whitespace> usedTokens = new TreeSet<>();
    private final CommonTokenStream tokenStream;
    private final CompilationUnitNode compUnit;

    private ParserRuleContext getWSWasCalledOn;
    // Set of all WS, from the ParserContexts that has been exited, since the last call to getWS
    private SortedSet<Whitespace> wsSinceLastNode = new TreeSet<>();

    public BLangWSPreservingParserListener(CompilerContext context,
                                           CommonTokenStream tokenStream,
                                           CompilationUnitNode compUnit,
                                           BDiagnosticSource diagnosticSrc) {
        super(context, compUnit, diagnosticSrc);
        this.tokenStream = tokenStream;
        this.compUnit = compUnit;
        createNewRange(-1); // a fake range, repressions stuff before CompilationUnit.
    }

    private void createNewRange(int tokenIndex) {
        Stack<TokenRange> current = new Stack<>();
        current.push(new TokenRange(tokenIndex));
        rangesOfRuleContext.push(current);
    }

    private void closeLastRange(int rangeEndIndex) {
        // TODO: check why rangesOfRuleContext become empty before closing the last range.
        if (!rangesOfRuleContext.isEmpty()) {
            Stack<TokenRange> rangesOfLastRuleContext = rangesOfRuleContext.peek();
            TokenRange range = rangesOfLastRuleContext.peek();
            range.to = rangeEndIndex;
        }
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
        int tokenIndex = parserRuleContext.start.getTokenIndex();
        closeLastRange(tokenIndex);
        createNewRange(tokenIndex);
    }


    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
        if (getWSWasCalledOn == parserRuleContext) {
            return;
        }
        int rangeEndTokenIndex;
        if (parserRuleContext.stop == null) {
            ParseTree child = parserRuleContext.getChild(1);
            if (child instanceof TerminalNode) {
                // This is needed to handle A + B + C case of BinaryAddSubExpression
                rangeEndTokenIndex = ((TerminalNode) child).getSymbol().getTokenIndex();
            } else {
                rangesOfRuleContext.pop();
                return;
            }
        } else {
            rangeEndTokenIndex = parserRuleContext.stop.getTokenIndex() + 1;
        }

        closeLastRange(rangeEndTokenIndex);

        Stack<TokenRange> tokenRanges = new Stack<>();
        // TODO: check why rangesOfRuleContext become empty before closing the last range.
        if (!rangesOfRuleContext.isEmpty()) {
            tokenRanges = rangesOfRuleContext.pop();
        }

        Stack<Whitespace> ws = new Stack<>();
        for (TokenRange range : tokenRanges) {
            addWSFromRange(ws, range);
        }
        wsSinceLastNode.addAll(ws);

        if (!rangesOfRuleContext.isEmpty()) {
            rangesOfRuleContext.peek().add(new TokenRange(rangeEndTokenIndex));
        }
    }

    private void addWSFromRange(Stack<Whitespace> ws, TokenRange range) {
        int rangeStart = range.from;
        int rangeEnd = range.to;
        boolean lastTokenWasHidden = true;

        Token previousNonWS = null;
        for (int j = rangeEnd - 1; j >= -1; j--) {
            if (j == -1) {
                if (!lastTokenWasHidden) {
                    // capturing (non-exiting) WS at the start of range, if the range starts at 0.
                    // this happens if the file starts with a non-ws token.
                    pushWS(ws, previousNonWS, "");
                }
                break;
            }

            Token token = this.tokenStream.get(j);
            if (previousNonWS == null && token.getChannel() == Token.HIDDEN_CHANNEL) {
                continue;
            }
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                // we need to capture WS before the start of a range,
                // therefor only break after previous range's first non-WS.
                if (j < rangeStart) {
                    if (!lastTokenWasHidden) {
                        pushWS(ws, previousNonWS, "");
                        // capturing (non-exiting) WS at the start of range (when there is no space between ranges).
                    }
                    break;
                }
                // capturing (non-exiting) WS between two default tokens.
                if (!lastTokenWasHidden) {
                    pushWS(ws, previousNonWS, "");
                }
                lastTokenWasHidden = false;
                previousNonWS = token;
            } else {
                if (lastTokenWasHidden) {
                    // merging adjacent WS tokens.
                    ws.peek().prependWS(token.getText());
                } else {
                    // capturing (non-zero-len) WS.
                    pushWS(ws, previousNonWS, token.getText());
                }
                lastTokenWasHidden = true;
            }
        }
    }

    private static boolean isAllUpper(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (!Character.isUpperCase(ch) && ch != '_') {
                return false;
            }
        }
        return true;
    }

    private void pushWS(Stack<Whitespace> whitespaceStack, Token previousNonWS, String wsString) {
        boolean isStatic = isAllUpper(BallerinaLexer.VOCABULARY.getSymbolicName(previousNonWS.getType()));
        Whitespace wsToken = new Whitespace(previousNonWS.getTokenIndex(), wsString, previousNonWS.getText(), isStatic);
        whitespaceStack.push(wsToken);
    }

    @Override
    protected Set<Whitespace> getWS(ParserRuleContext parserRuleContext) {
        exitEveryRule(parserRuleContext);
        this.getWSWasCalledOn = parserRuleContext;
        SortedSet<Whitespace> wsForThisNode = wsSinceLastNode;

        // Same WS can't belong to two nodes, but parser allocates same token to two rule contexts. Hence de-duplicate.
        //TODO: this logic may be simplified, need a bigger sample set to check. check when unit tests are impl.
        for (Iterator<Whitespace> iterator = wsForThisNode.iterator(); iterator.hasNext(); ) {
            Whitespace ws = iterator.next();
            if (usedTokens.contains(ws)) {
                iterator.remove();
            } else {
                usedTokens.add(ws);
            }
        }

        wsSinceLastNode = new TreeSet<>();
        return wsForThisNode;
    }

    private static class TokenRange {
        int from;
        int to;

        TokenRange(int from) {
            this.from = from;
            this.to = -1;
        }

        @Override
        public String toString() {
            return "(" + from + "," + to + ")";
        }
    }

}
