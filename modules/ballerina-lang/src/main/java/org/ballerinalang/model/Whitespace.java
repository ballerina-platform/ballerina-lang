package org.ballerinalang.model;

/**
 * Represent a white space in the source code. There is a Whitespace for each non-whitespace token in source.
 * Whitespaces are indexed by the the proceeding token's index, and therefor unique to the index number.
 * Special index -1 is used for {@link org.ballerinalang.model.tree.CompilationUnitNode}'s whitespace,
 * that occurs at the start of hte file.
 */
public class Whitespace implements Comparable<Whitespace> {

    private String ws;
    private final int index;
    // Previous non-ws token's kept here for debugging reasons, no functional value.
    private final String previous;
    private final boolean isStatic;

    public String getPrevious() {
        return previous;
    }

    public Whitespace(int index, String ws, String previous, boolean isStatic) {
        this.index = index;
        this.ws = ws;
        this.previous = previous;
        this.isStatic = isStatic;
    }

    public String getWs() {
        return ws;
    }

    @Override
    public int compareTo(Whitespace o) {
        return index - o.index;
    }

    public void prependWS(String text) {
        ws = text + ws;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && index == ((Whitespace) o).index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return "[" + index + "," + quote(previous) + "," + quote(ws).replace(' ', '\u2022') + "]";
    }

    private static String quote(String string) {
        if (string == null) {
            return "null";
        }
        return '"' + string.replace("\n", "\\n").replace("\"", "\\\"") + '"';
    }

    public boolean isStatic() {
        return isStatic;
    }

    public int getIndex() {
        return index;
    }
}
