package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Implements the getWS to actually return the whitespaces.
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

    public BLangWSPreservingParserListener(CommonTokenStream tokenStream,
                                           CompilationUnitNode compUnit,
                                           BDiagnosticSource diagnosticSrc) {
        super(compUnit, diagnosticSrc);
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
        Stack<TokenRange> rangesOfLastRuleContext = rangesOfRuleContext.peek();
        TokenRange range = rangesOfLastRuleContext.peek();
        range.to = rangeEndIndex;
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

        int rangeEndTokenIndex = parserRuleContext.stop.getTokenIndex() + 1;
        closeLastRange(rangeEndTokenIndex);

        Stack<TokenRange> tokenRanges = rangesOfRuleContext.pop();

        Stack<Whitespace> ws = new Stack<>();
        for (TokenRange range : tokenRanges) {
            addWSFromRange(ws, range);
        }
        wsSinceLastNode.addAll(ws);

        rangesOfRuleContext.peek().add(new TokenRange(rangeEndTokenIndex));
    }

    private void addWSFromRange(Stack<Whitespace> ws, TokenRange range) {
        int rangeStart = range.from;
        int rangeEnd = range.to;
        boolean lastTokenWasHidden = true;

        Token previousNonWS = null;
        for (int j = rangeStart; j < this.tokenStream.size(); j++) {
            Token token = this.tokenStream.get(j);
            if (previousNonWS == null && token.getChannel() == Token.HIDDEN_CHANNEL) {
                continue;
            }
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                // we need to capture dangling WS after end of a range,
                // therefor only break after next range's first non-WS.
                if (j >= rangeEnd) {
                    if (!lastTokenWasHidden) {
                        // capturing (non-exiting) WS at the end of range (when there is no space between ranges).
                        ws.push(new Whitespace("", previousNonWS));
                    }
                    break;
                }
                // capturing (non-exiting) WS between two default tokens.
                if (!lastTokenWasHidden) {
                    ws.push(new Whitespace("", previousNonWS));
                }
                lastTokenWasHidden = false;
                previousNonWS = token;
            } else {
                if (lastTokenWasHidden) {
                    // merging adjacent WS tokens.
                    ws.peek().appendWS(token.getText());
                } else {
                    // capturing (non-zero-len) WS.
                    ws.push(new Whitespace(token.getText(), previousNonWS));
                }
                lastTokenWasHidden = true;
            }
        }
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

    @Override
    public void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
        super.exitCompilationUnit(ctx);
        // CompilationUnit is special, it's the only element that has WS that is not prefixed by non-ws token
        // So we have to manually attach it.
        int i = 0;
        StringBuilder w = new StringBuilder();
        Token t;
        while ((t = this.tokenStream.get(i++)).getChannel() == Token.HIDDEN_CHANNEL) {
            w.append(t.getText());
        }
        compUnit.addWS(Collections.singleton(new Whitespace(-1, w.toString(), null)));
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
