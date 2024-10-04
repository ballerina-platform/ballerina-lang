module io.ballerina.parser {
    requires io.ballerina.tools.api;
    requires static org.jetbrains.annotations;
    exports io.ballerina.compiler.syntax.tree;
    exports io.ballerina.compiler.internal.parser.tree to io.ballerina.lang;
}
