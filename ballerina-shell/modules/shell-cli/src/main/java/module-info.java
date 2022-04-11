module io.ballerina.shell.cli {
    requires io.ballerina.shell;
    requires io.ballerina.parser;
    requires io.ballerina.lang;
    requires jline;
    requires com.google.gson;
    requires io.ballerina.tools.api;

    exports io.ballerina.shell.cli;
    exports io.ballerina.shell.cli.handlers.help;
    exports io.ballerina.shell.cli.handlers;
}
