package org.wso2.siddhi.core.table.predicate;

public class PredicateToken {

    public enum Type {
        VARIABLE, OPERATOR, VALUE;
    }

    private String tokenValue;
    private Type getTokenType;

    public PredicateToken() {
    }

    public PredicateToken(Type tokenType, String tokenValue) {
        this.getTokenType = tokenType;
        this.tokenValue = tokenValue;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public Type getGetTokenType() {
        return getTokenType;
    }

    public void setGetTokenType(Type getTokenType) {
        this.getTokenType = getTokenType;
    }
}
