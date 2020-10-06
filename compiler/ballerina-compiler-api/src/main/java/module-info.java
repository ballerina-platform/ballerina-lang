module org.ballerina.compiler {
    requires io.ballerina.tools.api;
    requires io.ballerina.lang;
    exports io.ballerina.compiler.api;
    exports io.ballerina.compiler.api.symbols;
    exports io.ballerina.compiler.api.types;
    exports io.ballerina.compiler.impl;
}
