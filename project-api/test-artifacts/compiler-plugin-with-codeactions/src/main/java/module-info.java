module compiler.plugin.test.plugin.with.one.dependency {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;

    exports io.ballerina.plugins.codeaction;
}
