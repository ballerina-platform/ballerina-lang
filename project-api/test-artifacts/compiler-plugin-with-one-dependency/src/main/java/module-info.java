module compiler.plugin.test.plugin.with.one.dependency {
    requires io.ballerina.lang;
    requires compiler.plugin.test.string.utils.lib;

    exports io.samjs.plugins.onedependency;
}
