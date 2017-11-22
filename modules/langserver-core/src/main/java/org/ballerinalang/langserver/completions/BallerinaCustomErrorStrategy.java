package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Capture possible errors from source.
 */
public class BallerinaCustomErrorStrategy extends BallerinaParserErrorStrategy {

    protected final TextDocumentPositionParams positionParams;

    protected List<PossibleToken> possibleTokens;

    private SuggestionsFilterDataModel suggestionsFilterDataModel;

    public BallerinaCustomErrorStrategy(CompilerContext compilerContext, TextDocumentPositionParams positionParams,
                                        SuggestionsFilterDataModel filterDataModel) {
        super(compilerContext, null);
        this.positionParams = positionParams;
        possibleTokens = new LinkedList<>();
        this.suggestionsFilterDataModel = filterDataModel;
    }
    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        fetchPossibleTokens(parser, e.getOffendingToken(), e.getExpectedTokens());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        fetchPossibleTokens(parser, parser.getCurrentToken(), parser.getExpectedTokens());
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        fetchPossibleTokens(parser, e.getOffendingToken(), e.getExpectedTokens());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        fetchPossibleTokens(parser, parser.getCurrentToken(), parser.getExpectedTokens());
    }

    public List<PossibleToken> getPossibleTokens() {
        return possibleTokens;
    }

    protected void fetchPossibleTokens(Parser parser, Token currentToken, IntervalSet expectedTokens) {
        ParserRuleContext currentContext = parser.getContext();
        // Currently disabling the check since the possible token based implementation has been skipped

        if (isCursorBetweenGivenTokenAndLastNonHiddenToken(currentToken, parser)) {
            this.suggestionsFilterDataModel.initParserContext(parser, currentContext, this.possibleTokens);
        }

    }
    /**
     * Checks whether cursor is within the whitespace region between current token to last token.
     * @param token Token to be evaluated
     * @param parser Parser Instance
     * @return true|false
     */
    protected boolean isCursorBetweenGivenTokenAndLastNonHiddenToken(Token token, Parser parser) {
        this.setContextException(parser);
        boolean isCursorBetween = false;
        int line = positionParams.getPosition().getLine();
        int character = positionParams.getPosition().getCharacter();

        Token lastNonHiddenToken = null;
        for (int tokenIdx = token.getTokenIndex() - 1; tokenIdx >= 0; tokenIdx--) {
            Token lastToken = parser.getTokenStream().get(tokenIdx);
            if (lastToken.getChannel() != Token.HIDDEN_CHANNEL) {
                lastNonHiddenToken = lastToken;
                break;
            }
        }
        if (lastNonHiddenToken != null) {
            if (line >= lastNonHiddenToken.getLine() && line <= token.getLine()) {
                if (line == lastNonHiddenToken.getLine()) {
                    isCursorBetween = character >= (lastNonHiddenToken.getCharPositionInLine()
                                    + lastNonHiddenToken.getText().length());
                } else {
                    isCursorBetween = line != token.getLine() || character <= token.getCharPositionInLine();
                }
            }
        }
        return isCursorBetween;
    }

    @Override
    protected void setContextException(Parser parser) {
        // Here the type of the exception is not important.
        InputMismatchException e = new InputMismatchException(parser);
        ParserRuleContext context = parser.getContext();
        // Note: Here we forcefully set the exception to null, in order to avoid the callable unit body being null at
        // the run time
        if (context instanceof BallerinaParser.CallableUnitBodyContext) {
            context.exception = null;
            return;
        }
        context.exception = e;
        // Note: Following check added, when the context is variable definition and the type name context is hit,
        // We need to set the error for the variable definition as well.
        if (context.getParent() instanceof BallerinaParser.VariableDefinitionStatementContext) {
            context.getParent().exception = e;
        }
    }
}
