module io.ballerina.shell {
    requires io.ballerina.shell.rt;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires io.ballerina.lang;
    requires io.ballerina.runtime;
    requires io.ballerina.identifier;
    requires compiler;

    exports io.ballerina.shell.exceptions;
    exports io.ballerina.shell.invoker;
    exports io.ballerina.shell.invoker.classload;
    exports io.ballerina.shell.invoker.classload.context;
    exports io.ballerina.shell.parser;
    exports io.ballerina.shell.parser.trials;
    exports io.ballerina.shell.preprocessor;
    exports io.ballerina.shell.snippet;
    exports io.ballerina.shell.snippet.factory;
    exports io.ballerina.shell.snippet.types;
    exports io.ballerina.shell.utils;
    exports io.ballerina.shell;
}
