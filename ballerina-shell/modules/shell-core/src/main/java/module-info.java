module io.ballerina.shell {
    requires io.ballerina.shell.rt;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires io.ballerina.lang;
    requires io.ballerina.runtime;
    requires io.ballerina.identifier;
    requires compiler;

    exports io.ballerina.shell.exceptions;
    exports io.ballerina.shell.parser;
    exports io.ballerina.shell;
}
