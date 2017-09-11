package org.wso2.ballerinalang.compiler.parser;

import org.antlr.v4.runtime.Token;

public class WSToken implements Comparable<WSToken> {

    String ws;
    final int index;
    // Previous non-ws token's kept here for debugging reasons, no functional value.
    private final String previous;

    public WSToken(int index, String ws, String previous) {
        this.index = index;
        this.ws = ws;
        this.previous = previous;
    }

    public WSToken(String s, Token token) {
        this(token.getTokenIndex(), s, token.getText());
    }

    @Override
    public int compareTo(WSToken o) {
        return index - o.index;
    }

    public void appendWS(String text) {
        ws = ws + text;
    }

    @Override
    public String toString() {
        return "[" + index + "," + quote(previous) + "," +
                quote(ws).replace(' ', '\u2022') + "]";

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WSToken wsToken = (WSToken) o;
        return index == wsToken.index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    private static String quote(String string) {
        if (string == null) {
            return "null";
        }
        return '"' + string.replace("\n", "\\n").replace("\"", "\\\"") + '"';
    }
}
