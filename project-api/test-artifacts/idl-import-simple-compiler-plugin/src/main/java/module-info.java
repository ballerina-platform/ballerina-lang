module simple.idl.client.plugin {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires compiler.plugin.test.diagnostic.utils.lib;

    exports io.idl.plugins.simpleclient;
}
