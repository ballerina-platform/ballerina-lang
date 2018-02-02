package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Represents a possible token for current position.
 */
public class PossibleToken {

    private int tokenType;

    private int lastTokenIndex;

    private String tokenName;

    private ParserRuleContext currentContext;

    public PossibleToken (int tokenType, String tokenName, ParserRuleContext currentContext) {
        this.tokenType = tokenType;
        this.tokenName = tokenName;
        this.currentContext = currentContext;
    }

    public PossibleToken() {
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public int getTokenType() {
        return tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public ParserRuleContext getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(ParserRuleContext currentContext) {
        this.currentContext = currentContext;
    }

    public int getLastTokenIndex() {
        return lastTokenIndex;
    }

    public void setLastTokenIndex(int lastTokenIndex) {
        this.lastTokenIndex = lastTokenIndex;
    }
}
