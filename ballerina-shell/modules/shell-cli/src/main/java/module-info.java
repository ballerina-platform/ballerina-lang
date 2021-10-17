module io.ballerina.shell.cli {
    requires io.ballerina.shell;
    requires jline;
    requires com.google.gson;

    exports io.ballerina.shell.cli;
    exports io.ballerina.shell.cli.handlers.help;
    exports io.ballerina.shell.cli.handlers;
}
