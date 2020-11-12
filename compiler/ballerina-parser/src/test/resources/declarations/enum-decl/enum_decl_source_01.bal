#this is doc comment
enum SyntaxKind {
    A,
    B,
    C
}

public enum Color {
    RED,
    #this is green
    GREEN,
    @annotationsss
    BLUE
}

@Override:annotations
public enum Color {
    RED = "read-color",
    GREEN = "green-color",
    @checkmising
    isMissing = true,
    attributes,
    value = +5,
    #parents can be null
    @checknull
    parents = null,
    other
}
