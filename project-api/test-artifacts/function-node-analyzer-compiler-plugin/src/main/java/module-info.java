module function.node.analyzer.compiler.plugin {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires compiler.plugin.test.diagnostic.utils.lib;

    exports io.samjs.plugins.funcnodeanalyzer;
    exports io.samjs.plugins.lifecycle;
}
